package org.pokedgram;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.core.util.Loader.getResourceBySelfClassLoader;
import static org.pokedgram.DeckUtils.initializeDeck;
import static org.pokedgram.DeckUtils.shuffleDeck;

//TODO() rename to PokedgramDealerBot
public class PokedgramBot extends TelegramLongPollingBot {

    Path PARAMS_PATH, STICKERS_PATH;
    List<String> PARAMS_LIST, STICKERS_LIST;
    {
        try {
            PARAMS_PATH   = Paths.get(getResourceBySelfClassLoader("config.tg").toURI()); // added to .gitignore. 1 line - token, 2nd - roomId, 3rd - room invite link
            STICKERS_PATH = Paths.get(getResourceBySelfClassLoader("stickerpack.list").toURI());
            PARAMS_LIST   = Files.readAllLines(PARAMS_PATH, StandardCharsets.UTF_8);
            STICKERS_LIST = Files.readAllLines(STICKERS_PATH, StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
    final String PLAYING_ROOM_ID = PARAMS_LIST.get(1);
    final String PLAYING_ROOM_LINK = PARAMS_LIST.get(2);
    final String NEXT_LINE = "\n";
    final String DOUBLE_NEXTLINE = NEXT_LINE + NEXT_LINE;
    final String TRIPLE_NEXT_LINE = NEXT_LINE + NEXT_LINE + NEXT_LINE;
    final String COMMANDS_TEXT; {
        COMMANDS_TEXT =
                MessageFormat.format("COMMANDS {0}   /about - about this bot  (alias command \"rules\"){1}   /commands - this list{2}   /poker <number> - start game registration with <number> of players{3}   /reg, /unreg - register to the game, or forfeit{4}   /invite <userId> - invite tg user to join game (success only if user communicated with bot in personal messages earlier){5}   /queue - get registered members list{6}To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat {7}. To set up in 1 step input command about. ", DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, DOUBLE_NEXTLINE, PLAYING_ROOM_LINK);
    }
    final String RULES_TEXT; {
        RULES_TEXT = MessageFormat.format("I''m NL holdem dealer bot. {0}To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat {1} (without ''/'' before command){2}To start game: {3}1. type command \"poker [2-9]\" e.g. \"poker 3\" directly into playing room chat group, where arg = number of sits(=players) in the game. accepted values are >=2 && <=9 {4}2. type command \"reg\" to register to the game. (Also you can send invite to telegram user via \"invite\" command; Also player can unregister by using \"unreg\" command){5}When registered users will take all the sits, game starts.{6}Game scenario:{7}1. Dealer unpacks and shuffles deck, then deals 1 card to each player (Cards revealed). Deal order determined by players registration order. {8}2. The player with highest card starts on button, the players after him start on blinds. (value order on roll phase currently is 2<3<4<5<6<7<8<9<10<J<Q<K<A>2; if more than 1 player got same highest value, dealer shuffles deck and deal repeats. ){9}3. The game starts, and all the rules match TEXAS NL HOLDEM : {10}Cards are dealt in private message from bot. Player on his turn have to choose one of options (fold|check|call|bet|reraise). Player types his decision on his turn in playing room. Additionally, there is an option to schedule decision @ bot pm.{11}useless links:{12}https://automaticpoker.com/how-to-play-poker/texas-holdem-basic-rules/{13}https://automaticpoker.com/poker-basics/burn-and-turn-rules/{14}https://automaticpoker.com/poker-basics/texas-holdem-order-of-play/{15}READYCHECK{16}pregame:{17}+- create nl holdem game by type: +STP, -MTP, -cash{18}+- game registration mechanics: +reg, +-unreg, +invite, +queue, -clearstate{19}+- card deal mechanics: +draw, +-preflop, +-turn, +-river, +-showdown, +-burnwhendealing{20}game:{21}+- game state mechanics: +countBlindSize, +countBlindId, -substractfromplayerbalance, -addtopot +-removeBankrupt, +-finishGame {22}{23}+- player hand ranking: -pairs -2pairs -triple -fullhouse -quad -street +flash -streetflash +-highestkicker{24}- player turn phases mechanics: -check -call +-bet -raise -reraise -allin -pmMoveScheduler -afk{25}- clearing phase mechanics: -calculateCombinations -splitPotReward{26}misc:{27}+- gui: +mudMode, +msgEdit, +commands, +-inlineCommands, +-markupMessages{28}-stats: -processUsersStat, -provideUserStat, -processTournamentStat, -provideTournamentStat{29}TL;DR: currently unplayable after cards dealt", DOUBLE_NEXTLINE, PLAYING_ROOM_LINK, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, TRIPLE_NEXT_LINE, TRIPLE_NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, TRIPLE_NEXT_LINE, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, TRIPLE_NEXT_LINE);
    }
    final String COMMAND_REGEXP = "^(.[a-zA-Z@_]*) ([0-9]*)(.*)$";
    final String ARGUMENT_REGEXP = "^.([a-zA-Z@_ ]*)(.*)$";
    final boolean ROLL_VISIBILITY = true;
    final boolean DEAL_VISIBILITY = false;
    final boolean CHECK_PLAYERS_INTERACTION = true;
    final int START_CHIPS = 20000;
    final int HAND_CARDS_COUNT = 2; // holdem
    final int SLEEP_MS = 1000;
    final int BLIND_INCREASE_RATE_DEALS = 5;
    final int START_SMALL_BLIND_SIZE = 250;


    ArrayList[] playersQueue;
    int buttonId = -1;
    int registeredPlayers = 0;
    int extractNumber;
    int maxPlayers;
    int bigBlindId = -1;
    int smallBlindId = -1;
    int smallBlindSize, bigBlindSize;
    int dealNumber;
    int minBetSize = 0;


    boolean registrationStarted = false;
    boolean preGameStarted = false;
    boolean gameStarted = false;
    boolean tradePreflopDone = false;
    boolean tradeFlopDone = false;
    boolean tradeTurnDone = false;
    boolean tradeRiverDone = false;
    //    boolean isTradePreflopDone = false;
    //    boolean isTradeFlopDone = false;
    //    boolean isTradeRiverDone = false;

    SendMessage message = new SendMessage();
    String text = "", currentPlayerHandText = "", allHandsText = "", userIdMessage = "", userIdCallback = "";;
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    InlineKeyboardButton buttonFold = new InlineKeyboardButton(),
            buttonCheck = new InlineKeyboardButton(),
            buttonCall = new InlineKeyboardButton(),
            buttonBet = new InlineKeyboardButton(),
            buttonBetSize1 = new InlineKeyboardButton(),
            buttonBetSize2 = new InlineKeyboardButton(),
            buttonBetSize3 = new InlineKeyboardButton(),
            buttonBetSize4 = new InlineKeyboardButton(),
            buttonBetSize5 = new InlineKeyboardButton(),
            makeMoveButton = new InlineKeyboardButton();


    @Override
    public void onRegister() {
        try {

            message.setChatId(PLAYING_ROOM_ID);
            message.setText(COMMANDS_TEXT);
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

        return PARAMS_LIST.get(3);
    }

    @Override
    public String getBotToken() {

        return PARAMS_LIST.get(0);

    }

    public void sendSticker() {
        try {
            String currentId = message.getChatId();
            message.setChatId(PLAYING_ROOM_ID);
            SendSticker sendSticker = new SendSticker();
            sendSticker.setChatId(PLAYING_ROOM_ID);
            sendSticker.setSticker(new InputFile(STICKERS_LIST.get(0)));
            execute(sendSticker);
            try {
                message.setChatId(currentId);
            } catch (NullPointerException e) {
                System.out.println("exception: " + message);
            }
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void sendToPlayingRoom(String text) {
        try {
            message = new SendMessage();
            message.setChatId(PLAYING_ROOM_ID);
            message.setText(text);
            execute(message);
            Thread.sleep(SLEEP_MS);

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
            Thread.sleep(SLEEP_MS);
            return true;
            //System.out.println("ok: " + message);
        } catch (TelegramApiException | InterruptedException e) {
            //e.printStackTrace();
            System.out.println("exception: " + e.getMessage() + NEXT_LINE + "message: " + message);
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
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: " + message);
        }


    }
    public Integer sendToPlayingRoomAndGetMessageId(String text) {
        message = new SendMessage();
        int messageId = -1;
        try {
            message.setChatId(PLAYING_ROOM_ID);
            message.setText(text);
            messageId = execute(message).getMessageId();
            Thread.sleep(SLEEP_MS);
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
            message.setChatId(PLAYING_ROOM_ID);
            message.setText(text);
            message.setReplyMarkup(inlineKeyboardMarkup);

            messageId = execute(message).getMessageId();
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message);
        }
        return messageId;
    }
    public void editMessage(String text, Integer messageId) {


        EditMessageText msg = new EditMessageText();
        try {
            msg.setChatId(PLAYING_ROOM_ID);
            msg.setMessageId(messageId);
            msg.setText(text);
            execute(msg);
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg);
        }
    }
    public void editMessageWithButtons(String text, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        try {
            msg.setChatId(PLAYING_ROOM_ID);
            msg.setMessageId(messageId);
            msg.setText(text);
            msg.setReplyMarkup(inlineKeyboardMarkup);
            execute(msg);
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg);
        }
    }


    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()  && gameStarted ) {
            userIdMessage = update.getMessage().getFrom().getId().toString();
            userIdCallback = "0";
        } else {
            userIdMessage = "0";
        }

        if (update.hasCallbackQuery()) {

            userIdCallback = update.getCallbackQuery().getFrom().getId().toString();
            userIdMessage = "0";
//            System.out.println("im in update.hasCallbackQuery() && gameStarted; userIdCallback = " + userIdCallback);
//            try {
//                SendMessage message = new SendMessage();
//                message.setChatId(PLAYING_ROOM_ID);
//                message.setText(update.getCallbackQuery().getData() + NEXT_LINE + userIdCallback);
//                //message.setText(update.getCallbackQuery().getFrom().getId().toString());
//                //dfgdfg.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
//                execute(message);
//                Thread.sleep(SLEEP_MS);
//            } catch (TelegramApiException |  InterruptedException e) {
//                e.printStackTrace();
//            }
        }
//        else {
//            userIdCallback = "0";
//        }

        {

            minBetSize = (((dealNumber / BLIND_INCREASE_RATE_DEALS) + 1) * START_SMALL_BLIND_SIZE) * 2;


            buttonCheck.setText("check");
            buttonFold.setText("fold");
            buttonCall.setText("call");
            buttonBet.setText("bet");
            buttonCheck.setCallbackData("check pressed by - usrid: " + userIdMessage + ", cbid: " + userIdCallback);
            buttonFold.setCallbackData("fold pressed by - usrid: " + userIdMessage + ", cbid: " + userIdCallback);
            buttonCall.setCallbackData("call pressed by - usrid: " + userIdMessage + ", cbid: " + userIdCallback);
            buttonBet.setCallbackData("bet pressed by - usrid: " + userIdMessage + ", cbid: " + userIdCallback);
            buttonBetSize1.setText(String.valueOf(minBetSize));
            buttonBetSize2.setText(String.valueOf(minBetSize * 2));
            buttonBetSize3.setText(String.valueOf(minBetSize * 3));
            buttonBetSize4.setText(String.valueOf(minBetSize * 4));
            buttonBetSize5.setText("\uD83D\uDD34 allin");

            buttonBetSize1.setCallbackData(String.valueOf(minBetSize));
            buttonBetSize2.setCallbackData(String.valueOf(minBetSize * 2));
            buttonBetSize3.setCallbackData(String.valueOf(minBetSize * 3));
            buttonBetSize4.setCallbackData(String.valueOf(minBetSize * 4));
            buttonBetSize5.setCallbackData("\uD83D\uDD34 allin");
            makeMoveButton.setText("make move");
            makeMoveButton.setCallbackData("make move");
            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();


            keyboardButtonsRow1.add(buttonFold);
            keyboardButtonsRow1.add(buttonCheck);
            keyboardButtonsRow1.add(buttonCall);
            keyboardButtonsRow1.add(buttonBet);
            keyboardButtonsRow2.add(buttonBetSize1);
            keyboardButtonsRow2.add(buttonBetSize2);
            keyboardButtonsRow2.add(buttonBetSize3);
            keyboardButtonsRow2.add(buttonBetSize4);
            keyboardButtonsRow2.add(buttonBetSize5);
            keyboardButtonsRow3.add(makeMoveButton);


            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow1);
            rowList.add(keyboardButtonsRow2);
            rowList.add(keyboardButtonsRow3);
            inlineKeyboardMarkup.setKeyboard(rowList);
        } //setup inline buttons for bets phase
        // ToDO() refactor
        System.out.println("onUpdateReceived inited, usr1 = " + userIdMessage + ", usr2 = " + userIdCallback);


        if ((update.hasMessage() && update.getMessage().hasText())) {
            String userName, userId;
            userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            userId   = String.valueOf(update.getMessage().getFrom().getId());
            System.out.println("got update with text, from userName" + userName + "(" + userId + ")");

            if (!gameStarted) {
                if (!preGameStarted) {

                    userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + " " + "(" + update.getMessage().getFrom().getUserName() + ")";
                    userId   = String.valueOf(update.getMessage().getFrom().getId());
                    String chatId = String.valueOf(update.getMessage().getChatId());

                        String extractCommand, arg;
                        extractCommand = update.getMessage().getText().replaceAll(COMMAND_REGEXP, "$1");

                        if (extractCommand.length() < update.getMessage().getText().length()) {
                            try {
                                arg = update.getMessage().getText().replaceAll(ARGUMENT_REGEXP, "$2");
                                System.out.println("arg = " + arg + NEXT_LINE + "extractCommand = " + extractCommand);
                                extractNumber = Integer.parseInt(arg);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                extractNumber = 0;
                            }

                        }


                        switch (extractCommand.toLowerCase()) {
                            case "commands", "/commands", "/commands@pokedgram_bot":
                                sendBack(chatId, COMMANDS_TEXT);
                                break;

                            case "rules", "/rules", "/rules@pokedgram_bot", "/about", "/about@pokedgram_bot":
                                sendBack(chatId, RULES_TEXT);
                                break;

                            case "reg", "/reg", "/reg@pokedgram_bot":
                                if (registrationStarted) { // TODO() split prereg and reg switch

                                    int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);
                                    if (checkAlreadyReg < 0) {


                                        boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                        int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                        if (!checkQueueFull || checkForUnreg > -1) {
                                            if (CHECK_PLAYERS_INTERACTION && !sendToPlayer(userId, "Register success.")) {
                                                sendToPlayingRoom("Register failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                            } else {
                                                playersQueue = PlayerUtils.addPlayerToQueue(userId, userName, playersQueue, PLAYING_ROOM_ID, registeredPlayers, START_CHIPS, checkQueueFull, checkForUnreg);
                                                registeredPlayers++;
                                                sendToPlayingRoom(userName + " registered to the game! " + (maxPlayers - registeredPlayers) + " to go");

                                                if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                                    preGameStarted = true;

                                                    sendToPlayingRoom("Players registered! Lets roll!");
                                                    break;
                                                }
                                                return;

                                            }
                                        } else
                                            sendBack(chatId, "Register failed: checkForUnreg - " + checkForUnreg + ", checkForUnreg - " + checkForUnreg);
                                    } else sendBack(chatId, "Register failed or limits");
                                } else sendBack(chatId, "Registration is not open");

                            case "invite", "/invite", "inv", "/inv", "/invite@pokedgram_bot", "invite@pokedgram_bot":
                                if (registrationStarted) {
                                    if (extractNumber >= 10000000 && extractNumber <= 2121212100 && extractNumber != 2052704458) {
                                        String inviteUserId = String.valueOf(extractNumber);
                                        userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();

                                        int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(inviteUserId, playersQueue, registeredPlayers);
                                        if (checkAlreadyReg < 0) {

                                            boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                            int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                            if (!checkQueueFull || checkForUnreg > -1) {

                                                if (CHECK_PLAYERS_INTERACTION && !sendToPlayer(inviteUserId, "You have been invited to play some poker")) {
                                                    sendToPlayingRoom("Invite failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                                } else {
                                                    playersQueue = PlayerUtils.addPlayerToQueue(inviteUserId, "Player" + inviteUserId, playersQueue, PLAYING_ROOM_ID, registeredPlayers, START_CHIPS, checkQueueFull, checkForUnreg);

                                                    // TODO() count invites seperately
                                                    registeredPlayers++;
                                                    //sendToPlayer(userId, "Invite success. Current queue: " + PlayerUtils.getUserReg(playersQueue, registeredPlayers, userId));
                                                    sendToPlayer(userId, "Invite success. Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                                                    sendToPlayingRoom(inviteUserId + " invited to the game! " + (maxPlayers - registeredPlayers) + " to go");

                                                    if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                                        preGameStarted = true;
                                                        //registrationStarted = false;
                                                        sendToPlayingRoom("Players registered! Lets roll!");
                                                        break;
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
                                sendToPlayingRoom("Tournament initiated! Registration free and open. Game Texas NL Holdem." + DOUBLE_NEXTLINE + "playersCount: " + maxPlayers + NEXT_LINE + "stp config " + NEXT_LINE + "smallBlind " + START_SMALL_BLIND_SIZE + NEXT_LINE + "startChips " + START_CHIPS + NEXT_LINE + "blindIncrease x2 every 5 rounds");
                                registrationStarted = true;
                                playersQueue = new ArrayList[ maxPlayers ];
                                return;

                            default:
                                return;


                        } // pregame menu switch

                } //before reg open

                if (preGameStarted) {
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
                        currentPlayerHandText = hand + NEXT_LINE;

                        sendToPlayer(playersQueue[ y ].get(0).toString(), "pregame roll number " + tryCount + NEXT_LINE + "Your cards: " + currentPlayerHandText);
                        allHandsText += playersQueue[ y ].get(1).toString() + currentPlayerHandText;
                    }


                    if (ROLL_VISIBILITY) {
                        editMessage("*Dealer shuffling deck*" + DOUBLE_NEXTLINE + allHandsText, rollMessageId);
                    } else {
                        editMessage("*Dealer shuffling deck*" + DOUBLE_NEXTLINE, rollMessageId);
                    }

                    rollWinnerPlayerId = DeckUtils.rankRoll(maxPlayers, playerCards);


                    if (rollWinnerPlayerId == -1) {
                        editMessage("*Dealer shuffling deck*" + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "We've equal highest, redraw (trycount " + tryCount + ")" + DOUBLE_NEXTLINE + "Deck info: " + deck, rollMessageId);
                        gameStarted = false;
                    } else {
                        editMessage("*Dealer shuffling deck*" + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + playersQueue[ rollWinnerPlayerId ].get(1).toString() + " starting on the button" + TRIPLE_NEXT_LINE + "Deck info: " + deck, rollMessageId);
                        buttonId    = rollWinnerPlayerId;
                        gameStarted = true;
                        return;
                        //return;
                        //TODO() delete pregame commands and add game commands promt
                        //TODO() create comfortable UX to make moves (inline/buttons/etc)
                    }
                    //} return;

                } //pregame started (reg finished)
            }
        }
        if (gameStarted) { // if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {

                System.out.println("if gamestarted == true");
                String dealInfoLast = "deal order last round: " + NEXT_LINE + PlayerUtils.getUserRegQueue(playersQueue);
                Integer dealOrderMsg = sendToPlayingRoomAndGetMessageId(dealInfoLast);
                ArrayList[] players = new ArrayList[ playersQueue.length ];
                players = playersQueue;
                players = PlayerUtils.shiftPlayerOrder(players, buttonId);
                int playersCount = players.length;
                //sendToPlayingRoom("deal order: " + nextLine + PlayerUtils.getUserRegQueue(players));

                boolean tournamentReady = false;
                while (!tournamentReady && gameStarted) {
                    //boolean dealReady = false;
                    boolean tradeReady = false;
                    boolean clearingReady = false;
                    boolean tableReady = false;
                    String dealInfoCurrent = "deal order current round: " + NEXT_LINE + PlayerUtils.getUserRegQueue(players);
                    editMessage(dealInfoLast + NEXT_LINE + dealInfoCurrent,dealOrderMsg);


                    //game start
                    tradeReady = true;


                    while (tradeReady) { //round start
                        dealNumber = dealNumber + 1;
                        //boolean preflopPhase = true, flopPhase = false, turnPhase = false, riverPhase = false, showdown = false, tradeDone = false;
                        boolean preflopPhase = true, flopPhase = false, turnPhase = false, riverPhase = false, showdown = false;//, tradeDone = false;
                        smallBlindSize = ((dealNumber / BLIND_INCREASE_RATE_DEALS) + 1) * START_SMALL_BLIND_SIZE;
                        bigBlindSize     = smallBlindSize * 2;
                        //HAND_CARDS_COUNT = 2;

                        if (players.length == 2) {
                            smallBlindId = 0;
                            bigBlindId   = smallBlindId + 1;
                        } else {
                            System.out.println("else players.lenght != 2");
                            break;
                        }

                        String roundAnnounceString =
                                "deal number: " + dealNumber + "." + NEXT_LINE +
                                "\uD83D\uDD35 small blind @ "  +  players[smallBlindId].get(1) + ". size: "+ smallBlindSize + NEXT_LINE  +
                                "\uD83D\uDFE1 big blind @ " + players[bigBlindId].get(1) + ". size: "+ bigBlindSize;
                        List<?> deck = initializeDeck();

                        deck = shuffleDeck(deck);
                        Integer dealMessageId = sendToPlayingRoomWithButtonsAndGetMessageId(roundAnnounceString + NEXT_LINE + "*Dealer shuffling deck*");

                        ArrayList[][] playerCards = new ArrayList[ playersCount ][ HAND_CARDS_COUNT ];

                        playerCards           = DeckUtils.dealCards(players.length, HAND_CARDS_COUNT, deck);
                        text                  = "";
                        currentPlayerHandText = "";
                        allHandsText          = "";

                        int moveCount = 0;
                        clearingReady = false;
                        tableReady    = false;

                            int currentPot = 0;
                            boolean isSidePotExist = false;

                            String tableCards;
                            //boolean phaseBets = true;

                            for (int y = 0; y < players.length; y++) {
                                if (y < 2) {
                                    players[ y ].set(3, String.valueOf(Integer.parseInt(players[ y ].get(3).toString()) - smallBlindSize * (y + 1))); //current chips at player


                                    players[ y ].set(9, String.valueOf(Integer.parseInt(players[ y ].get(9).toString()) + smallBlindSize * (y + 1))); // current bet at player
                                    currentPot = Integer.parseInt((players[ y ].get(9).toString()) + smallBlindSize * (y + 1));
                                }
                                StringBuilder hand = new StringBuilder();
                                for (int i = 0; i < HAND_CARDS_COUNT; i++) {
                                    hand.append(playerCards[ y ][ i ].get(0));
                                }
                                //currentPlayerHandText = "player " + players[ y ].get(1) + NEXT_LINE + " hand: " + hand + NEXT_LINE;
                                currentPlayerHandText = "player " + players[ y ].get(1) + NEXT_LINE + " hand: " + hand + NEXT_LINE;
                                sendToPlayer(players[ y ].get(0).toString(), "dealNumber " + dealNumber + NEXT_LINE + "Your cards: " + currentPlayerHandText);
                                allHandsText = allHandsText + currentPlayerHandText;
                            }

                            if (DEAL_VISIBILITY) {
                                editMessageWithButtons(roundAnnounceString + NEXT_LINE + "*Dealer shuffling deck*" + DOUBLE_NEXTLINE + "current move @ player order: " + NEXT_LINE + players[ moveCount ].get(1), dealMessageId);
                            } else {
                                editMessageWithButtons(roundAnnounceString + NEXT_LINE + "*Dealer shuffling deck*" + DOUBLE_NEXTLINE + "current move @ player order: " + NEXT_LINE + players[ moveCount ].get(1), dealMessageId);
                            }

                        while (!tableReady) {
                            //cards dealed blinds substracted, time to make moves
                            preflopPhase = true;
                            while (preflopPhase) {

                                // TODO() broken logic, check tradeready goes here

                                while (moveCount <= players.length && !PlayerUtils.checkBetsEqual(players)) { //userId =
                                    //if (update.hasMessage() && update.getMessage().hasText() && phaseBets) {

                                    System.out.println("movecount: " + moveCount);
                                    // if recieve msg
                                    if (update.hasCallbackQuery() && gameStarted) {

                                        //userIdCallback = update.getCallbackQuery().getFrom().getId().toString();
                                        if (userIdCallback.equals(players[ moveCount ].get(0).toString())) {
                                            sendToPlayingRoom("userId " + userIdCallback + " matched " + players[ moveCount ].get(0));
                                            try {
                                                SendMessage message = new SendMessage();
                                                message.setChatId(PLAYING_ROOM_ID);
                                                message.setText(players[ moveCount ].get(0) + "made move" + NEXT_LINE + update.getCallbackQuery().getData() + NEXT_LINE + userIdCallback);
                                                execute(message);
                                                moveCount++;
                                                Thread.sleep(SLEEP_MS);
                                            } catch (TelegramApiException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            sendToPlayingRoom("userId " + userIdCallback + " not matched " + players[ moveCount ].get(0) + NEXT_LINE + "expecting move from player" + players[ moveCount ].get(0) + " (" + players[ moveCount ].get(1) + ")" + NEXT_LINE);
                                            //userIdCallback = "0";
                                        }
                                        if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players) && update.hasCallbackQuery()) {
                                            tradePreflopDone = true;
                                            preflopPhase     = false;
                                            flopPhase        = true;
                                        } else {
                                            System.out.println("moveCount < players" + NEXT_LINE + " PlayerUtils.checkBetsEqual(players) \nmovecount: " + moveCount);

                                        }

                                        try {
                                            Thread.sleep(SLEEP_MS);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

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
                                    }
                                //tradeFlopDone    = true;
                                //tradeTurnDone    = true;
                                //tradeRiverDone   = true;
                                //phaseBets = false;

                                while (flopPhase) {

                                    turnPhase = true;
                                    String flopCards = DeckUtils.phaseFlop(maxPlayers, HAND_CARDS_COUNT, deck);
                                    tableCards = flopCards;
                                    editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "flop phase: " + NEXT_LINE + tableCards, dealMessageId);

                                    while (turnPhase) {
                                        riverPhase = true;
                                        String turnCards = flopCards + DeckUtils.phaseTurn(maxPlayers, HAND_CARDS_COUNT, deck);
                                        tableCards = turnCards;
                                        editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "turn phase: " + NEXT_LINE + tableCards, dealMessageId);


                                        while (riverPhase) {

                                            tableCards = turnCards + DeckUtils.phaseRiver(maxPlayers, HAND_CARDS_COUNT, deck);
                                            editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "river phase: " + NEXT_LINE + tableCards + DOUBLE_NEXTLINE + "deck: " + deck, dealMessageId);

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
                                                    players = PlayerUtils.removeZeroBalance(players);
                                                    players = PlayerUtils.shiftPlayerOrder(players, 1);
                                                    moveCount = 0;
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
                                                        Thread.sleep(SLEEP_MS);
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
                            }
                        }
                    }
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