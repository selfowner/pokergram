package org.pokedgram;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.core.util.Loader.getResourceBySelfClassLoader;
import static org.pokedgram.DeckUtils.initializeDeck;
import static org.pokedgram.DeckUtils.shuffleDeck;

//TODO() rename to PokedgramDealerBot
public class PokedgramBot extends TelegramLongPollingBot {


    final int STARTCHIPS = 20000;
    URL cfgPath = getResourceBySelfClassLoader("config.tg"); // added to .gitignore. 1 line - token, 2nd - roomId, 3rd - room invite link
    URL stickerPath = getResourceBySelfClassLoader("stickerpack.list");
    Path path, path1;
    List<String> cfg, stickerpack;

    {
        try {
            path        = Paths.get(cfgPath.toURI());
            path1       = Paths.get(stickerPath.toURI());
            cfg         = Files.readAllLines(path, StandardCharsets.UTF_8);
            stickerpack = Files.readAllLines(path1, StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
    String playingRoomId = cfg.get(1);
    String playingRoomLink = cfg.get(2);

    SendMessage message = new SendMessage();
    int buttonId = -1;
    //init
    ArrayList[] playersQueue;
    int registeredPlayers = 0;
    int extractNumber;
    int maxPlayers;
    int bigBlindId = -1;
    int smallBlindId = -1;
    int smallBlindSize, bigBlindSize;
    //int moveCount = 0;
    String userName, userId;
    //nextlines
    String text = "", currentPlayerHandText = "", allHandsText = "";
    String nextLine = "\n";
    String doubleNextLine = nextLine + nextLine;
    // "   clearstate - discard current state. *BROKEN" + doubleNextLine +
    String commands = "COMMANDS " + doubleNextLine + "   /about - about this bot  (alias command \"rules\")" + nextLine + "   /commands - this list" + nextLine + "   /poker <number> - start game registration with <number> of players" + nextLine + "   /reg, /unreg - register to the game, or forfeit" + nextLine + "   /invite <userId> - invite tg user to join game (success only if user communicated with bot in personal messages earlier)" + nextLine + "   /queue - get registered members list" + doubleNextLine + "To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat " + playingRoomLink + " (without '/' before command)" + doubleNextLine + "At this moment, GUI (buttons etc.) not implemented. To set up in 1 step input command about." + " ";
    String tripleNextLine = nextLine + nextLine + nextLine;
    String rulesText = "I'm NL holdem dealer bot. " + doubleNextLine + "To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat " + playingRoomLink + " (without '/' before command)" + doubleNextLine + "To start game: " + nextLine + "1. type command \"poker [2-9]\" e.g. \"poker 3\" directly into playing room chat group, where arg = number of sits(=players) in the game. accepted values are >=2 && <=9 " + nextLine + "2. type command \"reg\" to register to the game. (Also you can send invite to telegram user via \"invite\" command; Also player can unregister by using \"unreg\" command)" + tripleNextLine + "When registered users will take all the sits, game starts." + tripleNextLine + "Game scenario:" + nextLine + "1. Dealer unpacks and shuffles deck, then deals 1 card to each player (Cards revealed). Deal order determined by players registration order. " + nextLine + "2. The player with highest card starts on button, the players after him start on blinds. (value order on roll phase currently is 2<3<4<5<6<7<8<9<10<J<Q<K<A>2; if more than 1 player got same highest value, dealer shuffles deck and deal repeats. )" + nextLine + "3. The game starts, and all the rules match TEXAS NL HOLDEM : " + nextLine + "Cards are dealt in private message from bot. Player on his turn have to choose one of options (fold,check|call,bet|raise|reraise). Player types his decision on his turn in playing room. Additionally, there is an option to schedule decision @ bot pm." + doubleNextLine + "useless links:" + nextLine + "https://automaticpoker.com/how-to-play-poker/texas-holdem-basic-rules/" + nextLine + "https://automaticpoker.com/poker-basics/burn-and-turn-rules/" + nextLine + "https://automaticpoker.com/poker-basics/texas-holdem-order-of-play/" + tripleNextLine + "READYCHECK" + doubleNextLine + "pregame:" + nextLine + "+- create nl holdem game by type: +STP, -MTP, -cash" + nextLine + "+- game registration mechanics: +reg, +unreg, +invite, +-queue, +-clearstate" + nextLine + "+- card deal mechanics: +draw, +-preflop, +-turn, +-river, -showdown, +-burnwhendealing" + doubleNextLine + "game:" + nextLine + "+- game state mechanics: +countBlindSize, +countBlindId, -substractfromplayerbalance, -addtopot +-removeBankrupt, +-finishGame " + nextLine + nextLine + "+- player hand ranking: -pairs -2pairs -triple -fullhouse -quad -street +flash -streetflash +-highestkicker" + nextLine + "- player turn phases mechanics: -check -call +-bet -raise -reraise -allin -pmMoveScheduler -afk" + nextLine + "- clearing phase mechanics: -calculateCombinations -splitPotReward" + nextLine + "misc:" + nextLine + "+- gui: +mudMode, +-msgEdit, -commands, -inlineCommands, -markupMessages" + nextLine + "-stats: -processUsersStat, -provideUserStat, -processTournamentStat, -provideTournamentStat" + tripleNextLine + "TL;DR: currently unplayable after cards dealt";
    //String commandRegEx = "^(.*)([0-9]*)(.*)$";
    //String commandRegExp = "^/(.*)(@pokedgram_bot) ([0-9]*)(.*)$";
    String commandRegExp = "^(.[a-zA-Z@_]*) ([0-9]*)(.*)$";
    String argRegExp = "^.([a-zA-Z@_ ]*)(.*)$";
    boolean tradePreflopDone = false;
    boolean tradeFlopDone = false;
    boolean tradeTurnDone = false;
    boolean tradeRiverDone = false;
    boolean registrationStarted = false;
    //    boolean isTradePreflopDone = false;
//    boolean isTradeFlopDone = false;
//    boolean isTradeRiverDone = false;
    boolean preGameStarted = false;
    boolean gameStarted = false;
    boolean rollVisibility = true;
    //    boolean tradeDone = false;
    boolean dealVisibility = false;
    int dealNumber;
    //tournament settings
    int startSmallBlindSize = 250;
    boolean sendToPlayerCheckOnReg = true;
    int HANDCARDSCOUNT = 2; // holdem
    int SLEEPMS = 3000;
    Integer minBetSize = 0;
    String usr1 = "";
    String usr2 = "";
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
    InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
    InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
    InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
    InlineKeyboardButton inlineNumericButton1 = new InlineKeyboardButton();
    InlineKeyboardButton inlineNumericButton2 = new InlineKeyboardButton();
    InlineKeyboardButton inlineNumericButton3 = new InlineKeyboardButton();
    InlineKeyboardButton inlineNumericButton4 = new InlineKeyboardButton();
    InlineKeyboardButton inlineNumericButton5 = new InlineKeyboardButton();
    InlineKeyboardButton helpButton = new InlineKeyboardButton();


    @Override
    public void onRegister() {
        try {

            message.setChatId(playingRoomId);
            message.setText(commands);
            execute(message);
            //System.out.println("onRegister ok: " + message.toString());
            //sendSticker();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("onRegister exception: " + message);
        }
    }

    @Override
    public String getBotUsername() {

        return cfg.get(3);
    }

    @Override
    public String getBotToken() {

        return cfg.get(0);

    }

    public void sendSticker() {
        try {
            String currentId = message.getChatId();
            message.setChatId(playingRoomId);
            SendSticker sendSticker = new SendSticker();
            sendSticker.setChatId(playingRoomId);
            sendSticker.setSticker(new InputFile(stickerpack.get(0)));
            execute(sendSticker);
            try {
                message.setChatId(currentId);
            } catch (NullPointerException e) {
                System.out.println("exception: " + message);
            }
            Thread.sleep(SLEEPMS);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendToPlayingRoom(String text) {
        try {
            message = new SendMessage();
            message.setChatId(playingRoomId);
            message.setText(text);
            execute(message);
            Thread.sleep(SLEEPMS);

        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean sendToPlayer(String userId, String text) {
        try {
            message = new SendMessage();
            message.setChatId(userId);
            message.setText(text);
            execute(message);
            Thread.sleep(SLEEPMS);
            return true;
            //System.out.println("ok: " + message);
        } catch (TelegramApiException | InterruptedException e) {
            //e.printStackTrace();
            System.out.println("exception: " + e.getMessage() + nextLine + "message: " + message);
            return false;
        }
    }

    public void sendBack(String userId, String text) {
        message = new SendMessage();
        try {
            message.setChatId(userId);
            message.setText(text);
            execute(message);
            //System.out.println("ok: " + message);
            Thread.sleep(SLEEPMS);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: " + message);
        }


    }

    public Integer sendToPlayingRoomAndGetMessageId(String text) {
        message = new SendMessage();
        int messageId = -1;
        try {
            message.setChatId(playingRoomId);
            message.setText(text);
            messageId = execute(message).getMessageId();
            Thread.sleep(SLEEPMS);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message);
        }
        return messageId;
    }


    public Integer sendToPlayingRoomWithButtonsAndGetMessageId(String text) {
        message = new SendMessage();
        int messageId = -1;
        try {
            message.setChatId(playingRoomId);
            message.setText(text);
            message.setReplyMarkup(inlineKeyboardMarkup);

            messageId = execute(message).getMessageId();
            Thread.sleep(SLEEPMS);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message);
        }
        return messageId;
    }



    public void editMessage(String text, Integer messageId) {


        EditMessageText msg = new EditMessageText();
        try {
            msg.setChatId(playingRoomId);
            msg.setMessageId(messageId);
            msg.setText(text);
            execute(msg);
            Thread.sleep(SLEEPMS);
        } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg);
        }
    }

    public void editMessageWithButtons(String text, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        try {
            msg.setMessageId(messageId);
            msg.setText(text);
            msg.setReplyMarkup(inlineKeyboardMarkup);
            execute(msg);
            Thread.sleep(SLEEPMS);
        } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg);
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
//        if (update != null) {
//
//            update.getInlineQuery().
//            InlineQueryResult r1 = new InlineQueryResultArticle("1", "title 1", "content 1");
//            InlineQueryResult r2 = new InlineQueryResultArticle("2", "title 2", "content 2");
//
//            execute(
//                            new AnswerInlineQuery(
//                                    update.setCallbackQuery(new CallbackQuery()).inlineQuery().id(), r1, r2).cacheTime(0));
//        }
        if (update.hasMessage()) {
            usr1 = update.getMessage().getFrom().getId().toString();
        } else {
            usr1 = "0";
        }


        if (update.hasCallbackQuery()) {
            usr2 = update.getCallbackQuery().getFrom().getId().toString();
            try {
                SendMessage dfgdfg = new SendMessage();
                dfgdfg.setText(update.getCallbackQuery().getData());
                dfgdfg.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                execute(dfgdfg);
                Thread.sleep(SLEEPMS);
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            usr2 = "0";
        }

        {

            inlineKeyboardButton1.setText("fold");
            inlineKeyboardButton2.setText("check");
            inlineKeyboardButton3.setText("call");
            inlineKeyboardButton4.setText("bet");
            inlineKeyboardButton2.setCallbackData("check pressed by - usrid: " + usr1 + ", cbid: " + usr2);
            inlineKeyboardButton1.setCallbackData("fold pressed by " + usr1 + ", cbid: " + usr2);
            inlineKeyboardButton3.setCallbackData("call pressed by " + usr1 + ", cbid: " + usr2);
            inlineKeyboardButton4.setCallbackData("bet pressed by " + usr1 + ", cbid: " + usr2);
            minBetSize = (((dealNumber / 5) + 1) * startSmallBlindSize) * 2;
            inlineNumericButton1.setText(String.valueOf(minBetSize));

            inlineNumericButton2.setText(String.valueOf(minBetSize * 2));
            inlineNumericButton3.setText(String.valueOf(minBetSize * 3));

            inlineNumericButton4.setText(String.valueOf(minBetSize * 4));
            inlineNumericButton5.setText("\uD83D\uDD34 allin");
            inlineNumericButton1.setCallbackData(String.valueOf(minBetSize));
            inlineNumericButton2.setCallbackData(String.valueOf(minBetSize * 2));
            inlineNumericButton3.setCallbackData(String.valueOf(minBetSize * 3));
            inlineNumericButton4.setCallbackData(String.valueOf(minBetSize * 4));
            inlineNumericButton5.setCallbackData("\uD83D\uDD34 allin");
            helpButton.setText("fold");
            helpButton.setCallbackData("=(");
            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();


            keyboardButtonsRow1.add(inlineKeyboardButton1);
            keyboardButtonsRow1.add(inlineKeyboardButton2);
            keyboardButtonsRow1.add(inlineKeyboardButton3);
            keyboardButtonsRow1.add(inlineKeyboardButton4);
            keyboardButtonsRow2.add(inlineNumericButton1);
            keyboardButtonsRow2.add(inlineNumericButton2);
            keyboardButtonsRow2.add(inlineNumericButton3);
            keyboardButtonsRow2.add(inlineNumericButton4);
            keyboardButtonsRow2.add(inlineNumericButton5);
            keyboardButtonsRow3.add(helpButton);


            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow1);
            rowList.add(keyboardButtonsRow2);
            rowList.add(keyboardButtonsRow3);
            inlineKeyboardMarkup.setKeyboard(rowList);
        } //setup inline buttons for bets phase
        // ToDO() refactor
        System.out.println("onUpdateReceived inited, usr1 = " + usr1 + ", usr2 = " + usr2);

        
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userName, userId;
            userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            userId   = String.valueOf(update.getMessage().getFrom().getId());
            System.out.println("got update with text, from userName" + userName + "(" + userId + ")");

            if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {
                System.out.println("if gamestarted == true");
                sendToPlayingRoom("deal order old: " + nextLine + PlayerUtils.getUserRegQueue(playersQueue));
                ArrayList[] players = new ArrayList[ playersQueue.length ];
                players = playersQueue;
                players = PlayerUtils.shiftPlayerOrder(players, buttonId);
                int playersCount = players.length;
                //sendToPlayingRoom("deal order: " + nextLine + PlayerUtils.getUserRegQueue(players));

                boolean tournamentReady = false;
                while (!tournamentReady && gameStarted) {
                    //boolean dealReady = false;
                    boolean tradeReady;// = false;
                    boolean clearingReady = false;
                    boolean tableReady;// = false;

                    sendToPlayingRoom("deal order: " + nextLine + PlayerUtils.getUserRegQueue(players));


                    //game start
                    tradeReady = true;


                    while (tradeReady) { //round start
                        dealNumber = dealNumber + 1;
                        //boolean preflopPhase = true, flopPhase = false, turnPhase = false, riverPhase = false, showdown = false, tradeDone = false;
                        boolean preflopPhase, flopPhase = false, turnPhase, riverPhase, showdown;//, tradeDone = false;
                        smallBlindSize = ((dealNumber / 5) + 1) * startSmallBlindSize;
                        bigBlindSize   = smallBlindSize * 2;
                        HANDCARDSCOUNT = 2;

                        if (players.length == 2) {
                            smallBlindId = 0;
                            bigBlindId   = smallBlindId + 1;
                        } else {
                            System.out.println("else players.lenght != 2");
                            break;
                        }

                        String roundAnnounceString = "deal number: " + dealNumber + nextLine + "\uD83D\uDD35 small blind: " + smallBlindSize + nextLine + "\uD83D\uDFE1 big blind: " + bigBlindSize;


                        clearingReady = false;
                        tableReady    = false;
                        while (!tableReady) {
                            List<?> deck = initializeDeck();

                            deck = shuffleDeck(deck);
                            Integer dealMessageId = sendToPlayingRoomWithButtonsAndGetMessageId(roundAnnounceString + nextLine + "*Dealer shuffling deck*");

                            ArrayList[][] playerCards = new ArrayList[ playersCount ][ HANDCARDSCOUNT ];

                            playerCards           = DeckUtils.dealCards(players.length, HANDCARDSCOUNT, deck);
                            text                  = "";
                            currentPlayerHandText = "";
                            allHandsText          = "";


                            int currentPot = 0;
                            boolean isSidePotExist = false;

                            String tableCards;
                            //boolean phaseBets = true;

                            for (int y = 0; y < players.length; y++) {
                                if (y < 2) {
                                    players[ y ].set(3, String.valueOf(Integer.parseInt(players[ y ].get(3).toString()) - smallBlindSize * (y + 1))); //current chips at player


                                    players[ y ].set(9, String.valueOf(Integer.parseInt(players[ y ].get(9).toString()) + smallBlindSize * (y + 1))); // current bet at player
                                    players[ y ].set(9, String.valueOf(Integer.parseInt(players[ y ].get(9).toString()) + smallBlindSize * (y + 1))); // current bet at player
                                    currentPot = Integer.parseInt((players[ y ].get(9).toString()) + smallBlindSize * (y + 1));
                                }
                                StringBuilder hand = new StringBuilder();
                                for (int i = 0; i < HANDCARDSCOUNT; i++) {
                                    hand.append(playerCards[ y ][ i ].get(0));
                                }
                                currentPlayerHandText = "player " + players[ y ].get(1) + nextLine + " hand: " + hand + nextLine;

                                sendToPlayer(players[ y ].get(0).toString(), "dealNumber " + dealNumber + nextLine + "Your cards: " + currentPlayerHandText);
                                allHandsText = allHandsText + currentPlayerHandText;
                            }

                            if (dealVisibility) {
                                //editMessageWithButtons(roundAnnounceString + nextLine + "*Dealer shuffling deck*", dealMessageId);
                                editMessageWithButtons(roundAnnounceString + nextLine + "*Dealer shuffling deck*" + doubleNextLine + allHandsText, dealMessageId);
                                //editMessage("*Dealer shuffling deck*" + " dealnumber: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "Deck info: " + deck, dealMessageId);
                            }
                            editMessageWithButtons(roundAnnounceString + nextLine + "*Dealer shuffling deck*" + tripleNextLine + "current move @ player order: " + nextLine + players[ 0 ].get(1), dealMessageId);

                            preflopPhase = true;
                            while (preflopPhase) {

                                // TODO() broken logic, check tradeready goes here
                                int moveCount = 0;
                                while (moveCount < players.length && !PlayerUtils.checkBetsEqual(players)) { //userId =
                                    //if (update.hasMessage() && update.getMessage().hasText() && phaseBets) {

                                    // if recieve msg
                                    if (String.valueOf(update.getMessage().getFrom().getId())
                                                .equals(players[ moveCount ].get(0).toString())
                                        ||
                                        String.valueOf(update.getCallbackQuery().getFrom().getId())
                                                .equals(players[ moveCount ].get(0).toString())) {

                                        if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {
                                            tradePreflopDone = true;
                                            break;
                                        }
                                        System.out.println("movecount: " + moveCount);
                                        sendToPlayingRoom("userId " + update.getMessage().getFrom().getId() + " matched " + players[ moveCount % players.length ].get(0));

                                        //String extractCommand;//, arg;
                                        //int extractNumber = 0;
                                        //extractCommand = update.getMessage().getText().replaceAll(commandRegExp, "$1");

                                            /*                                            {
                                                try {
                                                    extractNumber = Integer.parseInt(update.getMessage().getText().replaceAll(argRegExp, "$2"));

                                                } catch (NumberFormatException e) {
                                                    extractNumber = smallBlindSize;
                                                }
                                            }*/ //extract arg from msg

                                        //phaseBets = false;

                                            /*switch (extractCommand.toLowerCase()) {
                                                case "check", "/check" -> {
                                                    sendToPlayingRoom("preflopPhase && phaseBets case check" + extractNumber);
                                                    moveCount++;
                                                }
                                                case "call", "/call" -> {
                                                    sendToPlayingRoom("preflopPhase && phaseBets case call" + extractNumber);
                                                    moveCount++;
                                                }
                                                case "bet", "raise", "/bet", "/raise" -> {
                                                    sendToPlayingRoom("preflopPhase && phaseBetscase bet" + extractNumber);
                                                    moveCount++;
                                                }
                                                case "allin", "all-in", "/allin", "/all-in" -> {
                                                    sendToPlayingRoom("preflopPhase && phaseBets case allin" + extractNumber);
                                                    moveCount++;
                                                }
                                                case "fold", "pass", "/fold", "/pass" -> {
                                                    sendToPlayingRoom("preflopPhase && phaseBets case fold" + extractNumber);
                                                    moveCount++;
                                                }

                                                default -> {
                                                    System.out.println("checkBetsEqual = " + PlayerUtils.checkBetsEqual(players));
                                                    sendToPlayingRoom("msgid " + update.getMessage().getMessageId() + nextLine + "userId: " + userId + nextLine + "getmessageuserid: " + update.getMessage().getFrom().getId() + nextLine + "players[smallBlindId].get(0) = " + players[ smallBlindId ].get(0));
                                                    return;
                                                }
                                            }*/ //switch for msg


                                        return;
                                    } else {
                                        //                                            sendToPlayingRoom("userId " + update.getMessage().getFrom().getId() + " not matched " + players[ smallBlindId ].get(0));
//                                            sendToPlayingRoom("expecting move from player" + players[ smallBlindId ].get(0) + " (" + players[ smallBlindId ].get(1) + ")" + nextLine);
//                                            try {
//                                                Thread.sleep(SLEEPMS);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }

//                                            tradeFlopDone    = true;
//                                            tradeTurnDone    = true;
//                                            tradeRiverDone   = true;

                                        //currentPot = ;
                                        System.out.println("moveCount >= players.length && PlayerUtils.checkBetsEqual(players) \nmovecount: " + moveCount);
                                        return;


                                    }
                                }
                                preflopPhase = false;
                                flopPhase    = true;

                                while (flopPhase) {

                                    turnPhase = true;
                                    String flopCards = DeckUtils.phaseFlop(maxPlayers, HANDCARDSCOUNT, deck);
                                    tableCards = flopCards;
                                    editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "flop phase: " + nextLine + tableCards, dealMessageId);

                                    while (turnPhase) {
                                        riverPhase = true;
                                        String turnCards = flopCards + DeckUtils.phaseTurn(maxPlayers, HANDCARDSCOUNT, deck);
                                        tableCards = turnCards;
                                        editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "turn phase: " + nextLine + tableCards, dealMessageId);


                                        while (riverPhase) {

                                            tableCards = turnCards + DeckUtils.phaseRiver(maxPlayers, HANDCARDSCOUNT, deck);
                                            editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "river phase: " + nextLine + tableCards + doubleNextLine + "deck: " + deck, dealMessageId);

                                            //when bets equal and betsquantity>1 showdown=true;
                                            showdown = true;
                                            while (showdown) {
                                                System.out.println("im in showdown");
                                                while (!clearingReady) { //distribute chips
                                                    System.out.println("while (!clearingReady)");
                                                    // countCombinations
                                                    // if 0 tokens, remove player from game, stat his place
                                                    // if 1 player left, finish game
                                                    //find min stack
                                                    //if minstack > 0 minimum 2 times, set Tradeready false
                                                    //reinit players, cleanup playerhands, go to tradeready
                                                    players = PlayerUtils.checkLastChips(players, bigBlindSize); //playersCount;
                                                    ArrayList[] playersOld = PlayerUtils.shiftPlayerOrder(players, 1);
                                                    //players = new ArrayList[playersOld.length];

                                                    // add reward;

                                                    if (players.length == 1) {
                                                        //finish game and count prize
                                                        //break;
                                                        System.out.println("1 player left, game over");
                                                        return;
                                                    }

                                                    clearingReady = true;
                                                    showdown      = false;
                                                    try {
                                                        Thread.sleep(SLEEPMS);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return;
                                                }

                                                //clearingReady = true;

                                            }

                                        }

                                    }

                                    //tradeDone = false;
                                    preflopPhase = false;
                                    flopPhase    = false;
                                    riverPhase   = false;
                                    showdown     = false;
                                    tableReady   = true;
                                    tradeReady   = false;

                                    dealNumber++;

                                    return;

                                }
                                return;
                            }
                            return;

                        }
                        return;
                    }
                    return;
                }
            }


//        }
//
//        if (update.hasMessage() && update.getMessage().hasText()) {


            //pregame started (reg finished)
            if (!gameStarted && preGameStarted) {
                int tryCount = 0;
                int rollWinnerPlayerId;

                List<?> deck = initializeDeck();

                deck = shuffleDeck(deck);
                tryCount++;
                Integer rollMessageId = sendToPlayingRoomAndGetMessageId("*Dealer shuffling deck*");
                ArrayList[][] playerCards = DeckUtils.dealCards(maxPlayers, 1, deck);
                text                  = "";
                currentPlayerHandText = "";
                allHandsText          = "";

                for (int y = 0; y < maxPlayers; y++) {
                    StringBuilder hand = new StringBuilder();
                    for (int i = 0; i < 1; i++) {
                        hand.append(playerCards[ y ][ i ].get(0));
                    }
                    currentPlayerHandText = playersQueue[ y ].get(1).toString() + ": " + hand + nextLine;

                    sendToPlayer(playersQueue[ y ].get(0).toString(), "pregame roll number " + tryCount + nextLine + "Your cards: " + currentPlayerHandText);
                    allHandsText += currentPlayerHandText;
                }


                if (rollVisibility) {
                    editMessage("*Dealer shuffling deck*" + doubleNextLine + allHandsText, rollMessageId);
                }

                rollWinnerPlayerId = DeckUtils.rankRoll(maxPlayers, playerCards);


                if (rollWinnerPlayerId == -1) {
                    editMessage("*Dealer shuffling deck*" + doubleNextLine + allHandsText + doubleNextLine + "We've equal highest, redraw (trycount " + tryCount + ")" + doubleNextLine + "Deck info: " + deck, rollMessageId);
                    gameStarted = false;
                } else {
                    editMessage("*Dealer shuffling deck*" + doubleNextLine + allHandsText + doubleNextLine + playersQueue[ rollWinnerPlayerId ].get(1).toString() + " starting on the button" + tripleNextLine + "Deck info: " + deck, rollMessageId);
                    buttonId    = rollWinnerPlayerId;
                    gameStarted = true;
                    return;
                    //TODO() delete pregame commands and add game commands promt
                    //TODO() create comfortable UX to make moves (inline/buttons/etc)
                }
                //} return;

            }

            //before reg open
            if (!gameStarted && !preGameStarted) {

                userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + " " + "(" + update.getMessage().getFrom().getUserName() + ")";
                userId   = String.valueOf(update.getMessage().getFrom().getId());
                String chatId = String.valueOf(update.getMessage().getChatId());

                String extractCommand, arg;
                extractCommand = update.getMessage().getText().replaceAll(commandRegExp, "$1");

                if (extractCommand.length() < update.getMessage().getText().length()) {
                    try {
                        arg = update.getMessage().getText().replaceAll(argRegExp, "$2");
                        System.out.println("arg = " + arg + nextLine + "extractCommand = " + extractCommand);
                        extractNumber = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        extractNumber = 0;
                    }

                }

                // pregame menu switch
                switch (extractCommand.toLowerCase()) {
                    case "commands", "/commands", "/commands@pokedgram_bot":
                        sendBack(chatId, commands);
                        break;

                    case "rules", "/rules", "/rules@pokedgram_bot", "/about", "/about@pokedgram_bot":
                        sendBack(chatId, rulesText);
                        break;

//                    case "btntst":
//
//                        Integer btntst = sendToPlayingRoomAndGetMessageId("buttons test");
//                        SendMessage newmsg = new SendMessage();
//                        newmsg.setReplyMarkup(inlineKeyboardMarkup);
//                        sendToPlayingRoom(newmsg, "zxc");
                    //execute(sendInlineKeyBoardMessage());
                    //sendBack(chatId, rulesText);
                    //break;

                    case "reg", "/reg", "/reg@pokedgram_bot":
                        if (registrationStarted) { // TODO() split prereg and reg switch

                            int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);
                            if (checkAlreadyReg < 0) {


                                boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                if (!checkQueueFull || checkForUnreg > -1) {
                                    if (sendToPlayerCheckOnReg && !sendToPlayer(userId, "Register success.")) {
                                        sendToPlayingRoom("Register failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                    } else {
                                        playersQueue = PlayerUtils.addPlayerToQueue(userId, userName, playersQueue, playingRoomId, registeredPlayers, STARTCHIPS, checkQueueFull, checkForUnreg);
                                        registeredPlayers++;
                                        sendToPlayingRoom(userName + " registered to the game!" + (maxPlayers - registeredPlayers) + " to go");

                                        if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                            preGameStarted = true;

                                            sendToPlayingRoom("Players registered! Lets roll!");
                                            return;
                                        }
                                        return;

                                    }
                                } else
                                    sendBack(chatId, "Register failed: checkForUnreg - " + checkForUnreg + ", checkForUnreg - " + checkForUnreg);
                            } else sendBack(chatId, "Register failed or limits");
                        } else sendBack(chatId, "Registration is not open");

                    case "invite", "/invite", "/invite@pokedgram_bot", "invite@pokedgram_bot":
                        if (registrationStarted) {
                            if (extractNumber >= 10000000 && extractNumber <= 2121212100 && extractNumber != 2052704458) {
                                String inviteUserId = String.valueOf(extractNumber);


                                int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(inviteUserId, playersQueue, registeredPlayers);
                                if (checkAlreadyReg < 0) {

                                    boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                    int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                    if (!checkQueueFull || checkForUnreg > -1) {

                                        if (sendToPlayerCheckOnReg && !sendToPlayer(inviteUserId, "You have been invited to play some poker")) {
                                            sendToPlayingRoom("Invite failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                        } else {
                                            playersQueue = PlayerUtils.addPlayerToQueue(inviteUserId, "Player" + inviteUserId, playersQueue, playingRoomId, registeredPlayers, STARTCHIPS, checkQueueFull, checkForUnreg);

                                            // TODO() count invites seperately
                                            registeredPlayers++;
                                            //sendToPlayer(userId, "Invite success. Current queue: " + PlayerUtils.getUserReg(playersQueue, registeredPlayers, userId));
                                            sendToPlayer(userId, "Invite success. Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                                            sendToPlayingRoom(inviteUserId + " invited to the game! " + (maxPlayers - registeredPlayers) + " to go");

                                            if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                                preGameStarted = true;
                                                //registrationStarted = false;
                                                sendToPlayingRoom("Players registered! Lets roll!");
                                                return;
                                            } else {
                                                preGameStarted = false;
                                            }
                                        }
                                    }
                                } else
                                    sendToPlayer(userId, "Invite failed: checkReg - " + checkAlreadyReg + ", checkLimits - " + PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers));
                            } else sendToPlayer(userId, "userId " + extractNumber + "do not match format.");
                        } else sendBack(chatId, "Registration is not open");


                    case "/queue", "queue":
                        if (registrationStarted) {
                            sendBack(chatId, "Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                        } else {
                            sendBack(chatId, "Registration is not open");
                        }
                        return;

                    case "/poker", "poker", "poker@pokedgram_bot", "/poker@pokedgram_bot":
                        playersQueue = null;
                        if (extractNumber >= 1 && extractNumber <= 9) {
                            maxPlayers = extractNumber;
                        } else {
                            maxPlayers = 9;
                        }
                        registeredPlayers = 0;
                        sendToPlayingRoom("Tournament initiated! Registration free and open. Game Texas NL Holdem." + doubleNextLine + "playersCount: " + maxPlayers + nextLine + "stp config " + nextLine + "smallBlind " + startSmallBlindSize + nextLine + "startChips " + STARTCHIPS + nextLine + "blindIncrease x2 every 5 rounds");
                        registrationStarted = true;
                        playersQueue = new ArrayList[ maxPlayers ];
                        return;

                    default:
                        return;


                }
            }


        }

        System.out.println("onUpdate end");

    }

}


//                case "/clearstate":
//                    if (userId.equals("193873212")) {
//                        sendToPlayer(userId, "clearstate AFFORMINAVE");
//                        sendToPlayingRoom("clearstate command recieved, trying..");
//
//                        playersQueue = null;
//                        registeredPlayers = 0;
//                        registrationStarted = false;
//                        preGameStarted = false;
//                        gameStarted = false;
//                        exe.shutdown();
//                        // TODO () clean all
//                    } else {
//                        sendToPlayer(userId, "clearstate AFFORMINAVE");
//                    }
//                    break;
//
//                    case "unreg":
//                        if (registrationStarted) {
//
//                            if (PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers) >= 0) {
//                                playersQueue = PlayerUtils.userUnreg(playersQueue, userId);
//                                registeredPlayers--;
//                                sendToPlayer(userId, "Unregistered");
//                            }
//
//                        } else {
//                            sendBack("Registration is not open");
//                        }
//                        break; //
// clear and unreg