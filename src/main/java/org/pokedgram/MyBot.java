package org.pokedgram;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
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


public class MyBot extends TelegramLongPollingBot {

    URL cfgPath = getResourceBySelfClassLoader("config.tg"); // added to .gitignore. 1 line - token, 2nd - roomId, 3rd - room invite link
    URL stickerPath = getResourceBySelfClassLoader("stickerpack.list");
    Path path, path1;
    List<String> cfg, stickerpack;
    {
        try {
            path = Paths.get(cfgPath.toURI());
            path1 = Paths.get(stickerPath.toURI());
            cfg = Files.readAllLines(path, StandardCharsets.UTF_8);
            stickerpack = Files.readAllLines(path1, StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }}


    SendMessage message = new SendMessage();

    int buttonId = -1;
    //init
    ArrayList[] playersQueue;

    String playingRoomId = cfg.get(1);
    String playingRoomLink = cfg.get(2);


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
    String tripleNextLine = nextLine + nextLine + nextLine;

    //String commandRegEx = "^(.*)([0-9]*)(.*)$";
    String commandRegEx = "^(.*) ([0-9]*)(.*)$";
    String argRegEx = "^([a-zA-Z ]*)(.*)$";
    // "   clearstate - discard current state. *BROKEN" + doubleNextLine +
    String commands = "COMMANDS " + doubleNextLine +
            "   about - about this bot  (alias command \"rules\")" + nextLine +
            "   commands - this list" + nextLine +
            "   poker <number> - start game registration with <number> of players" + nextLine +
            "   reg/unreg - register to the game, or forfeit" + nextLine +
            "   invite <userId> - invite tg user to join game (success only if user communicated with bot in personal messages earlier)" + nextLine +
            "   queue - get registered members list" + doubleNextLine +
            "To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat " + playingRoomLink +
            " (without '/' before command)" + doubleNextLine +
            "At this moment, GUI (buttons etc.) not implemented. To set up in 1 step input command about." +
            " ";

    String rulesText = "I'm NL holdem dealer bot. " + doubleNextLine +
            "To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat " + playingRoomLink + " (without '/' before command)" + doubleNextLine +
            "To start game: " + nextLine +
            "1. type command \"poker [2-9]\" e.g. \"poker 3\" directly into playing room chat group, where arg = number of sits(=players) in the game. accepted values are >=2 && <=9 " + nextLine +
            "2. type command \"reg\" to register to the game. (Also you can send invite to telegram user via \"invite\" command; Also player can unregister by using \"unreg\" command)" + tripleNextLine +
            "When registered users will take all the sits, game starts." + tripleNextLine + "Game scenario:" + nextLine +
            "1. Dealer unpacks and shuffles deck, then deals 1 card to each player (Cards revealed). Deal order determined by players registration order. " + nextLine +
            "2. The player with highest card starts on button, the players after him start on blinds. (value order on roll phase currently is 2<3<4<5<6<7<8<9<10<J<Q<K<A>2; if more than 1 player got same highest value, dealer shuffles deck and deal repeats. )" + nextLine +
            "3. The game starts, and all the rules match TEXAS NL HOLDEM : " + nextLine +
            "Cards are dealt in private message from bot. Player on his turn have to choose one of options (fold,check|call,bet|raise|reraise). Player types his decision on his turn in playing room. Additionally, there is an option to schedule decision @ bot pm." +
            doubleNextLine + "useless links:" + nextLine +
            "https://automaticpoker.com/how-to-play-poker/texas-holdem-basic-rules/" + nextLine +
            "https://automaticpoker.com/poker-basics/burn-and-turn-rules/" + nextLine +
            "https://automaticpoker.com/poker-basics/texas-holdem-order-of-play/" + tripleNextLine +
            "READYCHECK" + doubleNextLine +
            "pregame:" + nextLine +
            "+- create nl holdem game by type: +STP, -MTP, -cash" + nextLine +
            "+- game registration mechanics: +reg, +unreg, +invite, +-queue, +-clearstate" + nextLine +
            "+- card deal mechanics: +draw, +-preflop, +-turn, +-river, -showdown, +-burnwhendealing" + doubleNextLine +
            "game:" + nextLine +
            "+- game state mechanics: +countBlindSize, +countBlindId, -substractfromplayerbalance, -addtopot +-removeBankrupt, +-finishGame " + nextLine + nextLine +
            "+- player hand ranking: -pairs -2pairs -triple -fullhouse -quad -street +flash -streetflash +-highestkicker" + nextLine +
            "- player turn phases mechanics: -check -call +-bet -raise -reraise -allin -pmMoveScheduler -afk" + nextLine +
            "- clearing phase mechanics: -calculateCombinations -splitPotReward" + nextLine +
            "misc:" + nextLine +
            "+- gui: +mudMode, +-msgEdit, -commands, -inlineCommands, -markupMessages" + nextLine +
            "-stats: -processUsersStat, -provideUserStat, -processTournamentStat, -provideTournamentStat" + tripleNextLine +
            "TL;DR: currently unplayable after cards dealt";


    boolean tradePreflopDone = false;
    boolean tradeFlopDone = false;
    boolean tradeTurnDone = false;
    boolean tradeRiverDone = false;
//    boolean isTradePreflopDone = false;
//    boolean isTradeFlopDone = false;
//    boolean isTradeRiverDone = false;

    boolean registrationStarted = false;
    boolean preGameStarted = false;
    boolean gameStarted = false;
//    boolean tradeDone = false;

    //debug flags
    boolean rollVisibility = true;
    boolean dealVisibility = false;
    int dealNumber;


    //tournament settings
    int startSmallBlindSize = 250;
    int chips = 20000;
    boolean sendToPlayerCheckOnReg = true;
    int cardsCount = 2; // holdem


    @Override
    public void onRegister() {
        try {

            message.setChatId(playingRoomId);
            message.setText(commands);
            execute(message);
            System.out.println("onRegister ok: " + message.toString());
            sendSticker();
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
    

    public void sendToPlayingRoom(SendMessage message, String text) {
        try {
            //String currentId = message.getChatId();
            message.setChatId(playingRoomId);
            message.setText(text);
            execute(message);
            Thread.sleep(2000);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
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
            Thread.sleep(2000);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean sendToPlayer(SendMessage message, String userId, String text) {
        try {
            message.setChatId(userId);
            message.setText(text);
            execute(message);
            //System.out.println("ok: " + message);
            Thread.sleep(2000);
            return true;
        } catch (TelegramApiException | InterruptedException e) {
            //e.printStackTrace();
            System.out.println("exception: " + e.getMessage() + nextLine + "message: " + message);
            return false;
        }
    }

    public Integer sendToPlayingRoomAndGetMessageId(SendMessage message, String text) {
        int messageId = -1;
        try {
            message.setChatId(playingRoomId);
            message.setText(text);
            messageId = execute(message).getMessageId();
            Thread.sleep(3000);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message);
        }
        return messageId;
    }

    public void sendBack(SendMessage message, String userId, String text) {
        try {
            message.setChatId(userId);
            message.setText(text);
            execute(message);
            //System.out.println("ok: " + message);
            Thread.sleep(3000);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: " + message);
        }


    }

    public void editMessage(String text, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        try {
            msg.setChatId(playingRoomId);
            msg.setMessageId(messageId);
            msg.setText(text);
            execute(msg);
            Thread.sleep(3000);
        } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("onUpdateReceived inited");
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        //System.out.println(update.toString());
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("im in if (update.hasMessage() && update.getMessage().hasText()) {");
            //String userName, userId;
            userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            userId = String.valueOf(update.getMessage().getFrom().getId());


            if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {
                System.out.println("im in if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {");


                boolean tournamentReady = false;
                while (!tournamentReady && gameStarted) {
                    boolean dealReady = false;
                    boolean tradeReady = false;
                    boolean clearingReady = false;
                    boolean tableReady = false;

                    sendToPlayingRoom(message, "deal order old: " + nextLine + PlayerUtils.getUserRegQueue(playersQueue));
                    ArrayList[] players;// = new ArrayList[playersQueue.length];
                    players = PlayerUtils.shiftPlayerOrder(playersQueue, buttonId);
                    //int playersCount = players.length;
                    sendToPlayingRoom(message, "deal order: " + nextLine + PlayerUtils.getUserRegQueue(players));

                    //editMessage(new StringBuilder().append("deal number: ").append(dealNumber).append(nextLine).toString(), introTextId);

                    //game start
                    tradeReady = true;


                    while (tradeReady) { //round start
                        dealNumber = dealNumber + 1;
                        boolean preflopPhase = true, flopPhase = false, turnPhase = false, riverPhase = false, showdown = false, tradeDone = false;
                        smallBlindSize = ((dealNumber / 5) + 1) * startSmallBlindSize;
                        bigBlindSize = smallBlindSize * 2;
                        cardsCount = 2;


                        String roundAnnounceString = new StringBuilder().append("deal number: ").append(dealNumber).append(nextLine).append("small blind: ").append(smallBlindSize).append(nextLine).append("big blind: ").append(bigBlindSize).toString();


                        clearingReady = false;
                        tableReady = false;
                        while (!tableReady) {
                            List<?> deck = initializeDeck();

                            deck = shuffleDeck(deck);
                            Integer dealMessageId = sendToPlayingRoomAndGetMessageId(message, "*Dealer shuffling deck*");
                            ArrayList[][] playerCards;// = new ArrayList[players.length][cardsCount];
                            playerCards = Draw.dealCards(players.length, cardsCount, deck);
                            text = "";
                            currentPlayerHandText = "";
                            allHandsText = "";
                            int currentPot = 0;
                            //boolean isSidePotExist = false;

                            String tableCards; // = new String();


                            if (players.length == 2) {
                                smallBlindId = 0;
                                bigBlindId = smallBlindId + 1;


                                boolean phaseBets = true;
                                preflopPhase = true;

                                while (preflopPhase) {
                                    for (int y = 0; y < players.length; y++) {
                                        if (y < 2) {
                                            players[y].set(3, Integer.parseInt(players[y].get(3).toString()) - smallBlindSize * (y + 1)); //current chips at player


                                            players[y].set(9, Integer.parseInt(players[y].get(9).toString()) + smallBlindSize * (y + 1)); // current bet at player
                                            players[y].set(9, Integer.parseInt(players[y].get(9).toString()) + smallBlindSize * (y + 1)); // current bet at player
                                            currentPot = Integer.parseInt((players[y].get(9).toString()) + smallBlindSize * (y + 1));
                                        }
                                        String hand = "";
                                        for (int i = 0; i < cardsCount; i++) {
                                            hand = hand + playerCards[y][i].get(0);
                                        }
                                        currentPlayerHandText = "player " + players[y].get(1) + nextLine + " hand: " + hand + nextLine;

                                        sendToPlayer(message, players[y].get(0).toString(), "dealNumber " + dealNumber + nextLine + "Your cards: " + currentPlayerHandText);
                                        allHandsText = allHandsText + currentPlayerHandText;
                                    }


                                    if (dealVisibility) {
                                        editMessage("*Dealer shuffling deck*" + " dealnumber: " + dealNumber, dealMessageId);
                                        editMessage("*Dealer shuffling deck*" + " dealnumber: " + dealNumber + doubleNextLine + allHandsText, dealMessageId);
                                        //editMessage("*Dealer shuffling deck*" + " dealnumber: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "Deck info: " + deck, dealMessageId);
                                    }
                                    editMessage(roundAnnounceString + tripleNextLine + "current move @ player order: " + nextLine + players[0].get(1), dealMessageId);


                                    // TODO() broken logic, check tradeready goes here
                                    int moveCount = 0;
                                    // while (moveCount <= moveCount*2 ||moveCount <3) { //userId =
                                    //if (update.hasMessage() && update.getMessage().hasText() && phaseBets) {
                                    if (String.valueOf(update.getMessage().getFrom().getId()).equals(players[moveCount].get(0))) {
                                        System.out.println("movecount: " + moveCount);
                                        sendToPlayingRoom(message, "userId " + update.getMessage().getFrom().getId() + " matched " + players[moveCount % players.length].get(0));

                                        String extractCommand, arg;
                                        //int extractNumber = 0;
                                        extractCommand = update.getMessage().getText().replaceAll(commandRegEx, "$1");
//
                                        //try {
                                        extractNumber = Integer.parseInt(update.getMessage().getText().replaceAll(argRegEx, "$2"));
                                        // } catch (NumberFormatException e) {
                                        //e.printStackTrace();
                                        //   }


//                                        phaseBets = false;


                                        switch (extractCommand.toLowerCase()) {
                                            case "check" -> {
                                                sendToPlayingRoom(message, "preflopPhase && phaseBets case check" + extractNumber);
                                                moveCount++;
//                                                    break;
                                            }
                                            case "call" -> {
                                                sendToPlayingRoom(message, "preflopPhase && phaseBets case call" + extractNumber);
                                                moveCount++;
//                                                    break;
                                            }
                                            case "bet", "raise" -> {
                                                sendToPlayingRoom(message, "preflopPhase && phaseBetscase bet" + extractNumber);
                                                moveCount++;
//                                                    break;
                                            }
                                            case "allin" -> {
                                                sendToPlayingRoom(message, "preflopPhase && phaseBets case allin" + extractNumber);
                                                moveCount++;
//                                                    break;
                                            }
                                            case "fold", "pass" -> {
                                                sendToPlayingRoom(message, "preflopPhase && phaseBets case fold" + extractNumber);
                                                moveCount++;
//                                                    break;
                                            }

                                            default -> {
                                                System.out.println("checkBetsEqual = " + PlayerUtils.checkBetsEqual(players));
                                                sendToPlayingRoom(message, "msgid " + update.getMessage().getMessageId() + nextLine + "userId: " + userId + nextLine + "getmessageuserid: " + update.getMessage().getFrom().getId() + nextLine + "players[smallBlindId].get(0) = " + players[smallBlindId].get(0));
                                            }
                                        }


                                        sendToPlayingRoom(message, "userId " + update.getMessage().getFrom().getId() + " not matched " + players[smallBlindId].get(0));

                                    } else {
                                        sendToPlayingRoom(message, "expecting move from player" + players[smallBlindId].get(0) + " (" + players[smallBlindId].get(1) + ")" + nextLine);
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                        }
                                        break;
                                    }

                                    if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {
                                        tradePreflopDone = true;
                                        tradeFlopDone = true;
                                        tradeTurnDone = true;
                                        tradeRiverDone = true;

                                        flopPhase = true;
                                        //currentPot = ; //
                                    }


                                    // }

                                    while (flopPhase) {


                                        turnPhase = true;
                                        String flopCards = DeckUtils.phaseFlop(maxPlayers, cardsCount, deck);
                                        tableCards = flopCards;
                                        editMessage("*Dealer shuffling deck*   deal number: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "flop phase: " + nextLine + tableCards, dealMessageId);

                                        while (turnPhase) {
                                            riverPhase = true;
                                            String turnCards = flopCards + DeckUtils.phaseTurn(maxPlayers, cardsCount, deck);
                                            tableCards = turnCards;
                                            editMessage("*Dealer shuffling deck*   deal number: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "turn phase: " + nextLine + tableCards, dealMessageId);


                                            while (riverPhase) {

                                                String riverCards = turnCards + DeckUtils.phaseRiver(maxPlayers, cardsCount, deck);
                                                tableCards = riverCards;
                                                editMessage("*Dealer shuffling deck*   deal number: " + dealNumber + doubleNextLine + allHandsText + doubleNextLine + "river phase: " + nextLine + tableCards + doubleNextLine + "deck: " + deck, dealMessageId);

                                                //when bets equal and betsquantity>1 showdown=true;
                                                showdown = true;
                                                while (showdown) {
                                                    System.out.println("im in showdown");
                                                    try {
                                                        Thread.sleep(2000);
                                                    } catch (InterruptedException e) {
                                                    }


                                                }

                                            }

                                        }


                                        while (!clearingReady) { //distribute chips
                                            System.out.println();
                                            // countCombinations
                                            // if 0 tokens, remove player from game, stat his place
                                            // if 1 player left, finish game
                                            //find min stack
                                            //if minstack > 0 minimum 2 times, set Tradeready false
                                            //reinit players, cleanup playerhands, go to tradeready
                                            players = PlayerUtils.checkLastChips(players, bigBlindSize); //playersCount;
                                            ArrayList[] playersOld = players;
                                            players = new ArrayList[playersOld.length];
                                            players = PlayerUtils.shiftPlayerOrder(playersOld, 1);

                                            // add reward;
                                            clearingReady = true;

                                            if (players.length == 1) {
                                                //finish game and count prize
                                                //break;
                                            }

                                            return;
                                        }

//                                        tradeDone = false;
//                                        preflopPhase = false;
//                                        flopPhase = false;
//                                        riverPhase = false;
//                                        showdown = false;
//                                        tableReady = true;
//                                        tradeReady = false;

                                        dealNumber++;
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                        }
                                        ;
                                        return;
                                        //break;


                                    }
                                }
                            }
                        }
                    }
                }
            }


        }

        if (update.hasMessage() && update.getMessage().hasText() && registrationStarted) {
            //message.setChatId(playingRoomId);
            //message = update.getMessage().getText(); String msgmessage = update.getMessage().getText().toString();
            //String messageSourceId = message.getChatId();
            userName = new StringBuilder()
                    .append(update.getMessage().getFrom().getFirstName())
                    .append(" ")
                    .append(update.getMessage().getFrom().getLastName())
                    .append(" ")
                    .append("(" + update.getMessage().getFrom().getUserName() + ")").toString();
            userId = String.valueOf(update.getMessage().getFrom().getId());
            String extractCommand, arg;

            extractCommand = update.getMessage().getText().replaceAll(commandRegEx, "$1");
            if (extractCommand.length() < update.getMessage().getText().replaceAll(commandRegEx, "$1").toString().length()) {
                try {
                    arg = update.getMessage().getText().replaceAll(argRegEx, "$2");
                    System.out.println("arg = " + arg + nextLine + "extractCommand = " + extractCommand);
                    extractNumber = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    extractNumber = 0;
                }
            }
        }
        //registrationStarted = false;

        if (update.hasMessage() && update.getMessage().hasText() && preGameStarted) {
            int tryCount = 0;
            int rollWinnerPlayerId;

            List<?> deck = initializeDeck();

            deck = shuffleDeck(deck);
            tryCount++;
            Integer rollMessageId = sendToPlayingRoomAndGetMessageId(message, "*Dealer shuffling deck*");
            int cardsCount = 1;
            ArrayList[][] playerCards = Draw.dealCards(maxPlayers, cardsCount, deck);
            text = "";
            currentPlayerHandText = "";
            allHandsText = "";

            for (int y = 0; y < maxPlayers; y++) {
                StringBuilder hand = new StringBuilder();
                for (int i = 0; i < cardsCount; i++) {
                    hand.append(playerCards[y][i].get(0));
                }
                currentPlayerHandText = playersQueue[y].get(1).toString() + ": " + hand + nextLine;

                sendToPlayer(message, playersQueue[y].get(0).toString(), "pregame roll number " + tryCount + nextLine + "Your cards: " + currentPlayerHandText);
                allHandsText += currentPlayerHandText;
            }


            if (rollVisibility) {
                editMessage("*Dealer shuffling deck*" + doubleNextLine + allHandsText, rollMessageId);
            }

            rollWinnerPlayerId = DeckUtils.rankRoll(maxPlayers, playerCards);


            if (rollWinnerPlayerId == -1) {
                editMessage(
                        "*Dealer shuffling deck*" +
                                doubleNextLine +
                                allHandsText +
                                doubleNextLine +
                                "We've equal highest, redraw (trycount " +
                                tryCount + ")" +
                                doubleNextLine +
                                "Deck info: " +
                                deck,
                        rollMessageId
                );
                gameStarted = false;
            } else {
                editMessage(
                        "*Dealer shuffling deck*" +
                                doubleNextLine +
                                allHandsText +
                                doubleNextLine +
                                playersQueue[rollWinnerPlayerId].get(1).toString() +
                                " starting on the button" +
                                tripleNextLine +
                                "Deck info: " +
                                deck,
                        rollMessageId
                );
                buttonId = rollWinnerPlayerId;
                gameStarted = true;
                return;
            }
            //} return;

        }

        //}
        //trade, when bets equal and betsquantity>1
        //if (!gameStarted) {
        if (update.hasMessage() && update.getMessage().hasText() && !gameStarted && !preGameStarted) { //&& !preGameStarted && !gameStarted) {

            System.out.println("im in if (update.hasMessage() && update.getMessage().hasText() && preGameStarted && gameStarted) {");
            if (!preGameStarted) {
                String userId;
                System.out.println("else if (!preGameStarted) {");
                userId = String.valueOf(update.getMessage().getFrom().getId());
                String extractCommand, arg;

                extractCommand = update.getMessage().getText().replaceAll(commandRegEx, "$1");
                if (extractCommand.length() < update.getMessage().getText().length()) {

                    try {
                        arg = update.getMessage().getText().replaceAll(argRegEx, "$2");
                        System.out.println("arg = " + arg.toString() + nextLine + "extractCommand = " + extractCommand);
                        extractNumber = Integer.parseInt(arg.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();

                    }
                }

                switch (extractCommand.toLowerCase()) {
                    case "commands":
                        sendBack(message, userId, commands);
                        break;

                    case "rules", "about":
                        sendBack(message, userId, rulesText);
                        break;

                    case "reg":
                        if (registrationStarted) { // TODO() split prereg and reg switch

                            int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);
                            if (checkAlreadyReg < 0) {


                                boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                if (!checkQueueFull || checkForUnreg > -1) {
                                    if (sendToPlayerCheckOnReg && !sendToPlayer(message, userId, "Register success.")) {
                                        sendToPlayingRoom(message, "Register failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                    } else {
                                        playersQueue = PlayerUtils.addPlayerToQueue(userId, userName, playersQueue, playingRoomId, registeredPlayers, chips, checkQueueFull, checkForUnreg);


                                        registeredPlayers++;
                                        //sendBack(message, "Register success. Current queue: " + PlayerUtils.getUserReg(playersQueue, registeredPlayers, userId));
                                        sendToPlayingRoom(message, userName + " registered to the game! \n" + (maxPlayers - registeredPlayers) + " to go");

                                        if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                            preGameStarted = true;

                                            sendToPlayingRoom(message, "Players registered! Lets roll!");
                                            break;
                                        }

                                    }
                                } else
                                    sendBack(message, userId, "Register failed: checkForUnreg - " + checkForUnreg + ", checkForUnreg - " + checkForUnreg);
                            } else sendBack(message, userId, "Register failed or limits");
                        } else sendBack(message, userId, "Registration is not open");

                    case "invite":
                        if (registrationStarted) {
                            if (extractNumber >= 10000000 && extractNumber <= 2121212100 && extractNumber != 2052704458) {
                                String inviteUserId = String.valueOf(extractNumber);


                                int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(inviteUserId, playersQueue, registeredPlayers);
                                if (checkAlreadyReg < 0) {

                                    boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                    int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                    if (!checkQueueFull || checkForUnreg > -1) {

                                        if (sendToPlayerCheckOnReg && !sendToPlayer(message, inviteUserId, "You have been invited to play some poker")) {
                                            sendToPlayingRoom(message, "Invite failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                        } else {
                                            playersQueue = PlayerUtils.addPlayerToQueue(inviteUserId, "Player" + inviteUserId, playersQueue, playingRoomId, registeredPlayers, chips, checkQueueFull, checkForUnreg);

                                            // TODO() count invites seperately
                                            registeredPlayers++;
                                            sendToPlayer(message, userId, "Invite success. Current queue: " + PlayerUtils.getUserReg(playersQueue, registeredPlayers, userId));
                                            sendToPlayingRoom(message, inviteUserId + " invited to the game! \n" + (maxPlayers - registeredPlayers) + " to go");

                                            if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                                preGameStarted = true;
                                                //registrationStarted = false;
                                                sendToPlayingRoom(message, "Players registered! Lets roll!");
                                                break;
                                            } else {
                                                preGameStarted = false;
                                            }
                                        }
                                    }
                                } else
                                    sendToPlayer(message, userId, "Invite failed: checkReg - " + checkAlreadyReg + ", checkLimits - " + PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers));
                            } else sendToPlayer(message, userId, "userId " + extractNumber + "do not match format.");
                        } else sendBack(message, userId, "Registration is not open");


//                case "clearstate":
//                    if (userId.equals("193873212")) {
//                        sendToPlayer(message, userId, "clearstate AFFORMINAVE");
//                        sendToPlayingRoom(message, "clearstate command recieved, trying..");
//
//                        playersQueue = null;
//                        registeredPlayers = 0;
//                        registrationStarted = false;
//                        preGameStarted = false;
//                        gameStarted = false;
//                        exe.shutdown();
//                        // TODO () clean all
//                    } else {
//                        sendToPlayer(message, userId, "clearstate AFFORMINAVE");
//                    }
//                    break;
//
//                    case "unreg":
//                        if (registrationStarted) {
//
//                            if (PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers) >= 0) {
//                                playersQueue = PlayerUtils.userUnreg(playersQueue, userId);
//                                registeredPlayers--;
//                                sendToPlayer(message, userId, "Unregistered");
//                            }
//
//                        } else {
//                            sendBack(message, "Registration is not open");
//                        }
//                        break; //
// clear and unreg

                    case "queue":
                        if (registrationStarted) {
                            sendBack(message, userId, "`  queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                            break;
                        } else {
                            sendBack(message, userId, "Registration is not open");
                        }
                        return;

                    case "poker":
                        playersQueue = null;
                        if (extractNumber >= 1 && extractNumber <= 9) {
                            maxPlayers = extractNumber;
                        } else {
                            maxPlayers = 9;
                        }
                        registeredPlayers = 0;
                        sendToPlayingRoom(message, userName + " initiated tournament! Registration free and open. Game Texas NL Holdem." + doubleNextLine + "playersCount: " + maxPlayers + nextLine + "stp config " + nextLine + "smallBlind " + startSmallBlindSize + nextLine + "startChips " + chips + nextLine + "blindIncrease x2 every 5 rounds");
                        registrationStarted = true;
                        playersQueue = new ArrayList[maxPlayers];
                        return;

                    default:
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                        }
                        //break;


                }
            }
        }
        System.out.println("passed 2 switches");


    }
}