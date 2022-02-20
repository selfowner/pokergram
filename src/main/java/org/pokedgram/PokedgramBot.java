package org.pokedgram;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.qos.logback.core.util.Loader.getResourceBySelfClassLoader;
import static org.pokedgram.DeckUtils.*;
import static org.pokedgram.PlayerUtils.*;
import static org.pokedgram.SuperStrings.*;


//TODO rename to PokedgramDealerBot
public class PokedgramBot extends TelegramLongPollingBot {

    Integer pokerMessageId;
    ArrayList[] players = new ArrayList[ 2 ];


    int checkSameStage = -2;
    Boolean dealStarted = false;
    Integer raiseCount;

    int minBetSize; // = smallBlindSize;
    int currentPlayerMoveChoice;
    int currentPlayerNumberChoice;
    String TOURNAMENT_INFO;

    String nickName = EMPTY_STRING;
    String[] playerStages = new String[]{"preflop", "flop", "turn", "river", "showdown"};
    int currentPlayerStage = -2;
    String flopCards = EMPTY_STRING, turnCards = EMPTY_STRING, riverCards = EMPTY_STRING;
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

    static Integer currentPot = 0;
    Integer moveCount = 0;

    SendMessage message = new SendMessage();
    public final String COMMANDS_TEXT = MessageFormat.format("COMMANDS {0}   /about - about this bot  (alias command \"rules\"){1}   /commands - this list{2}   /poker <number> - start game registration with <number> of players{3}   /reg, /unreg - register to the game, or forfeit{4}   /invite <userId> - invite tg user to join game (success only if user communicated with bot in personal messages earlier){5}   /queue - get registered members list{6}   /test - run game mechanics tests{7}To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat {8}. To set up in 1 step input command about. ", DOUBLE_NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, DOUBLE_NEXTLINE, BOT_NAME);
    public final String RULES_TEXT = MessageFormat.format("I''m NL holdem poker dealer bot. {0}To be able to play with bot you need to write him any personal message (https://t.me/pokedgram_bot), then input commands in chat {1} (without ''/'' before command){2}To start game: {3}1. type command \"poker [2-9]\" e.g. \"poker 3\" directly into playing room chat group, where arg = number of sits(=players) in the game. accepted values are >=2 && <=9 {4}2. type command \"reg\" to register to the game. (Also you can send invite to telegram user via \"invite\" command; {5}When registered users will take all the sits, game starts.{6}Game scenario:{7}1. Dealer unpacks and shuffles deck, then deals 1 card to each player (Cards revealed). Deal order determined by players registration order. {8}2. The player with highest card starts on button, the players after him start on blinds. (value order on roll stage currently is 2<3<4<5<6<7<8<9<10<J<Q<K<A>2; if more than 1 player got same highest value, dealer shuffles deck and deal repeats. ){9}3. The game starts, and all the rules match TEXAS NL HOLDEM : {10}To view cards enter @"+ getBotUsername() + " . Player on his turn have to choose one of options (fold|check|call|bet|reraise). Player types his decision on his turn in playing room. {15}current build features bugs in{16}pregame:{17}+- create nl holdem game, currently only heads up, +chips{18}+- registration mechanics: +reg, +invite, +queue{19}+- card deal mechanics: +draw, +preflop, +turn, +river, +-showdown, +-burnwhendealing{20}game:{21}+- game state mechanics: +countBlindSize, +countBlindId, +-playerBalance +-pot +-removeBankrupt, +-finishGame {22}{23}+- player hand ranking: +2x +2x2x +3x +3x2x +4x -12345 +-&&&&& -1&2&3&4&5& +-highestkicker{24}- player turn stages mechanics: +check +call +-bet +raise +reraise +-allin -setBet -afk{25}- clearing stage mechanics: +-calculateHands +-splitPotReward{26}misc:{27}+-gui, +msgEdit, +commands, +-inlineCommands, +-markupMessages{28}-stats: -processUsersStat, -provideUserStat, +-processTournamentStat, -provideTournamentStat{29}TL;DR: currently unplayable after cards dealt", DOUBLE_NEXTLINE, PLAYING_ROOM_ID, DOUBLE_NEXTLINE, NEXTLINE, NEXTLINE, TRIPLE_NEXTLINE, TRIPLE_NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, DOUBLE_NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, TRIPLE_NEXTLINE, DOUBLE_NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, DOUBLE_NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, NEXTLINE, TRIPLE_NEXTLINE);

    String tableCards;

    ArrayList[][] playerCards = new ArrayList[ players.length ][ HAND_CARDS_COUNT ];

    List<String> cardDeck;
    Integer dealOrderMsg = 0;
    Integer currentDealMessageId;

    String userName, userId;



    public ArrayList<?>[] getShowdownWinnerIdTest(ArrayList<?>[] players, ArrayList<String>[][] playerCards, List<?> cardDeck) {
        String outputTest = EMPTY_STRING;
        int winnerId =-1;
        Integer winnerCount = -1;

        //ArrayList[] tableArray = drawTable(players.length, cardDeck);
        if (false) {
            //sidepot
        }
        boolean gotWinner = false;

        //calculate winner and process chips
        while (!gotWinner) {

            //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0

            //System.out.println("findWinner start");


            // TODO count kickers on table for some cases
            winnerCount += checkDistinctTest(playerCards, players);
            //winnerCount += checkFlash(players, playerCards, tableArray);
            winnerCount += checkStraight(playerCards);

            System.out.println("checkDistinct winnerCount after checkDistinct checkFlash checkStraight: " + winnerCount);
            outputTest +=  "checkDistinct winnerCount after checkDistinct checkFlash checkStraight: " + winnerCount;


            if (Integer.parseInt(players[ 0 ].get(13).toString()) > Integer.parseInt(players[ 1 ].get(13).toString())) {
                System.out.println("player 0 win");
                outputTest += "player 0 win";
                winnerId = 0; //player 0 win
            } else if (Integer.parseInt(players[ 1 ].get(13).toString()) > Integer.parseInt(players[ 0 ].get(13).toString())){

                System.out.println("player 1 win");
                outputTest += "player 1 win";
                winnerId = 1; //player 1 win
            } else if ( // comboValue ==
                    Integer.parseInt(players[ 0 ].get(13).toString()) == Integer.parseInt(players[ 1 ].get(13).toString()) && (Integer.parseInt(players[ 0 ].get(13).toString()) > -1 && Integer.parseInt(players[ 1 ].get(13).toString()) > -1)
            ) {
                System.out.println("players tie, checking kicker:");
                outputTest += "players tie, checking kicker:";
                winnerCount = checkKicker(players, playerCards,0);
            }

            // winnerCount = checkKicker(players, playerCards, tableArray);



//            if (winnerCount > 0) {
//                System.out.println("checkDistinct winnerCount: " + winnerCount);
//                //find kicker or split reward
//                gotWinner = true;
//                break;
//            }

            //System.out.println("findWinner end");
            break;
        }

        int playerIncome = splitPot(currentPot,players);
        if (winnerCount > 1) {
            for (ArrayList player : players) {
                player.set(3, Integer.parseInt(player.get(3).toString()) + playerIncome);
                currentPot = 0;
            }
            System.out.println("winnerCount > 1; income "  + playerIncome);
            outputTest += "winnerCount > 1; income "  + playerIncome;
            gotWinner = true;
            // process chips
        } else if (winnerCount == 0) {
            System.out.println("winnerCount == 0; no combo found. Splitting pot, setting gotWinner true "  + playerIncome);
            outputTest += "winnerCount == 0; no combo found. Splitting pot, setting gotWinner true "  + playerIncome;
            for (ArrayList player : players) {
                player.set(3, Integer.parseInt(player.get(3).toString()) + playerIncome);
                currentPot = 0;
                gotWinner  = true;
            }
        } else if (winnerCount == 1) {
            //get winnerId
        }
        //messageSendToPlayingRoom(outputTest);
        return players;
    }

    int checkDistinctTest(ArrayList<String>[][] playersCards, ArrayList[] players) {
        //messageSendToPlayingRoom("checkDistinct: start");
        int countCombo = 0;
        int playerAndTableCards = 7;

        for (int i = 0; i < players.length; i++) {

            String[] playersDistinctCards = new String[ 3 ];
            int distinctQuantity = 0;
            String excludeSingle;
            ArrayList<?>[] currentPlayerCards = getPlayerCardsWithTable(i, playersCards, players);
            List<Integer> cardValues = new ArrayList<>();

            for (int b = 0; b < playerAndTableCards; b++) {

                cardValues.add(Integer.parseInt(currentPlayerCards[ 3 ].get(b).toString()));
            }

            excludeSingle = cardValues.stream().sorted().collect(Collectors.groupingBy(Function.identity(),
                    Collectors.counting())).toString().replaceAll(EXTRACT_DISTINCT_VALUES_REGEXP, EMPTY_STRING);

            distinctQuantity = excludeSingle.replaceAll(EXTRACT_VALUES_DIRTY_REGEXP, EMPTY_STRING).length();
            messageSendToPlayingRoom("player: " + i + NEXTLINE + "rank = distinct: " + excludeSingle + "; distinctQuantity: " + distinctQuantity);//

            try {

                if (distinctQuantity > 0) {
                    countCombo++;
                    playersDistinctCards[ 0 ] = excludeSingle.replaceAll(EXTRACT_DISTINCT_COUNT_REGEXP, "$1");
//str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0
                    if (playersDistinctCards[ 0 ].matches(FIND_4X_REGEXP)) {
                        System.out.println("matches " + FIND_4X_REGEXP);
                        players[ i ].set(13, 7);
                        //playerComboGrade[playerid] = %regex to find combo value%
                        //playerKickerCards[playerid] = %check hand cards + different cases
                    } else //4 of a kind
                        if (playersDistinctCards[ 0 ].matches(FIND_2X_REGEXP) &&      //full
                            playersDistinctCards[ 0 ].matches(FIND_3X_REGEXP)) {
                            System.out.println(".matches(\"^.*=[3].*$\").matches(\"^.*=[2].*$\")");

                            players[ i ].set(13, 6);
                        } else//house

                            if (playersDistinctCards[ 0 ].matches(FIND_3X_REGEXP)) {
                                System.out.println("matches " + FIND_3X_REGEXP);
                                players[ i ].set(13, 3);
                            } else//x3

                                if (playersDistinctCards[ 0 ].matches(FIND_2X2X_REGEXP)) {
                                    System.out.println("matches " + FIND_2X2X_REGEXP);

                                    players[ i ].set(13, 2);
                                } else//2x2

                                    if (playersDistinctCards[ 0 ].matches(FIND_2X_REGEXP)) {
                                        System.out.println("matches " + FIND_2X_REGEXP);

                                        players[ i ].set(13, 1);
                                    }  //x2


                    //else if check kicker
                    //if (distinctQuantity==4) {} else
                    //if (distinctQuantity == 3) {
                        //check 4x
                        //check 3x+2x
                        //check 2x + 2x
                        //check 2x
                        //
             //           messageSendToPlayingRoom("distinctQuantity==3 " + NEXT_LINE + playersDistinctCards[ 0 ]);
                   // } else if (distinctQuantity == 2) {
            //            messageSendToPlayingRoom("distinctQuantity==2 " + NEXT_LINE + playersDistinctCards[ 0 ]);
                    //} else if (distinctQuantity == 1) {
        //                messageSendToPlayingRoom("distinctQuantity==1 " + NEXT_LINE + playersDistinctCards[ 0 ]);
                    //}


                //} else { //0
                    //messageSendToPlayingRoom("distinctQuantity==0 - no combo found");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //messageSendToPlayingRoom("checkDistinct: end");
        }
        return countCombo;
    }
    
//    public void checkHandsasdasd() {
//
//    }
    
    public void switchPregame(String extractCommand, Integer extractNumber, Update update) {


        userName      = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + " " + "(" + update.getMessage().getFrom().getUserName() + ")";
        userId        = String.valueOf(update.getMessage().getFrom().getId());
        nickName      = update.getMessage().getFrom().getUserName();

        String chatId = String.valueOf(update.getMessage().getChatId());


        switch (extractCommand.toLowerCase()) {
            case "commands", "/commands", "/commands@pokedgram_bot":
                messageSendToPlayerBack(chatId, COMMANDS_TEXT);
                break;

            case "rules", "/rules", "/rules@pokedgram_bot", "/about", "/about@pokedgram_bot":
                messageSendToPlayerBack(chatId, RULES_TEXT);
                break;

            case "reg", "/reg", "/reg@pokedgram_bot":
                if (registrationStarted) { // TODO split prereg and reg switch

                    int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);
                    if (checkAlreadyReg < 0) {

                        boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                        int checkForUnreg = PlayerUtils.checkUserUnReg(playersQueue);
                        if (!checkQueueFull || checkForUnreg > -1) {
                            //TODO rework `!messageSendToPlayer(userId, "Register success.")` check
                            if (CHECK_PLAYERS_INTERACTION && !messageSendToPlayer(userId, "pmDelivered true; legitReg true; " + NEXTLINE + " Register success.")) {
                                messageSendToPlayingRoom(REG_FAILED + "Unable to send message to user. User need to start interact with bot in personal chat first.");
                            } else {
                                playersQueue = PlayerUtils.addPlayerToQueue(userId, userName, playersQueue,
                                        nickName, registeredPlayers, START_CHIPS, checkQueueFull);
                                //editMessageInc(playersQueue[ playersQueue.length-1 ].get(1).toString() + "
                                // (" + playersQueue[ playersQueue.length-1 ].get(0).toString() + ")", pokerMessageId);
                                registeredPlayers++;
                                if (PlayerUtils.checkUserUnReg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                    preGameStarted = true;
                                    dealStarted    = true;
                                    //editMessage(TOURNAMENT_INFO + DOUBLE_NEXTLINE + playersList + REG_FINISHED, pokerMessageId);
                                    messageEditInc(REG_FINISHED, pokerMessageId);
                                    break;
                                }
                                return;
                            }
                        } else
                            messageSendToPlayerBack(chatId, REG_FAILED + "checkForUnreg - " + checkForUnreg + ", " + "checkForUnreg - " + checkForUnreg);
                        break;
                    } else messageSendToPlayerBack(chatId, REG_FAILED + " or limits");
                    break;
                } else messageSendToPlayerBack(chatId, REG_CLOSED);
                break;

            case "invite", "/invite", "inv", "/inv", "/invite@pokedgram_bot", "invite@pokedgram_bot":
                if (registrationStarted) {
                    if (extractNumber >= 10000000 && extractNumber <= 2121212100 && extractNumber != 2052704458) {
                        String inviteUserId = String.valueOf(extractNumber);
                        userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
                        nickName = inviteUserId;
                        int checkAlreadyReg = PlayerUtils.checkUserRegExistAndActive(inviteUserId, playersQueue, registeredPlayers);
                        if (checkAlreadyReg < 0) {

                            boolean checkQueueFull = PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers);
                            int checkForUnreg = PlayerUtils.checkUserUnReg(playersQueue);
                            if (!checkQueueFull || checkForUnreg > -1) {

                                if (CHECK_PLAYERS_INTERACTION && !messageSendToPlayer(inviteUserId, "You have been invited to play some poker by" + userName)) {
                                    messageSendToPlayingRoom("Invite failed: Unable to send message to user. User need to start interact with bot in personal chat first.");
                                } else {
                                    playersQueue = PlayerUtils.addPlayerToQueue(inviteUserId, "Player" + inviteUserId, playersQueue, nickName, registeredPlayers, START_CHIPS, checkQueueFull);

                                    // TODO count invites seperately
                                    registeredPlayers++;
                                    //sendBack(userId, "Invite success. ");

                                    if (PlayerUtils.checkUserUnReg(playersQueue) == -1 && registeredPlayers == maxPlayers) {
                                        preGameStarted = true;
                                        //registrationStarted = false;
                                        messageSendToPlayingRoom(REG_FINISHED);
                                        break;
                                    } else {
                                        preGameStarted = false;
                                    }
                                }
                            }
                        } else
                            messageSendToPlayer(userId, "Invite failed: checkReg - " + checkAlreadyReg + ", checkLimits - " + PlayerUtils.checkUserLimit(maxPlayers, registeredPlayers));
                        break;
                    } else messageSendToPlayer(userId, "userId " + extractNumber + "do not match format.");
                    break;
                } else messageSendToPlayerBack(chatId, REG_CLOSED);
                break;


            case "/queue", "queue":
                if (registrationStarted) {
                    messageSendToPlayerBack(chatId, "Current queue: " + PlayerUtils.getUserRegQueue(playersQueue));
                } else {
                    messageSendToPlayerBack(chatId, REG_CLOSED);
                }
                break;

            case "/test", "test", "test@pokedgram_bot", "/test@pokedgram_bot":

                //ArrayList[] currentPlayerCards;//Integer potSize = 0;//Integer currentPot = 0;
                String testName;
                moveCount = 0;
                List<String> cardDeck;
                ArrayList[] players = prepareTestDataPlayers();

                //TODO make test more fair
                messageSendToPlayingRoom("Deck test: " + DOUBLE_NEXTLINE + "New deck: " + initializeCardsDeck() + NEXTLINE + "Shuffled deck: " + shuffleCardsDeck(initializeCardsDeck()) + NEXTLINE + "Shuffled again: " + shuffleCardsDeck(initializeCardsDeck()));

                //TODO add tests for gui, chips processing, kickers, playerstate cleanup, name generator, ...
                cardDeck = prepareTestDataCardDeck3x2xand2x2x2x();
                testName = "3x2x and 2x2x2x: ";
                playerCards  = DeckUtils.dealCardsFromDeck(players.length, HAND_CARDS_COUNT, cardDeck);
                //currentPlayerCards = getPlayerCardsWithTable(moveCount,playerCards,players);
                allHandsText = dealCardsCombosText(players, playerCards, HAND_CARDS_COUNT, cardDeck);
                messageSendToPlayingRoom(testName + DOUBLE_NEXTLINE + allHandsText + NEXTLINE);
                getShowdownWinnerIdTest(players, playerCards, cardDeck);


                cardDeck = prepareTestDataCardDeck4x();
                testName = "4x and 2x2x2x: ";
                playerCards  = DeckUtils.dealCardsFromDeck(players.length, HAND_CARDS_COUNT, cardDeck);
                //currentPlayerCards = getPlayerCardsWithTable(moveCount,playerCards,players);
                allHandsText = dealCardsCombosText(players, playerCards, HAND_CARDS_COUNT, cardDeck);
                messageSendToPlayingRoom( testName+ DOUBLE_NEXTLINE + allHandsText + NEXTLINE);
                getShowdownWinnerIdTest(players, playerCards, cardDeck);



                break;


            case "/poker", "poker", "poker@pokedgram_bot", "/poker@pokedgram_bot":

                //playersQueue = null;
                if (extractNumber >= 1 && extractNumber <= 9) {
                    maxPlayers = extractNumber;
                } else {
                    maxPlayers = 9;
                }
                registeredPlayers = 0;

                registrationStarted = true;
                playersQueue = new ArrayList[ maxPlayers ];
                TOURNAMENT_INFO = new String("Tournament initiated! Registration free and open. Game Texas NL Holdem." + DOUBLE_NEXTLINE + "playersCount: " + maxPlayers + NEXTLINE + "Single table tournament mode" + NEXTLINE + "smallBlind " + START_SMALL_BLIND_SIZE + NEXTLINE + "startChips " + START_CHIPS + NEXTLINE + "blindIncrease x2 every 5 rounds");

                pokerMessageId = messageSendToPlayingRoomAndGetMessageId(TOURNAMENT_INFO);
                break;

            default:
                return;


        } // pregame menu switch
    }

    private List<String> prepareTestDataCardDeck3x2xand2x2x2x() {
        ArrayList<String> cardDeck = new ArrayList<>(52);
        cardDeck.add("6♥");
        cardDeck.add("5♣");
        cardDeck.add("5♥");
        cardDeck.add("5♦");
        cardDeck.add("6♣");
        cardDeck.add("6♦");
        cardDeck.add("3♣");
        cardDeck.add("K♣"); // burn
        cardDeck.add("6♠");
        cardDeck.add("K♠"); // burn
        cardDeck.add("8♣");
        cardDeck.add("J♣");
        cardDeck.add("Q♣");
        cardDeck.add("5♣");
        cardDeck.add("3♣");
        cardDeck.add("4♣");
        cardDeck.add("J♠");
        cardDeck.add("Q♠");
        cardDeck.add("A♠");
        return cardDeck;
    }
    private List<String> prepareTestDataCardDeck4x() {
        List<String> cardDeck = new ArrayList<>(52);
        cardDeck.add("6♥");
        cardDeck.add("K♥");
        cardDeck.add("5♣");
        cardDeck.add("K♦");
        cardDeck.add("6♣");
        cardDeck.add("6♦");
        cardDeck.add("3♣"); // burn
        cardDeck.add("K♣");
        cardDeck.add("6♠");
        cardDeck.add("K♠"); // burn
        cardDeck.add("8♣");
        cardDeck.add("J♣");
        cardDeck.add("Q♣");
        cardDeck.add("5♣");
        cardDeck.add("3♣");
        cardDeck.add("4♣");
        cardDeck.add("J♠");
        cardDeck.add("Q♠");
        cardDeck.add("A♠");
        return cardDeck;
    }

    //TODO consider elements type
    private ArrayList<String>[] prepareTestDataPlayers() {
        ArrayList players[] = new ArrayList[2];
        players[0] = new ArrayList<String>(13) {};
        players[0].add(0, 100); // string userId
        players[0].add(1, 1); // string user firstname + lastname
        players[0].add(2, 1);
        players[0].add(3, 20000); //chipsQuantity
        players[0].add(4, TRUE_TEXT); // isActive for reg/unreg
        players[0].add(5, false); // FoldFlag
        players[0].add(6, false); // AllinFlag manual
        players[0].add(7, FALSE_TEXT); // isChipleader?
        players[0].add(8, 0); // currentStageBet
        players[0].add(9, 0); // currentRoundBet
        players[0].add(10, 0); // isAutoAllinOnBlind
        players[0].add(11, -1); // placeOnTableFinished, -1 = ingame
        players[0].add(12, false); // check this stage, true = active
        players[0].add(13, -1); //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0

        players[1] = new ArrayList(13) {};
        players[1].add(0, 100); // string userId
        players[1].add(1, 1); // string user firstname + lastname
        players[1].add(2, 1);
        players[1].add(3, 20000); //chipsQuantity
        players[1].add(4, TRUE_TEXT); // isActive for reg/unreg
        players[1].add(5, false); // FoldFlag
        players[1].add(6, false); // AllinFlag manual
        players[1].add(7, FALSE_TEXT); // isChipleader?
        players[1].add(8, 0); // currentStageBet
        players[1].add(9, 0); // currentRoundBet
        players[1].add(10, 0); // isAutoAllinOnBlind
        players[1].add(11, -1); // placeOnTableFinished, -1 = ingame
        players[1].add(12, false); // check this stage, true = active
        players[1].add(13, -1); //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0
        return players;
    }

    private InlineKeyboardMarkup makeButtons() {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton buttonFoldPass = new InlineKeyboardButton();
        InlineKeyboardButton buttonCheckCall = new InlineKeyboardButton();
        //InlineKeyboardButton buttonBet = new InlineKeyboardButton();
        InlineKeyboardButton buttonBetX1 = new InlineKeyboardButton();
        InlineKeyboardButton buttonBetX2 = new InlineKeyboardButton();
        InlineKeyboardButton buttonBetX3 = new InlineKeyboardButton();
        InlineKeyboardButton buttonBetX4 = new InlineKeyboardButton();
        InlineKeyboardButton buttonBetAllin = new InlineKeyboardButton();


        minBetSize = bigBlindSize;
        buttonCheckCall.setText("check/call");
        buttonFoldPass.setText("fold/pass");
        buttonCheckCall.setCallbackData("checkcall");
        buttonFoldPass.setCallbackData("fold");
        buttonBetX1.setText("bet " + minBetSize);
        buttonBetX2.setText("bet " + minBetSize * 2);
        buttonBetX3.setText("bet " + minBetSize * 3);
        buttonBetX4.setText("bet " + minBetSize * 4);
        buttonBetAllin.setText("\uD83D\uDD34 all-in");
        buttonBetX1.setCallbackData("bet " + minBetSize);
        buttonBetX2.setCallbackData("bet " + minBetSize * 2);
        buttonBetX3.setCallbackData("bet " + minBetSize * 3);
        buttonBetX4.setCallbackData("bet " + minBetSize * 4);
        buttonBetAllin.setCallbackData("allin " + players[ moveCount % players.length ].get(3));
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(buttonFoldPass);
        keyboardButtonsRow1.add(buttonCheckCall);
        keyboardButtonsRow2.add(buttonBetX1);
        keyboardButtonsRow2.add(buttonBetX2);
        keyboardButtonsRow2.add(buttonBetX3);
        keyboardButtonsRow2.add(buttonBetX4);
        keyboardButtonsRow2.add(buttonBetAllin);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public void messageSendStickerToPlayingRoom() {
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

    public void messageSendToPlayingRoom(String text) {
        try {
            message = new SendMessage();
            message.setChatId(PLAYING_ROOM_ID);
            message.setText(text);
            execute(message);
            Thread.sleep(SLEEP_MS);

        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: " + e.getMessage() + NEXTLINE + "message: " + message.getText());
        }
    }

    public Integer messageSendToPlayingRoomAndGetMessageId(String text) {
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

    public Integer messageSendToPlayingRoomWithButtonsAndGetMessageId(String text) {
        int messageId = -1;
        try {
            message.setChatId(PLAYING_ROOM_ID);
            message.setText(text);
            message.setReplyMarkup(makeButtons());

            messageId = execute(message).getMessageId();
            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message.getText());
        }

        return messageId;
    }

    public boolean messageSendToPlayer(String userId, String text) {
        try {
            message = new SendMessage();
            message.setChatId(userId);
            message.setText(text);
            execute(message);
            //Thread.sleep(SLEEP_MS);
            return true;
            //System.out.println("ok: " + message);
        } catch (TelegramApiException e) {
            //e.printStackTrace();
            System.out.println("exception: " + e.getMessage() + NEXTLINE + "message: " + message.getText());
            return false;
        }
    }

    public void messageSendToPlayerBack(String userId, String text) {
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

    public void messageDelete(Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        try {
            deleteMessage.setChatId(PLAYING_ROOM_ID);
            deleteMessage.setMessageId(messageId);
            execute(deleteMessage);

            deleteMessage.setChatId(userId);
            deleteMessage.setMessageId(messageId);
            execute(deleteMessage);

            Thread.sleep(SLEEP_MS);
        } catch (TelegramApiException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + message.getText());
        }
    }

    public void messageEdit(String newText, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        msg.setChatId(PLAYING_ROOM_ID);
        msg.setMessageId(messageId);
        String currentText = msg.getText();

        if (currentText!=null && currentText.length()>0) {
            if (!currentText.equals(newText)) {
                try {
                    msg.setText(newText);
                    //msg.setText(text);
                    execute(msg);
                    //Thread.sleep(SLEEP_MS);
                } catch (TelegramApiException | NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
                }
            } else {
                System.out.println("editMessage tried edit same message");
            }
        }
        System.out.println("editMessage msg.getText(); == null");
    }

    public void messageEditInc(String newText, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        msg.setChatId(PLAYING_ROOM_ID);
        msg.setMessageId(messageId);
        String currentText = msg.getText();
        if (currentText!=null && currentText.length()>0) {
            if (!currentText.equals(newText)) {

                msg.setMessageId(messageId);
                //String currentText = msg.getText();
                msg.setText(currentText + NEXTLINE + newText);
                try {
                    execute(msg);
                } catch (TelegramApiException | NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
                }
            } else {
                System.out.println("messageEditInc tried edit same message");
            }

            System.out.println("messageEditInc msg.getText(); == null");
        }
    }


    public void messageEditIncWithButtons(String text, Integer messageId) {
        EditMessageText msg = new EditMessageText();

        msg.setMessageId(messageId);
        String currentText = msg.getText();
        try {
            msg.setChatId(PLAYING_ROOM_ID);
            msg.setText(currentText + text);
            msg.setReplyMarkup(makeButtons());
            execute(msg);
            //Thread.sleep(SLEEP_MS);
            //return currentText;
        } catch (TelegramApiException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
        }
    }

    public void messageEditWithInterface(String newText, Integer messageId) {
        EditMessageText msg = new EditMessageText();
        msg.setChatId(PLAYING_ROOM_ID);
        msg.setMessageId(messageId);
                try {
                    msg.setText(newText);
                    msg.setReplyMarkup(makeButtons());
                    execute(msg);
                    Thread.sleep(SLEEP_MS);
                } catch (TelegramApiException | NumberFormatException | InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("exception: messageId = " + messageId + "; message: " + msg.getText());
                    System.out.println(NEXTLINE);
                }
        }




    public Integer iterateMove(ArrayList[] players, Integer moveCount) {
        System.out.println(NEXTLINE + players[ moveCount % players.length ].get(2) + " made bet");
        moveCount = moveCount + 1;
        return moveCount;
    }

    public boolean switchStage(Integer moveCount, ArrayList[] players) {
        moveCount  = checkPlayerAllinOrFold(moveCount);

        if ((players.length - PlayerUtils.checkFoldCount(players)) == 1) {
            System.out.println("1 player left, and he is a winner");
            currentPlayerStage = 4;

        } else if (moveCount >= players.length) {
            if (PlayerUtils.checkBetsEqual(players)) {
                if (currentPlayerStage > -1 && currentPlayerStage < 4) {
                    System.out.println(playerStages[ currentPlayerStage ] + " stage rdy");
                    return true;
                } else if (currentPlayerStage == 4) {

                }
            }
        } else {
            System.out.println(playerStages[ currentPlayerStage ] + " stage now: movecount - " + moveCount + "; " +
                               "checkBetsEqual - " + PlayerUtils.checkBetsEqual(players));
        }
        return false;
    }

    public Integer extractMove(Update update, Integer moveCount) {
        String extractMove = update.getCallbackQuery().getData().toLowerCase().replaceAll(COMMAND_REGEXP, "$1");
        //System.out.println("extractMove start");
        //System.out.println("currentPlayerMoveChoice " + currentPlayerNumberChoice + "; currentPlayerNumberChoice: "
        // + currentPlayerNumberChoice);
        // public void extractMove(extract)

        switch (extractMove) {
            case "fold" -> {
                ;
                currentPlayerMoveChoice = 0;

                if (currentPlayerMoveChoice == 0) { //fold
                    //set fold
                    // players[  moveCount % players.length  ].set(5, true); //get(5)

                    setPlayerStageFlag(players, moveCount % players.length, "fold flag");
                    return currentPlayerMoveChoice;
                }
            }
            case "checkcall" -> {

                currentPlayerMoveChoice = 1;

                if (currentPlayerMoveChoice == 1) { // check/call
                    //check
                    if (PlayerUtils.checkBetsEqual(players)) {
                        //System.out.println("check success - PlayerUtils.checkBetsEqual(players) == " + PlayerUtils.checkBetsEqual(players));
                        setPlayerStageFlag(players, moveCount % players.length, "check flag");

                    }

                    //call
                    else if (PlayerUtils.findMaxBet(players) > Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) &&
                             PlayerUtils.findMaxBet(players) <= Integer.parseInt(players[ moveCount % players.length ].get(3).toString())) {
                        currentPlayerNumberChoice = PlayerUtils.findMaxBet(players) - Integer.parseInt(players[ moveCount % players.length ].get(9).toString());

                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player

                        currentPot += currentPlayerNumberChoice;
                        System.out.println("call success; added chips " + currentPlayerNumberChoice);
                        setPlayerStageFlag(players, moveCount % players.length, "check flag");
                    }

                    return currentPlayerMoveChoice;
                    //check current bet = highest or equal
                }
            }
            case "bet" -> {

                currentPlayerMoveChoice = 2;

                if (currentPlayerMoveChoice == 2) { //bet
                    if (currentPlayerNumberChoice <= 0) {
                        //currentPlayerNumberChoice = minBetSize;
                    }
                    if (raiseCount < 4 || players.length < 3) {

                        if (currentPlayerNumberChoice > PlayerUtils.findMaxBet(players) * 2) { // ()TODO && > max bet * 2
                            //refresh buttons
                        }
                        currentPlayerNumberChoice =
                                Integer.parseInt(update.getCallbackQuery().getData().toLowerCase().replaceAll(BET_REGEXP, "$3"));
                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player
                        currentPot += currentPlayerNumberChoice;
                        raiseCount++;
                        return currentPlayerMoveChoice;

                    }
                }
            }
            case "allin" -> {

                currentPlayerMoveChoice = 3;

                if (currentPlayerMoveChoice == 3) { //allin
                    if (raiseCount < 4 || players.length < 3) {
                        //refresh buttons
                        currentPlayerNumberChoice = Integer.parseInt(players[ moveCount % players.length ].get(3).toString());
                        players[ moveCount % players.length ].set(3, Integer.parseInt(players[ moveCount % players.length ].get(3).toString()) - currentPlayerNumberChoice); //current chips at player
                        players[ moveCount % players.length ].set(9, Integer.parseInt(players[ moveCount % players.length ].get(9).toString()) + currentPlayerNumberChoice); // current bet at player
                        currentPot += currentPlayerNumberChoice;
                        setPlayerStageFlag(players, moveCount % players.length, "allin flag");
                        raiseCount++;
                        return currentPlayerMoveChoice;
                    } else {
                        System.out.println("raisecount >= 4, only call or fold is a option");
                    }

                }
            }
            default -> {
                //return currentPlayerMoveChoice;
            }

        }
        ;

//        if (currentPlayerMoveChoice == -1) {
//            sendToPlayer(players[moveCount].get(0).toString(),"to make bet choose 1 action from 1st menu row (if a choose bet, also push 1 button on 2nd row");
//        }
//        System.out.println("extractMove finish");
        return currentPlayerMoveChoice;
    }

    private void inlineResultDeck(Update update, ArrayList playersCard[][], ArrayList players[], Integer dealNumber) {
        if (update.hasInlineQuery()) {
            for (int i = 0; i < players.length; i++) {
                if (update.getInlineQuery().getFrom().getId().toString().equals(players[ i ].get(0).toString())) {
                    AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
                    List<InlineQueryResult> resultsList = new ArrayList<>();
                    InlineQueryResultArticle article = new InlineQueryResultArticle();
                    InputTextMessageContent messageContent = new InputTextMessageContent();


                    String hand = (playersCard[ i ][ 0 ].get(0).toString() + playersCard[ i ][ 1 ].get(0).toString());
                    String chips = players[i].get(3).toString();

                    String name = players[i].get(1).toString();



                    messageContent.setMessageText(AUTODELETE_TEXT);
                    article.setInputMessageContent(messageContent);
                    article.setId(Integer.toString(0));
                    article.setTitle("Deal №" + dealNumber);
                    article.setDescription("Your hand: " + hand);
                    resultsList.add(article);

/*
                    messageContent.setMessageText(AUTODELETE_TEXT+"ZXC");
                    article.setInputMessageContent(messageContent);
                    article.setId(Integer.toString(1));
                    article.setTitle(name);
                    article.setDescription("chips: " + chips);
                    resultsList.add(article);
*/



                    answerInlineQuery.setCacheTime(0);
                    answerInlineQuery.setInlineQueryId(update.getInlineQuery().getId());
                    answerInlineQuery.setIsPersonal(true);
                    answerInlineQuery.setResults(resultsList);

                    try {
                        execute(answerInlineQuery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Integer checkPlayerAllinOrFold(Integer moveCount) {
        if (Boolean.parseBoolean(players[ moveCount % players.length ].get(5).toString()) || Boolean.parseBoolean(players[ moveCount % players.length ].get(6).toString())) {
            System.out.println("found fold or allin at " + players[ moveCount % players.length ].get(2));
            moveCount = iterateMove(players, moveCount);
        }
        return moveCount;
    }

    public Integer currentStageTableText(Update update, String userIdCallback, Integer moveCount) {

        if (stageStarted) {

            moveCount    = moveCount % players.length;
            stageStarted = false;
        }

        moveCount = checkPlayerAllinOrFold(moveCount);

        if (checkPlayerCallback(update, userIdCallback, moveCount)) {

            moveCount = iterateMove(players, moveCount);

        }

        if (switchStage(moveCount, players)) {

            currentPlayerStage++;
            moveAllPlayerBetFromStageToRound(players);
            unsetAllPlayerStageFlag(players, "check flag");
            tableCards = getCurrentStageTableCardsText(currentPlayerStage, cardDeck);
            stageStarted = true;

        }

        //messageEditWithButtons(getRoundAnnounceText(players) + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)+ DOUBLE_NEXTLINE +  tableCards, currentDealMessageId);

        return moveCount;

    }
    
    public  String dealCardsCombosText(ArrayList[] players, ArrayList<String>[][] playerCards, Integer HAND_CARDS_COUNT, List<String> cardDeck) {
        // text                  = EMPTY_STRING;
        currentPlayerHandText = EMPTY_STRING;
        allHandsText          = EMPTY_STRING;
        for (int playerNumber = 0; playerNumber < players.length; playerNumber++) {

            StringBuilder hand = new StringBuilder();
            for (int playerCardNumber = 0; playerCardNumber < HAND_CARDS_COUNT; playerCardNumber++) {
                hand.append(playerCards[ playerNumber ][ playerCardNumber ].get(0));
            }

            currentPlayerHandText = playerNumber + " hand: " + hand + getCurrentStageTableCardsTextTest(4, cardDeck) + NEXTLINE;
            //sendToPlayer(players[ y ].get(0).toString(), "dealNumber " + dealNumber + NEXT_LINE + "Your hand: " + currentPlayerHandText);
//            sendToPlayer(players[ playerNumber ].get(0).toString(), "deal " + dealNumber + DOT + NEXT_LINE + hand +
//                                                                    " your cards at dealNumber" + dealNumber + DOT + NEXT_LINE); // + currentPlayerHandText);
            allHandsText = allHandsText + currentPlayerHandText;

        }
        return allHandsText;
    }

    public  String getCurrentStageTableCardsTextTest(Integer currentPlayerStage, List<String> cardDeck) {
        String currentTableCards = EMPTY_STRING;

        flopCards  = DeckUtils.stageFlop(players.length, HAND_CARDS_COUNT, cardDeck);
        turnCards  = flopCards + DeckUtils.stageTurn(players.length, HAND_CARDS_COUNT, cardDeck);
        riverCards = turnCards + DeckUtils.stageRiver(players.length, HAND_CARDS_COUNT, cardDeck);

        return riverCards;
    }
    
    public String getCurrentStageTableCardsText(Integer currentPlayerStage, List<String> cardDeck) {
        String currentTableCards = EMPTY_STRING;

        if (currentPlayerStage > -1) {
            flopCards  = DeckUtils.stageFlop(players.length, HAND_CARDS_COUNT, cardDeck);
            turnCards  = flopCards + DeckUtils.stageTurn(players.length, HAND_CARDS_COUNT, cardDeck);
            riverCards = turnCards + DeckUtils.stageRiver(players.length, HAND_CARDS_COUNT, cardDeck);
            if (currentPlayerStage == 0) {
                currentTableCards = "preflop";
            } else if (currentPlayerStage == 1) {
                currentTableCards = PHASE_FLOP_TEXT + NEXTLINE + flopCards;
            } else if (currentPlayerStage == 2) {
                currentTableCards = PHASE_TURN_TEXT + NEXTLINE + turnCards;
            } else if (currentPlayerStage >= 3) {
                currentTableCards = PHASE_RIVER_TEXT + NEXTLINE + riverCards;
            }
        }
        return currentTableCards;
    }
    
    public boolean checkPlayerCallback(Update update, String userIdCallback, Integer moveCount) {
        boolean res = false;
        if (update.hasCallbackQuery()) {
            // if (userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString())) {
            if (true) {
                if (extractMove(update, moveCount) >= 0) {
                    res = true;
                    return res;
                }

//                editMessageWithButtons(getRoundAnnounceString(players) + DOUBLE_NEXTLINE + DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2)
//                                       + DOUBLE_NEXTLINE + tableCards + DOUBLE_NEXTLINE + "press @pokedgram_bot to view cards", currentDealMessageId);
            } else if (!userIdCallback.toString().equals(players[ moveCount % players.length ].get(0).toString()) && !userIdCallback.equals("0")) {
                messageSendToPlayer(userIdCallback, "userId " + userIdCallback + " not matched " + players[ moveCount % players.length ].get(0) + NEXTLINE + "expecting move from player" + players[ moveCount % players.length ].get(0) + " (" + players[ moveCount % players.length ].get(1) + ")" + NEXTLINE);
            } else if (userIdCallback.equals("0")) {
                System.out.println("userIdCallback = " + userIdCallback);
            }
            //userIdCallback = "0";
            // }

        }
        return res;
    }




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

    String userIdMessage = EMPTY_STRING;
    String userIdCallback = EMPTY_STRING;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            System.out.println("onUpdateReceived callback usrtext = " + userIdMessage + ", usrcallback = " + userIdCallback);

            userIdCallback = String.valueOf(update.getCallbackQuery().getFrom().getId());
            userIdMessage  = "0";
        }

        if (update.hasMessage()) {
            System.out.println("onUpdateReceived msg usrtext = " + userIdMessage + ", usrcallback = " + userIdCallback);

            if (update.hasMessage() && update.getMessage().getText().matches(AUTODELETE_REGEXP)) {
                messageDelete(update.getMessage().getMessageId());
            }


            userName      = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            userId        = String.valueOf(update.getMessage().getFrom().getId());
            nickName      = update.getMessage().getFrom().getUserName();
            userIdMessage = update.getMessage().getFrom().getId().toString();

            System.out.println("got update with text, from userName: " + userName + "(userId " + userId + ")");
            userIdCallback = "0";

        }

        if (update.hasMessage() && update.getMessage().hasText() && currentPlayerStage == -2) {
            if (!gameStarted) {
                if (!preGameStarted) {

                    String extractCommand, arg;
                    extractCommand = update.getMessage().getText().replaceAll(COMMAND_REGEXP, "$1");
                    if (extractCommand.length() < update.getMessage().getText().length()) {
                        try {
                            arg = update.getMessage().getText().replaceAll(ARGUMENT_REGEXP, "$2");
                            if (arg.length() > 0) {
                                System.out.println("arg = " + arg + NEXTLINE + "extractCommand = " + extractCommand);
                                extractNumber = Integer.parseInt(arg);
                            } else {
                                extractNumber = 0;
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    }
                    switchPregame(extractCommand, extractNumber, update);


                    while (preGameStarted && !gameStarted) {
                        int tryCount = 0;
                        int buttonId;
                        cardDeck = initializeCardsDeck();

                        cardDeck = shuffleCardsDeck(cardDeck);
                        tryCount++;
                        Integer preGameMessageId = messageSendToPlayingRoomAndGetMessageId(DECK_SHUFFLE_TEXT);
                        ArrayList<?>[][] playerCards = DeckUtils.dealCardsFromDeck(maxPlayers, 1, cardDeck);
                        allHandsText = dealCardsText(playersQueue, playerCards, 1);

                        if (ROLL_VISIBILITY) {
                            messageEdit(DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + allHandsText, preGameMessageId);
                        } else {
                            messageEdit(DECK_SHUFFLE_TEXT + NEXTLINE + CARDS_DEALED_TEXT + DOUBLE_NEXTLINE, preGameMessageId);
                        }


                        buttonId = DeckUtils.rankRoll(maxPlayers, playerCards);


                        if (buttonId == -1) {
                            messageEdit(DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "We've equal highest, redraw (trycount " + tryCount + ")", preGameMessageId); //+ DOUBLE_NEXTLINE + "Deck info: " + deck, rollMessageId);
                            gameStarted = false;
                        } else {
                            messageEdit(DECK_SHUFFLE_TEXT + DOUBLE_NEXTLINE + allHandsText + DOUBLE_NEXTLINE + "@" + playersQueue[ buttonId ].get(1).toString() + " starting on the button", preGameMessageId); // + TRIPLE_NEXT_LINE + "Deck info: " + deck, rollMessageId);
                            gameStarted        = true;
                            players            = playersQueue;
                            players            = PlayerUtils.shiftPlayerOrder(players, buttonId);
                            currentPlayerStage = -1;
                            userIdMessage      = "0";
                            userIdCallback     = "0";
                            break;

                            //TODO delete pregame commands and add game commands promt
                            //TODO create comfortable UX to make moves (inline/buttons/etc)
                        }
                        //} return;

                    } //pregame started (reg finished). works fine
                    // if (update.hasMessage() && update.getMessage().hasText() && gameStarted) {

                }
            }
        }
        if (currentPlayerStage > -2) {

            if (currentPlayerStage == -1) {
                dealNumber++;
                if (dealNumber == 1) {
                    cardDeck = initializeCardsDeck();
                } else {
                    players = PlayerUtils.shiftPlayerOrder(players, 1);
                }

                boolean isSidePotExist = false;
                currentPot = 0;
                moveCount  = 0;
                raiseCount = 0;
                tableCards = EMPTY_STRING;
                smallBlindSize = ((dealNumber / BLIND_INCREASE_RATE_DEALS) + 1) * START_SMALL_BLIND_SIZE;
                bigBlindSize   = smallBlindSize * 2;



                if (players.length == 2) {
                    smallBlindId = 0;
                    bigBlindId   = smallBlindId + 1;

                    players[ smallBlindId ].set(3, Integer.parseInt(players[ smallBlindId ].get(3).toString()) - smallBlindSize); //current chips at player
                    players[ smallBlindId ].set(9, Integer.parseInt(players[ smallBlindId ].get(9).toString()) + smallBlindSize); // current
                    // bet at player
                    players[ bigBlindId ].set(3, Integer.parseInt(players[ bigBlindId ].get(3).toString()) - bigBlindSize); //current chips at player
                    players[ bigBlindId ].set(9, Integer.parseInt(players[ bigBlindId ].get(9).toString()) + bigBlindSize); // current bet at player

                    currentPot += Integer.parseInt(players[ smallBlindId ].get(9).toString());
                    currentPot += Integer.parseInt(players[ bigBlindId ].get(9).toString());

                    //public Integer betBlindsToPot() {}

                }

                String blindsInfo = "deal number: " + dealNumber + DOUBLE_NEXTLINE + "\uD83D\uDD35 small blind @ " + players[ (moveCount) % players.length ].get(1) + NEXTLINE + "\uD83D\uDFE1 big blind @ " + players[ (moveCount + 1) % players.length ].get(1);
                dealOrderMsg = messageSendToPlayingRoomAndGetMessageId(blindsInfo);

                cardDeck     = shuffleCardsDeck(cardDeck);
                playerCards  = DeckUtils.dealCardsFromDeck(players.length, HAND_CARDS_COUNT, cardDeck);
                allHandsText = dealCardsText(playersQueue, playerCards, HAND_CARDS_COUNT);

                currentDealMessageId = messageSendToPlayingRoomWithButtonsAndGetMessageId(
                        getRoundAnnounceText(players) + DOUBLE_NEXTLINE +
                        CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE +
                        "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2) + DOUBLE_NEXTLINE +
                        tableCards + DOUBLE_NEXTLINE
                );

                currentPlayerMoveChoice = 0;
                currentPlayerStage      = 0;
                //currentPlayerNumberChoice = bigBlindSize;
            }
            if (currentPlayerStage >= 0 && currentPlayerStage <= 4) { //preflop - river


                //TODO fix minBetSize logic
                minBetSize = PlayerUtils.findMaxBet(players);
                if (minBetSize == 0) {
                    minBetSize = bigBlindSize;
                }




                System.out.println("gameStarted; cardsDealt == true; userIdCallback = " + userIdCallback);
                if (currentPlayerStage >= 0) {
                    inlineResultDeck(update, playerCards, players, dealNumber);
                }
                //preflop flop turn river
                if (currentPlayerStage >= 0 && currentPlayerStage <= 3) {

                    tableCards = getCurrentStageTableCardsText(currentPlayerStage, cardDeck);
                    moveCount = currentStageTableText(update, userIdCallback, moveCount);
                  //  if (checkSameStage != currentPlayerStage) {
                        messageEditWithInterface(getRoundAnnounceText(players) + DOUBLE_NEXTLINE + CURRENT_POT_TEXT + currentPot + DOUBLE_NEXTLINE + "current move " + moveCount + ": " + players[ moveCount % players.length ].get(2) + DOUBLE_NEXTLINE + tableCards + TRIPLE_NEXTLINE, currentDealMessageId);
                   //     checkSameStage = currentPlayerStage;
                   // }

                }

                //showdown
                if (currentPlayerStage == 4) {
//                String resultCards = EMPTY_STRING;
//                String resultCombo = EMPTY_STRING;
                String res = EMPTY_STRING;
                    System.out.println("currentPlayerStage 4 - showdown");


                    getShowdownWinnerId(players, playerCards, cardDeck);


                    System.out.println("final results: (//str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0)");
                    for (Integer y = 0; y < players.length; y++) {
                        System.out.println("player" +y + ": " + players[y].get(13));
                        //resultCombo += "player" +y + ": " + players[y].get(13) + NEXT_LINE;
                        ArrayList[] currentPlayerCards = getPlayerCardsWithTable(moveCount,playerCards,players);
                        //resultCards += String.valueOf(Arrays.stream(currentPlayerCards).toList()) + NEXT_LINE;
                        res += "player" + y + ": " + players[y].get(13) + currentPlayerCards[0].stream().toString() + currentPlayerCards[1].stream().toString() + NEXTLINE;
                    }

                    //messageSendToPlayingRoom(resultCards + NEXT_LINE + resultCombo);

                    messageSendToPlayingRoom(res);

                    //userIdMessage = "0";
                    currentPlayerMoveChoice = -1;
                    currentPlayerStage      = -1;
                    currentPot              = 0;
                    smallBlindSize          = 0;
                    bigBlindSize            = 0;
                    unsetAllPlayerBetFromStageToRound(players);
                    unsetAllPlayerStageFlag(players, "clean all");
                    }

                }

/*                try {
                    Thread.sleep(SLEEP_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        update = null;
        //userIdCallback = "0";
        //userIdMessage  = "0";
        System.out.println("onUpdate end" + nickName);

        System.out.println(NEXTLINE);
        }
}


