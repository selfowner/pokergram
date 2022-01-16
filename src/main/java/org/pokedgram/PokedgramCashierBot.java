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

// TODO() check sources and execute DealerBot after
public class PokedgramCashierBot extends TelegramLongPollingBot {


    final int STARTCHIPS = 20000;
    URL cfgPath = getResourceBySelfClassLoader("config.tg"); // added to .gitignore. 1 line - token, 2nd - roomId, 3rd - room invite link
    URL stickerPath = getResourceBySelfClassLoader("stickerpack.list");
    Path path, path1;
    List<String> cfg, stickerpack;
    SendMessage message = new SendMessage();
    int buttonId = -1;
    //init
    ArrayList[] playersQueue;
    String playingRoomId = cfg.get(1);
    String playingRoomLink = cfg.get(2);
    int registeredPlayers = 0;
    int extractNumber;
    int maxPlayers;
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
    boolean registrationStarted = false;
    boolean preGameStarted = false;
    boolean gameStarted = false;
    //debug flags
    boolean rollVisibility = true;
    boolean dealVisibility = false;
    int dealNumber;
    //tournament settings
    int startSmallBlindSize = 250;
    boolean sendToPlayerCheckOnReg = true;
    int cardsCount = 2; // holdem

    {
        try {
            path = Paths.get(cfgPath.toURI());
            path1 = Paths.get(stickerPath.toURI());
            cfg = Files.readAllLines(path, StandardCharsets.UTF_8);
            stickerpack = Files.readAllLines(path1, StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

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
        msg.setParseMode("MarkdownV2");
        try {
            msg.setChatId(playingRoomId);
            msg.setMessageId(messageId);
            msg.setText(text);
            execute(msg);
            Thread.sleep(2500);
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
            String userName, userId;
            userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            userId = String.valueOf(update.getMessage().getFrom().getId());
            System.out.println("got update with text, from userName" + userName + "(" + userId + ")");

            if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {
                System.out.println("if gamestarted == true");
                sendToPlayingRoom(message, "deal order old: " + nextLine + PlayerUtils.getUserRegQueue(playersQueue));
                ArrayList[] players = new ArrayList[playersQueue.length];
                players = playersQueue;
                players = PlayerUtils.shiftPlayerOrder(players, buttonId);
                int playersCount = players.length;
                //sendToPlayingRoom(message, "deal order: " + nextLine + PlayerUtils.getUserRegQueue(players));

                boolean tournamentReady = false;
            }


        }

        if (update.hasMessage() && update.getMessage().hasText() && registrationStarted) {
            userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + " " + "(" + update.getMessage().getFrom().getUserName() + ")";
            userId = String.valueOf(update.getMessage().getFrom().getId());
            String extractCommand, arg;

            extractCommand = update.getMessage().getText().replaceAll(commandRegExp, "$1");
            if (extractCommand.length() < update.getMessage().getText().replaceAll(commandRegExp, "$1").length()) {
                try {
                    arg = update.getMessage().getText().replaceAll(argRegExp, "$2");
                    System.out.println("arg = " + arg + nextLine + "extractCommand = " + extractCommand);
                    extractNumber = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    extractNumber = 0;
                }
            }
        }
        //registrationStarted = false;

        /*if (update.hasMessage() && update.getMessage().hasText() && preGameStarted) {
            int tryCount = 0;
            int rollWinnerPlayerId;

            List<?> deck = initializeDeck();

            deck = shuffleDeck(deck);
            tryCount++;
            Integer rollMessageId = sendToPlayingRoomAndGetMessageId(message, "*Dealer shuffling deck*");
            int cardsCount = 1;
            ArrayList[][] playerCards = DeckUtils.dealCards(maxPlayers, cardsCount, deck);
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
                editMessage("*Dealer shuffling deck*" + doubleNextLine + allHandsText + doubleNextLine + "We've equal highest, redraw (trycount " + tryCount + ")" + doubleNextLine + "Deck info: " + deck, rollMessageId);
                gameStarted = false;
            } else {
                editMessage("*Dealer shuffling deck*" + doubleNextLine + allHandsText + doubleNextLine + playersQueue[rollWinnerPlayerId].get(1).toString() + " starting on the button" + tripleNextLine + "Deck info: " + deck, rollMessageId);
                buttonId = rollWinnerPlayerId;
                gameStarted = true;
                return;
            }
            //} return;

        }*/

        if (update.hasMessage() && update.getMessage().hasText() && !gameStarted && !preGameStarted) { //&& !preGameStarted && !gameStarted) {

            System.out.println("update && !preGameStarted"); // && !gameStarted");
            if (!preGameStarted) {
                String userId;
                //System.out.println("else if (!preGameStarted) {");
                userId = String.valueOf(update.getMessage().getFrom().getId());
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

                    }
                }

                switch (extractCommand.toLowerCase()) {
                    case "commands", "/commands", "/commands@pokedgram_bot":
                        sendBack(message, chatId, commands);
                        break;

                    case "rules", "/rules", "/rules@pokedgram_bot", "/about", "/about@pokedgram_bot":
                        sendBack(message, chatId, rulesText);
                        break;

                    case "reg", "/reg", "/reg@pokedgram_bot":
                        if (registrationStarted) { // TODO() split prereg and reg switch

                            int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);
                            if (checkAlreadyReg < 0) {


                                boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                                int checkForUnreg = PlayerUtils.checkUserUnreg(playersQueue);
                                if (!checkQueueFull || checkForUnreg > -1) {
                                    if (sendToPlayerCheckOnReg && !sendToPlayer(message, userId, "Register success.")) {
                                        sendToPlayingRoom(message, "Register failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                    } else {
                                        playersQueue = PlayerUtils.addPlayerToQueue(userId, userName, playersQueue, playingRoomId, registeredPlayers, STARTCHIPS, checkQueueFull, checkForUnreg);
                                        sendToPlayingRoom(message, playersQueue[registeredPlayers].get(1) + " registered to the game! \n" + (maxPlayers - registeredPlayers) + " to go");
                                        registeredPlayers++;

                                        if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                            preGameStarted = true;

                                            sendToPlayingRoom(message, "Players registered! Lets roll!");
                                            return;
                                        }
                                        return;

                                    }
                                } else
                                    sendBack(message, chatId, "Register failed: checkForUnreg - " + checkForUnreg + ", checkForUnreg - " + checkForUnreg);
                            } else sendBack(message, chatId, "Register failed or limits");
                        } else sendBack(message, chatId, "Registration is not open");

                    case "invite", "/invite", "/invite@pokedgram_bot", "invite@pokedgram_bot":
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
                                            playersQueue = PlayerUtils.addPlayerToQueue(inviteUserId, "Player" + inviteUserId, playersQueue, playingRoomId, registeredPlayers, STARTCHIPS, checkQueueFull, checkForUnreg);

                                            // TODO() count invites seperately
                                            registeredPlayers++;
                                            //sendToPlayer(message, userId, "Invite success. Current queue: " + PlayerUtils.getUserReg(playersQueue, registeredPlayers, userId));
                                            sendToPlayer(message, userId, "Invite success. Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                                            sendToPlayingRoom(message, inviteUserId + " invited to the game! " + (maxPlayers - registeredPlayers) + " to go");

                                            if (PlayerUtils.checkUserUnreg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                                preGameStarted = true;
                                                //registrationStarted = false;
                                                sendToPlayingRoom(message, "Players registered! Lets roll!");
                                                return;
                                            } else {
                                                preGameStarted = false;
                                            }
                                        }
                                    }
                                } else
                                    sendToPlayer(message, userId, "Invite failed: checkReg - " + checkAlreadyReg + ", checkLimits - " + PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers));
                            } else sendToPlayer(message, userId, "userId " + extractNumber + "do not match format.");
                        } else sendBack(message, chatId, "Registration is not open");


                    case "/queue", "queue":
                        if (registrationStarted) {
                            sendBack(message, chatId, "Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                        } else {
                            sendBack(message, chatId, "Registration is not open");
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
                        sendToPlayingRoom(message, "Tournament initiated! Registration free and open. Game Texas NL Holdem." + doubleNextLine + "playersCount: " + maxPlayers + nextLine + "stp config " + nextLine + "smallBlind " + startSmallBlindSize + nextLine + "startChips " + STARTCHIPS + nextLine + "blindIncrease x2 every 5 rounds");
                        registrationStarted = true;
                        playersQueue = new ArrayList[maxPlayers];
                        return;

                    default:
//                        try {
//                            Thread.sleep(4000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        return;


                }
            }
        }
        System.out.println("passed 2 switches till\nonUpdateRecieved end");


    }


}


//    public enum GamePhaseInit {
//        pregame (false),
//        game(false);
//
//        private Boolean phaseState;
//
//
//        GamePhaseInit (Boolean phaseState) {
//            this.phaseState = phaseState;
//        }
//    }


//                case "/clearstate":
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