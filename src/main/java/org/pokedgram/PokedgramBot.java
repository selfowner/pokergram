package org.pokedgram;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static ch.qos.logback.core.util.Loader.getResourceBySelfClassLoader;
//import static org.pokedgram.ChatUtils.*;
import static org.pokedgram.DeckUtils.initializeDeck;
import static org.pokedgram.DeckUtils.shuffleDeck;
import  static org.pokedgram.SuperStrings.*;
import static org.pokedgram.SuperStrings.ROLL_VISIBILITY;
//import static org.pokedgram.SuperStrings.*;


//TODO() rename to PokedgramDealerBot
public class PokedgramBot extends TelegramLongPollingBot {
    boolean clearingPhase;
    Integer pokerMessageId;
    Integer lastRegMessageId = 0;
    boolean prepareStuff = false;
    ArrayList[] players = new ArrayList[2];
    Boolean cardsDealPhase = false;
    Boolean dealStarted = false;
    Integer raiseCount;
    int currentDealMessageId;
    int minBetSize; // = smallBlindSize;
    int currentPlayerMoveChoice;
    int currentPlayerNumberChoice;
    String TOURNAMENT_INFO;
    String playersList = "";
    String nickName = "";
    String roundAnnounceString;
    boolean preflopPhase = true, flopPhase = false, turnPhase = false, riverPhase = false, showdownPhase = false;
    String flopCards = "", turnCards = "", riverCards = "";
    Path PARAMS_PATH, STICKERS_PATH;
    List<String> PARAMS_LIST;
    List<String> STICKERS_LIST;

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
    final String TOKEN = PARAMS_LIST.get(0);
    final String BOT_NAME = PARAMS_LIST.get(3);
    final String STICKER = STICKERS_LIST.get(0);
    final String PLAYING_ROOM_ID = PARAMS_LIST.get(1);
    final String PLAYING_ROOM_LINK = PARAMS_LIST.get(2);

    Integer currentPot;
    Integer moveCount;

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    InlineKeyboardButton buttonFoldPass = new InlineKeyboardButton();
    InlineKeyboardButton buttonCheckCall = new InlineKeyboardButton();
    //InlineKeyboardButton buttonCall = new InlineKeyboardButton();
    InlineKeyboardButton buttonBet = new InlineKeyboardButton();
    InlineKeyboardButton buttonBetSize1 = new InlineKeyboardButton();
    InlineKeyboardButton buttonBetSize2 = new InlineKeyboardButton();
    InlineKeyboardButton buttonBetSize3 = new InlineKeyboardButton();
    InlineKeyboardButton buttonBetSize4 = new InlineKeyboardButton();
    InlineKeyboardButton buttonBetSize5 = new InlineKeyboardButton();
    InlineKeyboardButton makeMoveButton = new InlineKeyboardButton();

    SendMessage message = new SendMessage();
    public final String COMMANDS_TEXT = MessageFormat.format("COMMANDS {0}   /about - about this bot  (alias command \"rules\"){1}   /commands - this list{2}   /poker <number> - start game registration with <number> of players{3}   /reg, /unreg - register to the game, or forfeit{4}   /invite <userId> - invite tg user to join game (success only if user communicated with bot in personal messages earlier){5}   /queue - get registered members list{6}To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat {7}. To set up in 1 step input command about. ", DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, DOUBLE_NEXTLINE, BOT_NAME);
    public final String RULES_TEXT = MessageFormat.format("I''m NL holdem dealer bot. {0}To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat {1} (without ''/'' before command){2}To start game: {3}1. type command \"poker [2-9]\" e.g. \"poker 3\" directly into playing room chat group, where arg = number of sits(=players) in the game. accepted values are >=2 && <=9 {4}2. type command \"reg\" to register to the game. (Also you can send invite to telegram user via \"invite\" command; Also player can unregister by using \"unreg\" command){5}When registered users will take all the sits, game starts.{6}Game scenario:{7}1. Dealer unpacks and shuffles deck, then deals 1 card to each player (Cards revealed). Deal order determined by players registration order. {8}2. The player with highest card starts on button, the players after him start on blinds. (value order on roll phase currently is 2<3<4<5<6<7<8<9<10<J<Q<K<A>2; if more than 1 player got same highest value, dealer shuffles deck and deal repeats. ){9}3. The game starts, and all the rules match TEXAS NL HOLDEM : {10}Cards are dealt in private message from bot. Player on his turn have to choose one of options (fold|check|call|bet|reraise). Player types his decision on his turn in playing room. Additionally, there is an option to schedule decision @ bot pm.{11}useless links:{12}https://automaticpoker.com/how-to-play-poker/texas-holdem-basic-rules/{13}https://automaticpoker.com/poker-basics/burn-and-turn-rules/{14}https://automaticpoker.com/poker-basics/texas-holdem-order-of-play/{15}READYCHECK{16}pregame:{17}+- create nl holdem game by type: +STP, -MTP, -cash{18}+- game registration mechanics: +reg, +-unreg, +invite, +queue, -clearstate{19}+- card deal mechanics: +draw, +-preflop, +-turn, +-river, +-showdown, +-burnwhendealing{20}game:{21}+- game state mechanics: +countBlindSize, +countBlindId, -substractfromplayerbalance, -addtopot +-removeBankrupt, +-finishGame {22}{23}+- player hand ranking: -pairs -2pairs -triple -fullhouse -quad -street +flash -streetflash +-highestkicker{24}- player turn phases mechanics: -check -call +-bet -raise -reraise -allin -pmMoveScheduler -afk{25}- clearing phase mechanics: -calculateCombinations -splitPotReward{26}misc:{27}+- gui: +mudMode, +msgEdit, +commands, +-inlineCommands, +-markupMessages{28}-stats: -processUsersStat, -provideUserStat, -processTournamentStat, -provideTournamentStat{29}TL;DR: currently unplayable after cards dealt", DOUBLE_NEXTLINE, PLAYING_ROOM_ID, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, TRIPLE_NEXT_LINE, TRIPLE_NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, TRIPLE_NEXT_LINE, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, DOUBLE_NEXTLINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, NEXT_LINE, TRIPLE_NEXT_LINE);

    String tableCards;
    //Date asd = tableCards;

    Integer playersCount;

    List<?> cardDeck;
    Integer dealOrderMsg = 0;
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

        return BOT_NAME;
    }

    @Override
    public String getBotToken() {

        return TOKEN;

    }

    public void sendSticker() {
        try {
            String currentId = message.getChatId();
            message.setChatId(PLAYING_ROOM_ID);
            SendSticker sendSticker = new SendSticker();
            sendSticker.setChatId(PLAYING_ROOM_ID);
            sendSticker.setSticker(new InputFile(STICKER));
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
            System.out.println("exception: " + e.getMessage() + NEXT_LINE + "message: " + message.getText());
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
            System.out.println("exception: " + e.getMessage() + NEXT_LINE + "message: " + message.getText());
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
            System.out.println("exception: " + message.getText());
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
            System.out.println("exception: messageId = " + messageId + "; message: " + message.getText());
        }
        return messageId;
    }
    public Integer sendToPlayingRoomWithButtonsAndGetMessageId(String text) {
        int messageId = -1;
        try {
            message.setChatId(PLAYING_ROOM_ID);
            message.setText(text);
            message.setReplyMarkup(inlineKeyboardMarkup);

            messageId = execute(message).getMessageId();
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message.getText());
        }
        return messageId;
    }
    public void deleteMessage(String messageId, String AUTODELETE_REGEXP, Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        try {
            deleteMessage.setChatId(PLAYING_ROOM_ID);
            if (update.hasMessage() && update.getMessage().getText().matches(AUTODELETE_REGEXP)) {

            }

            deleteMessage.setMessageId(update.getMessage().getMessageId());
            Thread.sleep(SLEEP_MS);
        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message.getText());
        }
    };
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
            System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
        }
    }
    public void editMessageIncr(String text, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        String currentText = msg.getText() + text;
        try {
            msg.setChatId(PLAYING_ROOM_ID);
            msg.setMessageId(messageId);
            msg.setText(currentText);
            execute(msg);
            Thread.sleep(SLEEP_MS);
            //return currentText;
        } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
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
        } catch (TelegramApiException |  NumberFormatException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
        }
    }

    public Integer extractMove(Update update, Integer moveCount) {
        String extractMove = update.getCallbackQuery().getData().toLowerCase().replaceAll(COMMAND_REGEXP, "$1");
        System.out.println("extractMove start");

        System.out.println("currentPlayerMoveChoice " + currentPlayerNumberChoice + "; currentPlayerNumberChoice: " + currentPlayerNumberChoice);
        switch (extractMove) {
            case "fold" -> {;
                currentPlayerMoveChoice = 0;

                if (currentPlayerMoveChoice == 0) { //fold
                    //set fold
                    players[  moveCount % players.length  ].set(5, true); //get(5)
                    moveCount = iterateMove(players, moveCount);
                }
            }
            case "check" -> {

                currentPlayerMoveChoice = 1;

                if (currentPlayerMoveChoice == 1) { // check/call
                    //check
                    if (PlayerUtils.checkBetsEqual(players)) {
                        System.out.println("check success - PlayerUtils.checkBetsEqual(players) == " + PlayerUtils.checkBetsEqual(players));
                        moveCount = iterateMove(players, moveCount);

                    }

                    //call
                    else if(PlayerUtils.findMaxbet(players) > Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) &&
                            PlayerUtils.findMaxbet(players) <= Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) ) {
                        currentPlayerNumberChoice = PlayerUtils.findMaxbet(players) - Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) ;

                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player

                        currentPot += currentPlayerNumberChoice;
                        System.out.println("call success; added chips " + currentPlayerNumberChoice);
                        moveCount = iterateMove(players, moveCount);
                    }

//                                                    else {
//                                                        System.out.println("check unsuccess - PlayerUtils.checkBetsEqual(players) == " + PlayerUtils.checkBetsEqual(players));
//                                                    }
                    //check current bet = highest or equal
                }
            }
            case "call" -> {

                currentPlayerMoveChoice = 1;

                if (currentPlayerMoveChoice == 1) { // check/call
                    //check
                    if (PlayerUtils.checkBetsEqual(players)) {
                        System.out.println("check success - PlayerUtils.checkBetsEqual(players) == " + PlayerUtils.checkBetsEqual(players));
                        moveCount = iterateMove(players, moveCount);

                    }

                    //call
                    if(PlayerUtils.findMaxbet(players) > Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) &&
                       PlayerUtils.findMaxbet(players) <= Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) ) {
                        currentPlayerNumberChoice = PlayerUtils.findMaxbet(players) - Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) ;

                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player

                        currentPot += currentPlayerNumberChoice;
                        System.out.println("call success; added chips " + currentPlayerNumberChoice);
                        moveCount = iterateMove(players, moveCount);
                    }

//                                                    else {
//                                                        System.out.println("check unsuccess - PlayerUtils.checkBetsEqual(players) == " + PlayerUtils.checkBetsEqual(players));
//                                                    }
                    //check current bet = highest or equal
                }
            }
            case "bet" -> {

                currentPlayerMoveChoice = 2;

                if (currentPlayerMoveChoice == 2) { //bet
                    if(currentPlayerNumberChoice <= 0) {
                        //currentPlayerNumberChoice = minBetSize;
                    }
                    if (raiseCount < 4) {

                        if (currentPlayerNumberChoice > PlayerUtils.findMaxbet(players) * 2) { // ()TODO && > max bet * 2
                            //refresh buttons
                        }
                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player
                        currentPot  += currentPlayerNumberChoice;
                        moveCount = iterateMove(players, moveCount);
                        raiseCount++;

                    }
                }
            }
            case "allin" -> {

                currentPlayerMoveChoice = 3;

                if (currentPlayerMoveChoice == 3) { //allin
                    if (raiseCount < 4) {
                        //refresh buttons
                        currentPlayerNumberChoice = Integer.parseInt(players[ moveCount % players.length ].get(3).toString());
                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player
                        currentPot  += currentPlayerNumberChoice;
                        moveCount = iterateMove(players, moveCount);
                        raiseCount++;
                    } else {System.out.println("raisecount >= 4, only call or fold is a option");}

                }
            }


//            case "makemove" -> {
//
//                if (currentPlayerMoveChoice != -1) {
//
//            }}

            case "number" -> {
                int tempNumber = Integer.parseInt(update.getCallbackQuery().getData().toLowerCase().replaceAll(BET_REGEXP, "$2"));

                if (tempNumber > 0 && Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) > tempNumber) {
                    currentPlayerNumberChoice = tempNumber;
                    System.out.println("player number: " + currentPlayerNumberChoice);
                }
                if (tempNumber > 0 && Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) < tempNumber) {
                    System.out.println("player number less than players balance:  " + currentPlayerNumberChoice);
                    System.out.println("setting max possible  " + Integer.parseInt(players[ moveCount % players.length ].get(3).toString()));
                    currentPlayerNumberChoice = Integer.parseInt(players[ moveCount % players.length ].get(3).toString());
                }
            }


            default -> {

                //System.out.println("checkBetsEqual = " + PlayerUtils.checkBetsEqual(players));
                //return;
            }
        };
        if (currentPlayerMoveChoice == -1) {
            sendToPlayer(players[moveCount].get(0).toString(),"to make bet choose 1 action from 1st menu row (if a choose bet, also push 1 button on 2nd row");
        }
        currentPlayerNumberChoice = -1;
        currentPlayerMoveChoice   = -1;
        System.out.println("extractMove finish");
        //);

        System.out.println(players[ moveCount % players.length ].get(1) + NEXT_LINE + "currentPlayerMoveChoice " + currentPlayerMoveChoice + NEXT_LINE + "currentPlayerNumberChoice " + currentPlayerNumberChoice + NEXT_LINE);
        return moveCount;
    }
    public String dealCards(ArrayList[] players, ArrayList[][] playerCards, Integer HAND_CARDS_COUNT) {
        text                  = "";
        currentPlayerHandText = "";
        allHandsText          = "";
        for (int playerNumber = 0; playerNumber < players.length; playerNumber++) {

            StringBuilder hand = new StringBuilder();
            for (int playerCardNumber = 0; playerCardNumber < HAND_CARDS_COUNT; playerCardNumber++) {
                hand.append(playerCards[ playerNumber ][ playerCardNumber ].get(0));
            }

            currentPlayerHandText = players[ playerNumber ].get(1) + " hand: " + hand + NEXT_LINE;
            //sendToPlayer(players[ y ].get(0).toString(), "dealNumber " + dealNumber + NEXT_LINE + "Your hand: " + currentPlayerHandText);
            sendToPlayer(players[ playerNumber ].get(0).toString(), "deal " + dealNumber + DOT + NEXT_LINE + hand + " your cards at dealNumber" + dealNumber + DOT + NEXT_LINE); // + currentPlayerHandText);
            allHandsText = allHandsText + currentPlayerHandText;




        }
        return allHandsText;
    }
/*    public static AnswerInlineQuery converteResultsToResponse(InlineQuery inlineQuery, List<?> results) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQuery.getId());
        //answerInlineQuery.setCacheTime(CACHETIME);
        answerInlineQuery.setResults((List<InlineQueryResult>) results);
        return answerInlineQuery;
    }
    private void handleIncomingInlineQuery(InlineQuery inlineQuery) {
        String query = inlineQuery.getQuery();
        try {
            if (!query.isEmpty()) {
                System.out.println("handleIncomingInlineQuery !query.isEmpty()");
                List<?> results = Collections.singletonList((query));
                execute(outputResults(converteResultsToResponse, results));
            } else {
                execute(outputResults(converteResultsToResponse, new ArrayList<>()));
            }
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }

    }
    private static List<InlineQueryResult> outputResults() {

        List<InlineQueryResult> results123 = new ArrayList<>();

        InputTextMessageContent messageContent = new InputTextMessageContent();
        messageContent.setMessageText("dick cunt");
        InlineQueryResultArticle article = new InlineQueryResultArticle();
        article.setInputMessageContent(messageContent);
        article.setId(Integer.toString(1));
        article.setTitle("titl");
        article.setDescription("de scription");
        article.setThumbUrl("about:blank");
        results123.add(article);

        return results123;
    }*/





    //List<InlineQueryResult> asdasd = new List





    @Override
    public void onUpdateReceived(Update update) {

        System.out.println("my_cards start");
        if (update.hasInlineQuery()) {

            AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
            List<InlineQueryResult> resultsList = new ArrayList<>();
            InlineQueryResultArticle article = new InlineQueryResultArticle();
            InputTextMessageContent messageContent = new InputTextMessageContent();
            article = new InlineQueryResultArticle();


            //List<?> cardDeck = new ArrayList<>();
            //cardDeck = initializeDeck();

            //messageContent.setDisableWebPagePreview(true);
            messageContent.setMessageText("DELETE_ME");
            article.setInputMessageContent(messageContent);
            //article.setInputMessageContent();
            article.setId(Integer.toString(0));
            article.setTitle("deal number 3.");
            article.setDescription("your hand: A♠ 3♠");
            //article.set
            //article.setReplyMarkup(inlineKeyboardMarkup);
            resultsList.add(article);


            answerInlineQuery.setInlineQueryId(update.getInlineQuery().getId());
            answerInlineQuery.setIsPersonal(true);
            //answerInlineQuery.setIsPersonal(true);
            answerInlineQuery.setResults(resultsList);

            System.out.println("my_cards end");
            try {
                execute(answerInlineQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }



        String userName, userId;
        String userIdMessage =  "0";
        String userIdCallback =  "0";


        {
            if (update.hasMessage()) {

                userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
                userId   = String.valueOf(update.getMessage().getFrom().getId());
                userIdMessage  = update.getMessage().getFrom().getId().toString();

                System.out.println("got update with text, from userName" + userName + "(" + userId + ")");
                userIdCallback = "0";
            }
            if (update.hasCallbackQuery()) {
                userIdCallback = String.valueOf(update.getCallbackQuery().getFrom().getId());
                userIdMessage  = "0";
            }
        }
        System.out.println("onUpdateReceived inited usrtext = " + userIdMessage + ", usrcallback = " + userIdCallback);



        if (!gameStarted && update.hasMessage() && update.getMessage().hasText()) {
            if (!preGameStarted) {

                userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + " " + "(" + update.getMessage().getFrom().getUserName() + ")";
                userId   = String.valueOf(update.getMessage().getFrom().getId());
                nickName = update.getMessage().getFrom().getUserName();

                String chatId = String.valueOf(update.getMessage().getChatId());
                String extractCommand, arg;
                extractCommand = update.getMessage().getText().replaceAll(COMMAND_REGEXP, "$1");
                if (extractCommand.length() < update.getMessage().getText().length()) {
                    try {
                        arg = update.getMessage().getText().replaceAll(ARGUMENT_REGEXP, "$2");
                        if (arg.length() > 0) {
                            System.out.println("arg = " + arg + NEXT_LINE + "extractCommand = " + extractCommand);
                            extractNumber = Integer.parseInt(arg);
                        } else {
                            extractNumber = 0;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
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
                                        sendToPlayingRoom(REG_FAILED + "Unable to send message to user. User need to start interact with bot in personal chat first.");
                                    } else {
                                        playersQueue = PlayerUtils.addPlayerToQueue(userId, userName, playersQueue, nickName, registeredPlayers, START_CHIPS, checkQueueFull, checkForUnreg);
                                        registeredPlayers++;
                                        playersList += userName + NEXT_LINE;
                                        editMessageIncr(DOUBLE_NEXTLINE + playersList,pokerMessageId);

                                        if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                            preGameStarted = true;
                                            dealStarted    = true;
                                            //editMessage(TOURNAMENT_INFO + DOUBLE_NEXTLINE + playersList + REG_FINISHED, pokerMessageId);
                                            editMessageIncr(REG_FINISHED,pokerMessageId);
                                            break;
                                        }
                                        return;

                                    }
                                } else
                                    sendBack(chatId, REG_FAILED + "checkForUnreg - " + checkForUnreg + ", checkForUnreg - " + checkForUnreg);
                                break;
                            } else sendBack(chatId, REG_FAILED + " or limits");
                            break;
                        } else sendBack(chatId, REG_CLOSED);
                        break;

                    case "invite", "/invite", "inv", "/inv", "/invite@pokedgram_bot", "invite@pokedgram_bot":
                        if (registrationStarted) {
                            if (extractNumber >= 10000000 && extractNumber <= 2121212100 && extractNumber != 2052704458) {
                                String inviteUserId = String.valueOf(extractNumber);
                                userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
                                nickName = "@" + inviteUserId;
                                int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(inviteUserId, playersQueue, registeredPlayers);
                                if (checkAlreadyReg < 0) {

                                    boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                    int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                    if (!checkQueueFull || checkForUnreg > -1) {

                                        if (CHECK_PLAYERS_INTERACTION && !sendToPlayer(inviteUserId, "You have been invited to play some poker by" + userName)) {
                                            sendToPlayingRoom("Invite failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                        } else {
                                            playersQueue = PlayerUtils.addPlayerToQueue(inviteUserId, "Player" + inviteUserId, playersQueue, nickName, registeredPlayers, START_CHIPS, checkQueueFull, checkForUnreg);

                                            // TODO() count invites seperately
                                            registeredPlayers++;
                                            //sendToPlayer(userId, "Invite success. Current queue: " + PlayerUtils.getUserReg(playersQueue, registeredPlayers, userId));
                                            sendBack(userId, "Invite success. "); //Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                                            //sendToPlayingRoom(inviteUserId + " invited to the game! " + (maxPlayers - registeredPlayers) + " to go");

                                            if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                                preGameStarted = true;
                                                //registrationStarted = false;
                                                sendToPlayingRoom(REG_FINISHED);
                                                break;
                                            } else {
                                                preGameStarted = false;
                                            }
                                        }
                                    }
                                } else
                                    sendToPlayer(userId, "Invite failed: checkReg - " + checkAlreadyReg + ", checkLimits - " + PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers));
                                break;
                            } else sendToPlayer(userId, "userId " + extractNumber + "do not match format.");
                            break;
                        } else sendBack(chatId, REG_CLOSED);
                        break;


                    case "/queue", "queue":
                        if (registrationStarted) {
                            sendBack(chatId, "Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                        } else {
                            sendBack(chatId, REG_CLOSED);
                        }
                        break;

                    case "/poker", "poker", "poker@pokedgram_bot", "/poker@pokedgram_bot":
                        playersQueue = null;
                        if (extractNumber >= 1 && extractNumber <= 9) {
                            maxPlayers = extractNumber;
                        } else {
                            maxPlayers = 9;
                        }
                        registeredPlayers = 0;
                        TOURNAMENT_INFO = new String("Tournament initiated! Registration free and open. Game Texas NL Holdem." + DOUBLE_NEXTLINE + "playersCount: " + maxPlayers + NEXT_LINE + "Single table tournament mode" + NEXT_LINE + "smallBlind " + START_SMALL_BLIND_SIZE + NEXT_LINE + "startChips " + START_CHIPS + NEXT_LINE + "blindIncrease x2 every 5 rounds");

                        pokerMessageId = sendToPlayingRoomAndGetMessageId(TOURNAMENT_INFO);
                        registrationStarted = true;
                        playersQueue = new ArrayList[ maxPlayers ];
                        break;

                    default:
                        return;


                } // pregame menu switch
                while (preGameStarted && !gameStarted) {
                    int tryCount = 0;
                    int rollWinnerPlayerId;
                    currentPot = 0;
                    List<?> deck = initializeDeck();

                    deck = shuffleDeck(deck);
                    tryCount++;
                    currentDealMessageId = sendToPlayingRoomAndGetMessageId(DECK_SHUFFLE_TEXT);
                    ArrayList[][] playerCards = DeckUtils.dealCards(maxPlayers, 1, deck);



//                        text                  = "";
//                        currentPlayerHandText = "";
//                        allHandsText          = "";
//
//                        for (int y = 0; y < playersQueue.length; y++) {
//                            StringBuilder hand = new StringBuilder();
//                            for (int i = 0; i < 1; i++) {
//                                hand.append(playerCards[ y ][ i ].get(0));
//                            }
//                            currentPlayerHandText = hand + NEXT_LINE;
//
//                            //sendToPlayer(playersQueue[ y ].get(0).toString(), "pregame roll number " + tryCount + NEXT_LINE + "Your cards: " + currentPlayerHandText);
//                            allHandsText += playersQueue[ y ].get(1).toString() + ": " + currentPlayerHandText;
//                        }

                    allHandsText = dealCards(playersQueue, playerCards, 1);
                    if (ROLL_VISIBILITY) {
                        editMessage(DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + allHandsText, currentDealMessageId);
                    } else {
                        editMessage(DECK_SHUFFLE_TEXT + NEXT_LINE + CARDS_DEALED_TEXT + DOUBLE_NEXTLINE, currentDealMessageId);
                    }


                    rollWinnerPlayerId = DeckUtils.rankRoll(maxPlayers, playerCards);


                    if (rollWinnerPlayerId == -1) {
                        editMessage(DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "We've equal highest, redraw (trycount " + tryCount + ")", currentDealMessageId); //+ DOUBLE_NEXTLINE + "Deck info: " + deck, rollMessageId);
                        gameStarted = false;
                    } else {
                        editMessage(DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "@" + playersQueue[ rollWinnerPlayerId ].get(1).toString() + " starting on the button", currentDealMessageId); // + TRIPLE_NEXT_LINE + "Deck info: " + deck, rollMessageId);
                        buttonId    = rollWinnerPlayerId;
                        gameStarted = true;
                        players     = playersQueue;
                        PlayerUtils.shiftPlayerOrder(players, buttonId);
                        userIdMessage = "0";
                        userIdCallback = "0";
                        break;

                        //TODO() delete pregame commands and add game commands promt
                        //TODO() create comfortable UX to make moves (inline/buttons/etc)
                    }
                    //} return;

                } //pregame started (reg finished). works fine
                // if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {


//return;
            }
        }

        if (gameStarted) {

            smallBlindSize = ((dealNumber / BLIND_INCREASE_RATE_DEALS) + 1) * START_SMALL_BLIND_SIZE;
            bigBlindSize   = smallBlindSize * 2;
            minBetSize = PlayerUtils.findMaxbet(players);
            if (minBetSize == 0) {
                minBetSize = bigBlindSize;
            }

            buttonCheckCall.setText("check/call");
            buttonFoldPass.setText("fold/pass");
            buttonCheckCall.setCallbackData("check");
            buttonFoldPass.setCallbackData("fold");
            buttonBetSize1.setText("bet " + String.valueOf(minBetSize));
            buttonBetSize2.setText("bet " + String.valueOf(minBetSize * 2));
            buttonBetSize3.setText("bet " + String.valueOf(minBetSize * 3));
            buttonBetSize4.setText("bet " + String.valueOf(minBetSize * 4));
            buttonBetSize5.setText("\uD83D\uDD34 all-in");
            buttonBetSize1.setCallbackData(String.valueOf("bet " + minBetSize));
            buttonBetSize2.setCallbackData(String.valueOf("bet " + minBetSize * 2));
            buttonBetSize3.setCallbackData(String.valueOf("bet " + minBetSize * 3));
            buttonBetSize4.setCallbackData(String.valueOf("bet " + minBetSize * 4));
            buttonBetSize5.setCallbackData("allin");
            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
            List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
            keyboardButtonsRow1.add(buttonFoldPass);
            keyboardButtonsRow1.add(buttonCheckCall);
            keyboardButtonsRow2.add(buttonBetSize1);
            keyboardButtonsRow2.add(buttonBetSize2);
            keyboardButtonsRow2.add(buttonBetSize3);
            keyboardButtonsRow2.add(buttonBetSize4);
            keyboardButtonsRow2.add(buttonBetSize5);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow1);
            rowList.add(keyboardButtonsRow2);
            inlineKeyboardMarkup.setKeyboard(rowList);

            //legacy
            //buttonBet.setText("bet");
            //buttonBet.setCallbackData("bet " + minBetSize);
            //makeMoveButton.setText("make move (press when choosen from 1 and 2 row)");
            //makeMoveButton.setCallbackData("makemove");
            //keyboardButtonsRow1.add(buttonCall);
            //keyboardButtonsRow1.add(buttonBet);
            //keyboardButtonsRow3.add(makeMoveButton);
            //rowList.add(keyboardButtonsRow3);  legacy

            if (cardsDealPhase == false) {
                currentPot = 0;
                moveCount = 0;
                raiseCount = 0;
                tableCards = "";
                dealNumber++;

                if (dealNumber > 1) {
                    players = PlayerUtils.shiftPlayerOrder(players, 1);
                } else {
                    cardDeck = initializeDeck();
                }


                if (players.length == 2) {
                    smallBlindId = 0;
                    bigBlindId   = smallBlindId + 1;

                    players[ smallBlindId ].set(3, Integer.parseInt(players[smallBlindId].get(3).toString()) - smallBlindSize); //current chips at player
                    players[ smallBlindId ].set(9, Integer.parseInt(players[smallBlindId].get(9).toString()) + smallBlindSize); // current
                    // bet at player
                    players[ bigBlindId ].set(3, Integer.parseInt(players[bigBlindId].get(3).toString()) - bigBlindSize); //current chips at player
                    players[ bigBlindId ].set(9, Integer.parseInt(players[ bigBlindId].get(9).toString()) + bigBlindSize); // current bet at player

                    currentPot += Integer.parseInt(players[ smallBlindId ].get(9).toString());
                    currentPot += Integer.parseInt(players[ bigBlindId ].get(9).toString());

                } else {
                    sendToPlayingRoom("only 2 ppl possible at this moment");
                    return;
                }
                //
                String dealInfoCurrent = "Player order: " + PlayerUtils.getUserRegQueue(players) +
                                         DOUBLE_NEXTLINE +
                                         players[ moveCount % players.length ].get(2) + " starting on the button" +
                                         DOUBLE_NEXTLINE +
                                         DECK_SHUFFLE_TEXT;
                //Integer dealOrderMsg = sendToPlayingRoomAndGetMessageId(dealInfoLast + NEXT_LINE + dealInfoCurrent);

                String blindsInfo = new String("deal number: " + dealNumber + DOUBLE_NEXTLINE +
                                               "\uD83D\uDD35 small blind @ " + players[(moveCount +1) % players.length].get(1) + NEXT_LINE +"\uD83D\uDFE1 big blind @ " + players[(moveCount +1)% players.length ].get(1));
                dealOrderMsg = sendToPlayingRoomAndGetMessageId(blindsInfo);
                editMessageIncr( DOUBLE_NEXTLINE + dealInfoCurrent,dealOrderMsg);


                cardDeck = shuffleDeck(cardDeck);

                ArrayList[][] playerCards = new ArrayList[ players.length ][ HAND_CARDS_COUNT ];

                playerCards = DeckUtils.dealCards(players.length, HAND_CARDS_COUNT, cardDeck);

                roundAnnounceString = new String(
                        "1." + players[ smallBlindId ].get(2) + " bet: " + players[ smallBlindId ].get(9) + ". " +
                        "Balance " + players[ smallBlindId ].get(3) + NEXT_LINE +
                        "2." + players[ bigBlindId ].get(2) + " bet: " + players[ bigBlindId ].get(9) + ". " +
                        "Balance " + players[ bigBlindId ].get(3));

                //currentDealMessageId = sendToPlayingRoomWithButtonsAndGetMessageId(roundAnnounceString + DOUBLE_NEXTLINE);
                if (DEAL_VISIBILITY) {
                    currentDealMessageId = sendToPlayingRoomWithButtonsAndGetMessageId(roundAnnounceString + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2) + DOUBLE_NEXTLINE+  tableCards);

                } else {
                    //editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)+ DOUBLE_NEXTLINE+  tableCards, currentDealMessageId);
                    currentDealMessageId = sendToPlayingRoomWithButtonsAndGetMessageId(roundAnnounceString + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2) + DOUBLE_NEXTLINE+  tableCards);
                }

                cardsDealPhase          = true;
                currentPlayerMoveChoice = -1;
                //currentPlayerNumberChoice = bigBlindSize;
            }
            if (cardsDealPhase == true) {

                System.out.println("gameStarted; cardsDealt == true; userIdCallback = " + userIdCallback);

                clearingPhase = false;
                //boolean preflopPhase = true, flopPhase = false, turnPhase = false, riverPhase = false, showdown = false;//, tradeDone = false;

                boolean isSidePotExist = false;


                //&& activeplayers>1
                //cards dealed blinds substracted, time to make moves
                if ((players.length - PlayerUtils.checkFoldCount(players)) == 1) {
                    cardsDealPhase = false;
                    System.out.println("1 player left, and he is a winner");

                    preflopPhase = true;
                    flopPhase    = false;
                    turnPhase    = false;
                    riverPhase    = false;
                    showdownPhase = false;

                    showdownPhase = true;
                    clearingPhase = false;
                }

                if (preflopPhase) {



                    if (Boolean.getBoolean(players[moveCount].get(5).toString()) || Boolean.getBoolean(players[moveCount].get(6).toString()) ) {
                        System.out.println("found fold or allin at " + players[moveCount].get(2));
                        moveCount = iterateMove(players, moveCount);
                    } else {

                        if (update.hasCallbackQuery() && userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString())) {
                            //sendToPlayingRoom("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("update.getCallbackQuery().toString() = " + update.getCallbackQuery().getData().toString());
                            moveCount = extractMove(update,moveCount);
                            roundAnnounceString = new String(
                                    "1." + players[ smallBlindId ].get(2) + " bet: " + players[ smallBlindId ].get(9) + ". " +
                                    "Balance " + players[ smallBlindId ].get(3) + NEXT_LINE +
                                    "2." + players[ bigBlindId ].get(2) + " bet: " + players[ bigBlindId ].get(9) + ". " +
                                    "Balance " + players[ bigBlindId ].get(3));

                            if (DEAL_VISIBILITY) {
                                editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            } else {
                                editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            }
                            //userIdCallback = update.getCallbackQuery().getFrom().getId().toString();
                            //System.out.println("userIdCallback: " + userIdCallback + "movecount: " + moveCount);

                            if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {
                                preflopPhase = false;
                                flopPhase    = true;
                                turnPhase    = false;
                                riverPhase    = false;
                                showdownPhase = false;
                                clearingPhase = false;
                                System.out.println("flop phase rdy");
                            } else {
                                System.out.println("flop phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                            }


                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && !userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString()) && userIdCallback != "0") {
                            sendToPlayer(userIdCallback, "userId " + userIdCallback + " not matched " + players[ moveCount % players.length ].get(0) + NEXT_LINE +
                                                         "expecting move from player" + players[ moveCount % players.length ].get(0) + " (" + players[ moveCount % players.length ].get(1) + ")" + NEXT_LINE);
                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && userIdCallback == "0") {
                            System.out.println("userIdCallback = " + userIdCallback);
                        }

                    }

                    if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {

                        preflopPhase = false;
                        flopPhase    = true;
                        turnPhase    = false;
                        riverPhase    = false;
                        showdownPhase = false;
                        clearingPhase = false;
                        System.out.println("flop phase rdy");
                    } else {
                        System.out.println("flop phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                    }

                }
                if (flopPhase) {
                    moveCount = 0+1;
                    //turnPhase = true;
                    flopCards = DeckUtils.phaseFlop(maxPlayers, HAND_CARDS_COUNT, cardDeck);
                    tableCards =  PHASE_FLOP_TEXT + DOUBLE_NEXTLINE +flopCards;

                    // editMessageWithButtons("*Dealer shuffling deck*   deal number: " + dealNumber + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "flop phase: " + NEXT_LINE + tableCards, currentDealMessageId);
                    editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                           +  DOUBLE_NEXTLINE + tableCards, currentDealMessageId);

                    if (Boolean.getBoolean(players[moveCount].get(5).toString())) {
                        System.out.println("found fold flag at " + players[moveCount].get(2));
                        moveCount = iterateMove(players, moveCount);
                    } else {
                        if (update.hasCallbackQuery() && userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString())) {
                            //sendToPlayingRoom("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("update.getCallbackQuery().toString() = " + update.getCallbackQuery().getData().toString());
                            moveCount = extractMove(update,moveCount);

                            if (DEAL_VISIBILITY) {
                                editMessageWithButtons(roundAnnounceString +DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            } else {
                                editMessageWithButtons(roundAnnounceString +DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            }
                            //userIdCallback = update.getCallbackQuery().getFrom().getId().toString();
                            //System.out.println("userIdCallback: " + userIdCallback + "movecount: " + moveCount);

                            if (moveCount >= players.length+1 && PlayerUtils.checkBetsEqual(players)) {

                                preflopPhase = false;
                                flopPhase    = false;
                                turnPhase    = true;
                                riverPhase    = false;
                                showdownPhase = false;
                                clearingPhase = false;
                                System.out.println("flop phase rdy");
                            } else {
                                System.out.println("flop phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                            }


                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && !userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString()) && userIdCallback != "0") {
                            sendToPlayer(userIdCallback, "userId " + userIdCallback + " not matched " + players[ moveCount % players.length ].get(0) + NEXT_LINE +
                                                         "expecting move from player" + players[ moveCount % players.length ].get(0) + " (" + players[ moveCount % players.length ].get(1) + ")" + NEXT_LINE);
                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && userIdCallback == "0") {
                            System.out.println("userIdCallback = " + userIdCallback);
                        }

                    }

                    if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {

                        preflopPhase = false;
                        flopPhase    = false;
                        turnPhase    = true;
                        riverPhase    = false;
                        showdownPhase = false;
                        clearingPhase = false;
                        System.out.println("river phase rdy");
                    } else {
                        System.out.println("river phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                    }



                }
                if (turnPhase) {
                    moveCount = 0;
                    turnCards = flopCards + DeckUtils.phaseTurn(maxPlayers, HAND_CARDS_COUNT, cardDeck);
                    tableCards = PHASE_TURN_TEXT + DOUBLE_NEXTLINE + turnCards;
                    //editMessageWithButtons("DECK_SHUFFLE_TEXT   deal number: " + dealNumber + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "turn phase: " + NEXT_LINE + tableCards, currentDealMessageId);
                    editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                           +  DOUBLE_NEXTLINE + tableCards, currentDealMessageId);

                    if (Boolean.getBoolean(players[moveCount].get(5).toString())) {
                        System.out.println("found fold flag at " + players[moveCount].get(2));
                        moveCount = iterateMove(players, moveCount);
                    } else {
                        if (update.hasCallbackQuery() && userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString())) {
                            //sendToPlayingRoom("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("update.getCallbackQuery().toString() = " + update.getCallbackQuery().getData().toString());
                            moveCount = extractMove(update,moveCount);
                            roundAnnounceString = new String(
                                    "1." + players[ smallBlindId ].get(2) + " bet: " + players[ smallBlindId ].get(9) + ". " +
                                    "Balance " + players[ smallBlindId ].get(3) + NEXT_LINE +
                                    "2." + players[ bigBlindId ].get(2) + " bet: " + players[ bigBlindId ].get(9) + ". " +
                                    "Balance " + players[ bigBlindId ].get(3));
                            if (DEAL_VISIBILITY) {
                                editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            } else {
                                editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            }
                            //userIdCallback = update.getCallbackQuery().getFrom().getId().toString();
                            //System.out.println("userIdCallback: " + userIdCallback + "movecount: " + moveCount);

                            if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {

                                preflopPhase = false;
                                flopPhase    = false;
                                turnPhase    = false;
                                riverPhase    = true;
                                showdownPhase = false;
                                System.out.println("flop phase rdy");
                            } else {
                                System.out.println("flop phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                            }


                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && !userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString()) && userIdCallback != "0") {
                            sendToPlayer(userIdCallback, "userId " + userIdCallback + " not matched " + players[ moveCount % players.length ].get(0) + NEXT_LINE +
                                                         "expecting move from player" + players[ moveCount % players.length ].get(0) + " (" + players[ moveCount % players.length ].get(1) + ")" + NEXT_LINE);
                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && userIdCallback == "0") {
                            System.out.println("userIdCallback = " + userIdCallback);
                        }

                    }

                    if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {

                        preflopPhase = false;
                        flopPhase    = false;
                        turnPhase    = false;
                        riverPhase    = true;
                        showdownPhase = false;
                        clearingPhase = false;
                        System.out.println("river phase rdy");
                    } else {
                        System.out.println("river phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                    }

                }
                if (riverPhase) {
                    moveCount = 0+1;
                    tableCards = turnCards + DeckUtils.phaseTurn(maxPlayers, HAND_CARDS_COUNT, cardDeck);
                    tableCards = PHASE_RIVER_TEXT + DOUBLE_NEXTLINE +  turnCards + DeckUtils.phaseRiver(maxPlayers, HAND_CARDS_COUNT, cardDeck);
                    editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                           +  DOUBLE_NEXTLINE  + tableCards, currentDealMessageId);


                    if (Boolean.getBoolean(players[moveCount].get(5).toString())) {
                        System.out.println("found fold flag at " + players[moveCount].get(2));
                        moveCount = iterateMove(players, moveCount);
                    } else {
                        if (update.hasCallbackQuery() && userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString())) {
                            //sendToPlayingRoom("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("userId " + userIdCallback + " matched " + players[ moveCount % players.length ].get(0));
                            //System.out.println("update.getCallbackQuery().toString() = " + update.getCallbackQuery().getData().toString());
                            moveCount = extractMove(update,moveCount);
                            roundAnnounceString = new String(
                                    "1." + players[ smallBlindId ].get(2) + " bet: " + players[ smallBlindId ].get(9) + ". " +
                                    "Balance " + players[ smallBlindId ].get(3) + NEXT_LINE +
                                    "2." + players[ bigBlindId ].get(2) + " bet: " + players[ bigBlindId ].get(9) + ". " +
                                    "Balance " + players[ bigBlindId ].get(3));
                            if (DEAL_VISIBILITY) {
                                editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            } else {
                                editMessageWithButtons(roundAnnounceString + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
                                                       + DOUBLE_NEXTLINE + tableCards, currentDealMessageId);
                            }
                            //userIdCallback = update.getCallbackQuery().getFrom().getId().toString();
                            //System.out.println("userIdCallback: " + userIdCallback + "movecount: " + moveCount);

                            if (moveCount >= players.length+1 && PlayerUtils.checkBetsEqual(players)) {

                                preflopPhase = false;
                                flopPhase    = false;
                                turnPhase    = false;
                                riverPhase    = false;
                                showdownPhase = true;
                                System.out.println("flop phase rdy");
                            } else {
                                System.out.println("flop phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                            }


                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && !userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString()) && userIdCallback != "0") {
                            sendToPlayer(userIdCallback, "userId " + userIdCallback + " not matched " + players[ moveCount % players.length ].get(0) + NEXT_LINE +
                                                         "expecting move from player" + players[ moveCount % players.length ].get(0) + " (" + players[ moveCount % players.length ].get(1) + ")" + NEXT_LINE);
                            userIdCallback = "0";
                        }
                        if (update.hasCallbackQuery() && userIdCallback == "0") {
                            System.out.println("userIdCallback = " + userIdCallback);
                        }

                    }

                    //when bets equal and betsquantity>1 showdown=true;
                    if (moveCount >= players.length && PlayerUtils.checkBetsEqual(players)) {
                        preflopPhase = false;
                        flopPhase    = false;
                        turnPhase    = false;
                        riverPhase    = false;
                        showdownPhase = true;
                        clearingPhase = false;
                        System.out.println("showdown phase rdy");
                    } else {
                        System.out.println("showdown phase not rdy. moveCount < players. PlayerUtils.checkBetsEqual(players). movecount: " + moveCount);

                    }
                }
                if (showdownPhase) {

                    clearingPhase = true;
                    //System.out.println("im in showdown");
                    if (clearingPhase) { //distribute chips
                        System.out.println("while (!clearingReady)");
                        // countCombinations
                        // if 0 tokens, remove player from game, stat his place
                        // if 1 player left, finish game
                        //find min stack
                        //if minstack > 0 minimum 2 times, set Tradeready false
                        //reinit players, cleanup playerhands, go to tradeready
                        players   = PlayerUtils.checkLastChips(players, bigBlindSize); //playersCount;
                        players   = PlayerUtils.removeZeroBalance(players);
                        players   = PlayerUtils.shiftPlayerOrder(players, 1);
                        moveCount = 0;
                        //players = new ArrayList[playersOld.length];

                        // add reward;
                        preflopPhase = false;
                        flopPhase = false;
                        turnPhase = false;
                        riverPhase = false;
                        clearingPhase = false;
                        showdownPhase  = false;
                        cardsDealPhase = false;
                        //clean folds allins etc
                        try {
                            Thread.sleep(SLEEP_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //clearingReady = true;
                    //edit
                }
            }

        }

        //userIdCallback = "0";
        //userIdMessage = "0";
        System.out.println("onUpdate end"+ nickName);
    }



    Integer iterateMove(ArrayList[] players, Integer moveCount) {
        moveCount = moveCount+1;
        System.out.println(players[moveCount % players.length ].get(2) + "made bet");
        return moveCount;
    }



}


//                case "/clearstate":
//                    if (userId.equals("193873212")) {
//                        sendToPlayer(userId, "clearstate +");
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
//                            sendBack(REG_CLOSED);
//                        }
//                        break; //
// clear and unreg