package org.pokedgram;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.pokedgram.SuperStrings.*;


public class DeckUtils extends PokedgramBot {
    String flopCards, turnCards, riverCards = EMPTY_STRING;

    public static List<String> initializeCardsDeck() {

        List<String> cardDeck = new ArrayList<>(52);
        String[] cardSuits = new String[]{"♣", "♦", "♥", "♠"}; //String[] cardSuits = new String[]{"♧", "♢", "♡", "♤"};
        String[] cardRanks = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        //Integer[] cardRanksValue = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; //♠♥♦♣♤♡♢♧
        //Integer[] cardRanksValue = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};

        //System.out.println(List.of(cardSuits) + NEXT_LINE + List.of(cardRanks));

        for (String suit : cardSuits) {
            for (String rank : cardRanks) {
                cardDeck.add(rank + suit);
            }
        }
        return cardDeck;
    }

    public static List<String> shuffleCardsDeck(List<String> currentDeck) {
        Collections.shuffle(currentDeck);
        System.out.println("deck shuffled: " + currentDeck);
        //System.out.println("deck shuffled.");
        return currentDeck;
    }

    //static HashMap<String, String>[][] dealCardsFromDeck(int playersQuantity, int playersCardsCount, List<String> deck) {
        static ArrayList<String>[][] dealCardsFromDeck(int playersQuantity, int playersCardsCount, List<String> deck) {

            ArrayList<String>[][] playersHand = new ArrayList[ playersQuantity ][ playersCardsCount + 5 ];

            //deal cards to players
            for (int iterateCard = 0; iterateCard < playersCardsCount; iterateCard++) { // deal card loop
                for (int iteratePlayer = 0; iteratePlayer < playersQuantity; iteratePlayer++) { //select player to deal card
                    playersHand[ iteratePlayer ][ iterateCard ] = new ArrayList<>() {
                    };
                    playersHand[ iteratePlayer ][ iterateCard ].add(0, deck.get((iteratePlayer + (iterateCard * playersQuantity)))); // 0 string
                    playersHand[ iteratePlayer ][ iterateCard ].add(1, playersHand[ iteratePlayer ][ iterateCard ].get(0).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING)); // 1 suit
                    playersHand[ iteratePlayer ][ iterateCard ].add(2, playersHand[ iteratePlayer ][ iterateCard ].get(0).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING)); // 2 rank
                    playersHand[ iteratePlayer ][ iterateCard ].add(3, String.valueOf(cardValue(playersHand[ iteratePlayer ][ iterateCard ].get(2)))); // 3 value

                    //System.out.println("Player " + (y) + " card: " + deck.get((y + (i * playersQuantity))));

                    //put table cards after players cards for easier calculacting
                    // TODO ref-cktor
                    if (iterateCard == 1) {
                        playersHand[ iteratePlayer ][ 2 ] = new ArrayList<>() {};
                        playersHand[ iteratePlayer ][ 3 ] = new ArrayList<>() {};
                        playersHand[ iteratePlayer ][ 4 ] = new ArrayList<>() {};
                        playersHand[ iteratePlayer ][ 5 ] = new ArrayList<>() {};
                        playersHand[ iteratePlayer ][ 6 ] = new ArrayList<>() {};

                        playersHand[ iteratePlayer ][ 2 ].add(0, deck.get((playersQuantity * playersCardsCount + 3)));
                        playersHand[ iteratePlayer ][ 2 ].add(1, deck.get((playersQuantity * playersCardsCount + 3)).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 2 ].add(2, deck.get((playersQuantity * playersCardsCount + 3)).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 2 ].add(3, String.valueOf(cardValue(playersHand[ iteratePlayer ][ 2 ].get(2))));

                        playersHand[ iteratePlayer ][ 3 ].add(0, deck.get((playersQuantity * playersCardsCount + 3 + 1)));
                        playersHand[ iteratePlayer ][ 3 ].add(1, deck.get((playersQuantity * playersCardsCount + 3 + 1)).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 3 ].add(2, deck.get((playersQuantity * playersCardsCount + 3 + 1)).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 3 ].add(3, String.valueOf(cardValue(playersHand[ iteratePlayer ][ 3 ].get(2))));


                        playersHand[ iteratePlayer ][ 4 ].add(0, deck.get((playersQuantity * playersCardsCount + 3 + 2)));
                        playersHand[ iteratePlayer ][ 4 ].add(1, deck.get((playersQuantity * playersCardsCount + 3 + 2)).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 4 ].add(2, deck.get((playersQuantity * playersCardsCount + 3 + 2)).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 4 ].add(3, String.valueOf(cardValue(playersHand[ iteratePlayer ][ 4 ].get(2))));

                        playersHand[ iteratePlayer ][ 5 ].add(stageTurn(2, 2, deck));
                        playersHand[ iteratePlayer ][ 5 ].add(1, stageTurn(2, 2, deck).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 5 ].add(2, stageTurn(2, 2, deck).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 5 ].add(3, String.valueOf(cardValue(playersHand[ iteratePlayer ][ 5 ].get(2))));

                        playersHand[ iteratePlayer ][ 6 ].add(stageRiver(2, 2, deck));
                        playersHand[ iteratePlayer ][ 6 ].add(1, stageRiver(2, 2, deck).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 6 ].add(2, stageRiver(2, 2, deck).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                        playersHand[ iteratePlayer ][ 6 ].add(3, String.valueOf(cardValue(playersHand[ iteratePlayer ][ 6 ].get(2))));
                    }

                }
            }

            return playersHand;
        }


        /*
        HashMap<String, String>[][] playersHand = new HashMap[ playersQuantity ][ playersCardsCount + 5 ];

        //deal cards to players
        for (int iterateCard = 0; iterateCard < playersCardsCount; iterateCard++) { // deal card loop
            for (int iteratePlayer = 0; iteratePlayer < playersQuantity; iteratePlayer++) { //select player to deal card
                playersHand[ iteratePlayer ][ iterateCard ] = new HashMap<String, String>() {};
                playersHand[ iteratePlayer ][ iterateCard ].put("card text", deck.get((iteratePlayer + (iterateCard * playersQuantity)))); // 0 string
                playersHand[ iteratePlayer ][ iterateCard ].put("card suit", playersHand[ iteratePlayer ][ iterateCard ].get("card text").toString().replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING)); // 1 suit
                playersHand[ iteratePlayer ][ iterateCard ].put("card rank", playersHand[ iteratePlayer ][ iterateCard ].get("card text").toString().replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING)); // 2 rank
                playersHand[ iteratePlayer ][ iterateCard ].put("card value", String.valueOf(cardValue(playersHand[ iteratePlayer ][ iterateCard ].get("card rank").toString()))); // 3 value

                //System.out.println("Player " + (y) + " card: " + deck.get((y + (i * playersQuantity))));

                //put table cards after players cards for easier calculacting
                // TODO ref-cktor
                if (iterateCard == 1) {
                    playersHand[ iteratePlayer ][ 2 ] = new HashMap<String, String>() {};
                    playersHand[ iteratePlayer ][ 3 ] = new HashMap<String, String>() {};
                    playersHand[ iteratePlayer ][ 4 ] = new HashMap<String, String>() {};
                    playersHand[ iteratePlayer ][ 5 ] = new HashMap<String, String>() {};
                    playersHand[ iteratePlayer ][ 6 ] = new HashMap<String, String>() {};

                    playersHand[ iteratePlayer ][ 2 ].put("card text", deck.get((playersQuantity * playersCardsCount + 3)));
                    playersHand[ iteratePlayer ][ 2 ].put("card suit", deck.get((playersQuantity * playersCardsCount + 3)).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 2 ].put("card rank", deck.get((playersQuantity * playersCardsCount + 3)).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 2 ].put("card value", String.valueOf(cardValue(playersHand[ iteratePlayer ][ 2 ].get("card rank").toString())));

                    playersHand[ iteratePlayer ][ 3 ].put("card text", deck.get((playersQuantity * playersCardsCount + 3 + 1)));
                    playersHand[ iteratePlayer ][ 3 ].put("card suit", deck.get((playersQuantity * playersCardsCount + 3 + 1)).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 3 ].put("card rank", deck.get((playersQuantity * playersCardsCount + 3 + 1)).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 3 ].put("card value", String.valueOf(cardValue(playersHand[ iteratePlayer ][ 3 ].get("card rank").toString())));


                    playersHand[ iteratePlayer ][ 4 ].put("card text", deck.get((playersQuantity * playersCardsCount + 3 + 2)));
                    playersHand[ iteratePlayer ][ 4 ].put("card suit", deck.get((playersQuantity * playersCardsCount + 3 + 2)).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 4 ].put("card rank", deck.get((playersQuantity * playersCardsCount + 3 + 2)).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 4 ].put("card value", String.valueOf(cardValue(playersHand[ iteratePlayer ][ 4 ].get("card rank").toString())));

                    playersHand[ iteratePlayer ][ 5 ].put("card text", stageTurn(2, 2, deck).toString());
                    playersHand[ iteratePlayer ][ 5 ].put("card suit", stageTurn(2, 2, deck).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 5 ].put("card rank", stageTurn(2, 2, deck).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 5 ].put("card value", String.valueOf(cardValue(playersHand[ iteratePlayer ][ 5 ].get("card rank").toString())));

                    playersHand[ iteratePlayer ][ 6 ].put("card text", stageRiver(2, 2, deck));
                    playersHand[ iteratePlayer ][ 6 ].put("card suit", stageRiver(2, 2, deck).replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 6 ].put("card rank", stageRiver(2, 2, deck).replaceAll(FIND_CARDS_SUIT_REGEXP, EMPTY_STRING));
                    playersHand[ iteratePlayer ][ 6 ].put("card value", String.valueOf(cardValue(playersHand[ iteratePlayer ][ 6 ].get("card rank").toString())));
                }

            }
        }

        return playersHand;
    }*/

    public static int cardValue(String card) {
        int cardValue;
        if (Objects.equals(card, "A")) {
            cardValue = 14; // or 1? for str
        } else if (Objects.equals(card, "K")) {
            cardValue = 13;
        } else if (Objects.equals(card, "Q")) {
            cardValue = 12;
        } else if (Objects.equals(card, "J")) {
            cardValue = 11;
        } else {
            cardValue = Integer.parseInt(card);
        }
        return cardValue;
    }

    public static int getSplitRewardSize(int potSize, HashMap[] players) {
        System.out.println("getSplitRewardSize start");
        //int splitCount = 2;
        int playerReward = -1;
        if (potSize % 2 != 0) {
            potSize = potSize - 1;
            System.out.println("!!! if (potSize % 2 != 0) { potSize = potSize-1;");
        }
        if (potSize == 0) {
            playerReward = 0;
        } else {
            playerReward = potSize / players.length; // TODO fix 2players hardcode
        }

        System.out.println("potSize = " + potSize);
        System.out.println("playerReward = " + playerReward);

        System.out.println("getSplitRewardSize finish");
        return playerReward;
    }


    //ToDo() return Highest card with players id for counting winner
    static int checkFlash(HashMap[] players, ArrayList<String>[][] playersCards, ArrayList[] table) {
        ArrayList<Integer>[] matchedPlayers = new ArrayList[players.length];//{};
        String playersHandWithTable;
        int checkFlashCount = 0;
        int checkFlashSuit = -1;

        if (table[ 1 ].get(0).toString().matches(FIND_FLASH_DRAW_CLUBS_REGEXP)) {
            checkFlashSuit = 0;
        } else if (table[ 1 ].get(0).toString().matches(FIND_FLASH_DRAW_DIAMONDS_REGEXP)) {
            checkFlashSuit = 1;
        } else if (table[ 1 ].get(0).toString().matches(FIND_FLASH_DRAW_HEARTS_REGEXP)) {
            checkFlashSuit = 2;
        } else if (table[ 1 ].get(0).toString().matches(FIND_FLASH_DRAW_SPADES_REGEXP)) {
            checkFlashSuit = 3;
        }

        if (checkFlashSuit >= 0) {
            for (int iteratePlayer = 0; iteratePlayer < players.length; iteratePlayer++) {
                playersHandWithTable = playersCards[ iteratePlayer ][ 0 ].get(1).toString() + playersCards[ iteratePlayer ][ 1 ].get(1).toString() + table[ 1 ].get(0).toString();
                // 0 string;  1 suit; 2 rank; 3 value
                if (playersHandWithTable.matches(FIND_FLASH_REGEXP)) {
                    matchedPlayers[ checkFlashCount ] = new ArrayList();
                    matchedPlayers[ checkFlashCount ].add(iteratePlayer);
                    matchedPlayers[ checkFlashCount ].add(Integer.valueOf(players[ iteratePlayer ].get(0).toString()));
                    matchedPlayers[ checkFlashCount ].add(Integer.valueOf(playersHandWithTable));
                    players[ iteratePlayer ].put(COMBOPOWER_VALUE, 5);
                    checkFlashCount++;
                    System.out.println("checkFlash: " + iteratePlayer + ": " + ((playersCards[ iteratePlayer ][ 0 ].get(1).toString() + playersCards[ iteratePlayer ][ 1 ].get(1).toString() + table[ 1 ].get(0))));
                }
            }

            if (checkFlashCount > 1) {
                for (int matchedFlashCount = 0; matchedFlashCount <= checkFlashCount; matchedFlashCount++) {
                    matchedFlashCount = matchedFlashCount;
                    //playersCards[z][0].get(1) + playersCards[z][1].get(1);
                    //matchedPlayers[z].get(2);

                }
            }
        }


        return checkFlashCount;
    }

    static Integer checkStraight(ArrayList<String>[][] playersCards) {
        System.out.println("checkStraight not implemented");
        return -1;

    }

    static int checkDistinct(ArrayList<String>[][] playersCards, HashMap[] players) {
        System.out.println("checkDistinct: start");
        int countCombo = 0;
        int playerAndTableCards = 7;

        for (int iteratePlayer = 0; iteratePlayer < players.length; iteratePlayer++) {

            String[] playersDistinctCards = new String[ 3 ];
            int distinctQuantity = 0;
            //countCombo = 0;
            String excludeSingle = EMPTY_STRING;
            ArrayList<?>[] currentPlayerCards = getPlayerCardsWithTable(iteratePlayer, playersCards, players);
            List<Integer> cardValues = new ArrayList<>();

            System.out.println(NEXTLINE);
            System.out.println("player: " + iteratePlayer);

            for (int iterateAvailableCards = 0; iterateAvailableCards < playerAndTableCards; iterateAvailableCards++) {

                cardValues.add(Integer.parseInt(currentPlayerCards[ 3 ].get(iterateAvailableCards).toString()));
            }

            excludeSingle = cardValues.stream().sorted().collect(Collectors.groupingBy(Function.identity(),
                    Collectors.counting())).toString().replaceAll(EXTRACT_DISTINCT_VALUES_REGEXP, EMPTY_STRING);

            distinctQuantity = excludeSingle.replaceAll(EXTRACT_VALUES_DIRTY_REGEXP, EMPTY_STRING).length();
            System.out.println("rank = distinct: " + excludeSingle + "; distinctQuantity: " + distinctQuantity);//

            try {

                if (distinctQuantity > 0) {
                    countCombo++;
                    playersDistinctCards[ 0 ] = excludeSingle.replaceAll(EXTRACT_DISTINCT_COUNT_REGEXP, "$1");
                    if (playersDistinctCards[ 0 ].matches(FIND_4X_REGEXP)) {

                        System.out.println("matches " + FIND_4X_REGEXP);
                        players[ iteratePlayer ].put(COMBOPOWER_VALUE, 7);

                        //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4
                        // triple 3 //two pair 2 //pair 1 //high card 0

                        //playerComboGrade[playerid] = %regex to find combo value%
                        //playerKickerCards[playerid] = %check hand cards + different cases
                    } else if (playersDistinctCards[ 0 ].matches(FIND_2X_REGEXP) &&      //full
                            playersDistinctCards[ 0 ].matches(FIND_3X_REGEXP)) {
                            System.out.println(".matches(FIND_2X_REGEXP).matches(FIND_3X_REGEXP)");
                            players[ iteratePlayer ].put(COMBOPOWER_VALUE, 6);
                        } else if (playersDistinctCards[ 0 ].matches(FIND_3X_REGEXP)) {

                                System.out.println("matches " + FIND_3X_REGEXP);
                                players[ iteratePlayer ].put(COMBOPOWER_VALUE, 3);
                            } else if (playersDistinctCards[ 0 ].matches(FIND_2X2X_REGEXP)) {

                                    System.out.println("matches " + FIND_2X2X_REGEXP);
                                    players[ iteratePlayer ].put(COMBOPOWER_VALUE, 2);
                                } else if (playersDistinctCards[ 0 ].matches(FIND_2X_REGEXP)) {
                                        System.out.println("matches " + FIND_2X_REGEXP);

                                        players[ iteratePlayer ].put(COMBOPOWER_VALUE, 1);
                                    }

                } else if (distinctQuantity == 0) {

                    System.out.println("distinctQuantity==0 - no combo found");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("checkDistinct: end");
        }
        return countCombo;

    }

        //if (distinctQuantity==4) {} else
        //                        System.out.println("distinctQuantity==3 " + NEXTLINE + playersDistinctCards[ 0 ]);
        //                    } else if (distinctQuantity == 2) {
        //                        System.out.println("distinctQuantity==2 " + NEXTLINE + playersDistinctCards[ 0 ]);
        //                    } else if (distinctQuantity == 1) {
        //                        System.out.println("distinctQuantity==1 " + NEXTLINE + playersDistinctCards[ 0 ]);
        //                    }



    // else if check kicker
    // kicker = card(s) in players hand to break ties between hands of the same rank

    static int checkKicker(HashMap[] players, ArrayList<String>[][] playerCards, int comboRank) throws Exception { //get highest card, if highest quantity > 1, return -1

        int handHighest = -1;
        int winnerCount = 0;
        int winnerId = -1;


        if (comboRank == 1 || comboRank == 2 || comboRank == 3 || comboRank == 6 || comboRank == 7) { // check pairs rank, mb another for rank 6 (fullhosue)

        }

        if (comboRank == 5) { // flash, compare with suit first, (?) otherwise split (?)

            // ...
        }

        if (comboRank == 4 || comboRank == 8) { //str & strflash - as a straight

            // ...
        }

        //str fl 8+ //quad 7 //fullhouse 6 //flash 5 //straight 4+ //triple 3 //two pair 2 //pair 1 //high card 0
        //TODO if kickers same - split reward
        //TODO copypaste from coinflip, here need set stats in players[].(13,
        if (comboRank == 0) { // if highest card equal  compare second
            handHighest = -1;
            for (int iteratePlayer = 0; iteratePlayer < players.length; iteratePlayer++) {
                for (int iterateCard = 0; iterateCard < HAND_CARDS_COUNT; iterateCard++) {
                    if (handHighest < Integer.parseInt(playerCards[ iteratePlayer ][ iterateCard ].get(3))) {
                        handHighest = Integer.parseInt(playerCards[ iteratePlayer ][ iterateCard ].get(3));
                        winnerCount = 1;
                        winnerId    = iteratePlayer;
                        int cardValue = Integer.parseInt(playerCards[ iteratePlayer ][ iterateCard ].get(3));
                    } else if (handHighest == Integer.parseInt(playerCards[ iteratePlayer ][ iterateCard ].get(3))) {
                        winnerCount++;
                        winnerId = -1;
                    }
                }
            }



        }

        if (winnerId > -1) {
            if (winnerCount == 1) {
                return winnerId;
            }

            if (winnerCount == 2) {
                Exception E = new Exception("no kicker found, ");
                throw E;
                //return winnerId;
            }
        } else {
            return -1;
        }




        return winnerId;
    }


    /*static HashMap<String,String>[] getPlayerCardsWithTable(Integer moveCount, ArrayList<String>[][] playerCards, HashMap[] players) {

        HashMap<String,String>[] playerCardsWithTable = new HashMap[ 4 ];
        playerCardsWithTable[ 0 ] = new HashMap();
        playerCardsWithTable[ 1 ] = new HashMap();
        playerCardsWithTable[ 2 ] = new HashMap();
        playerCardsWithTable[ 3 ] = new HashMap();
        //List<HashMap> playersHandWithTable = new HashMap();

        List<?> playerCardsWithTableSorted;
        for (int iterateAvailableCards = 0; iterateAvailableCards < HAND_CARDS_COUNT + 5; iterateAvailableCards++) {
            playerCardsWithTable[ 0 ].put("card text", playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(0));
            playerCardsWithTable[ 1 ].put("card suit", playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(1));
            playerCardsWithTable[ 2 ].put("card rank", playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(2));
            playerCardsWithTable[ 3 ].put("card value", playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(3));

            //playersHandWithTable = List.of(playerCardsWithTable[ 0 ]);
            // 0 string;  1 suit; 2 rank; 3 value

        }
        playerCardsWithTableSorted = playerCardsWithTable[ 3 ].values().stream().sorted().toList();
        System.out.println("playersHandWithTable output sorted " + playerCardsWithTableSorted);
        //System.out.println("playersHandWithTable output" + NEXT_LINE + playersHandWithTable + NEXT_LINE + List.of(playerCardsWithTable[1]) + NEXT_LINE  + List.of(playerCardsWithTable[2]) + NEXT_LINE + List.of(playerCardsWithTable[3]) + NEXT_LINE + "playersHandWithTable end");

        //System.out.println("playersHandWithTable output " + playerCardsWithTable[3].toString());


        return playerCardsWithTable;
    }*/
    static ArrayList<?>[] getPlayerCardsWithTable(Integer moveCount, ArrayList<String>[][] playerCards, HashMap[] players) {

        ArrayList<String>[] playerCardsWithTable = new ArrayList[ 4 ];
        playerCardsWithTable[ 0 ] = new ArrayList();
        playerCardsWithTable[ 1 ] = new ArrayList();
        playerCardsWithTable[ 2 ] = new ArrayList();
        playerCardsWithTable[ 3 ] = new ArrayList();
        //List<HashMap> playersHandWithTable = new HashMap();

        List<?> playerCardsWithTableSorted;
        for (int iterateAvailableCards = 0; iterateAvailableCards < HAND_CARDS_COUNT + 5; iterateAvailableCards++) {
            playerCardsWithTable[ 0 ].add(playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(0));
            playerCardsWithTable[ 1 ].add(playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(1));
            playerCardsWithTable[ 2 ].add(playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(2));
            playerCardsWithTable[ 3 ].add(playerCards[ moveCount % players.length ][ iterateAvailableCards ].get(3));

            //playersHandWithTable = List.of(playerCardsWithTable[ 0 ]);
            // 0 string;  1 suit; 2 rank; 3 value

        }
        playerCardsWithTableSorted = playerCardsWithTable[ 3 ].stream().sorted().toList();
        System.out.println("playersHandWithTable output sorted " + playerCardsWithTableSorted);
        //System.out.println("playersHandWithTable output" + NEXT_LINE + playersHandWithTable + NEXT_LINE + List.of(playerCardsWithTable[1]) + NEXT_LINE  + List.of(playerCardsWithTable[2]) + NEXT_LINE + List.of(playerCardsWithTable[3]) + NEXT_LINE + "playersHandWithTable end");

        //System.out.println("playersHandWithTable output " + playerCardsWithTable[3].toString());


        return playerCardsWithTable;
    }

    static int rankRoll(int playersQuantity, ArrayList<String>[][] playerCards) { //get highest card, if highest quantity > 1, return -1
        int handHighest = -1;
        int winnerCount = 0;
        int winnerId = -1;
        for (int iteratePlayer = 0; iteratePlayer < playersQuantity; iteratePlayer++) {
            //     for (int i = 0; i < playersCardsCount; i++) {
            if (handHighest < Integer.parseInt(playerCards[ iteratePlayer ][ 0 ].get(3))) {
                handHighest = Integer.parseInt(playerCards[ iteratePlayer ][ 0 ].get(3));
                winnerCount = 1;
                winnerId    = iteratePlayer;
                //      }
            } else if (handHighest == Integer.parseInt(playerCards[ iteratePlayer ][ 0 ].get(3).toString())) {
                winnerCount++;
                winnerId = -1;
            }
        }
        if (winnerCount == 1 && winnerId > -1) {
            return winnerId;
        } else {
            return -1;
        }
    }

    // TODO() check if card burning shifts order correctly
    static String stageFlop(int playersQuantity, int playersCardsCount, List<?> deck) {
        return deck.get((playersQuantity * playersCardsCount + 3)).toString() +
               deck.get((playersQuantity * playersCardsCount + 3 + 1)).toString() +
               deck.get((playersQuantity * playersCardsCount + 3 + 2)).toString();
    }

    static String stageTurn(int playersQuantity, int playersCardsCount, List<?> deck) {
        return deck.get((playersQuantity * playersCardsCount + 3 + 2 + 1 + 1)).toString();
    }

    static String stageRiver(int playersQuantity, int playersCardsCount, List<?> deck) {
        return deck.get((playersQuantity * playersCardsCount + 3 + 2 + 1 + 1 + 1 + 1)).toString();
    }

    public static int anyRandomIntRange(int low, int high) {
        Random random = new Random();
        return random.nextInt(high) + low + 1;
    }

    static ArrayList[] drawTable(int playersQuantity, List<?> deck) {


        ArrayList[] tableArray = new ArrayList[ playersQuantity ];//(playersQuantity);
        tableArray[ 0 ] = new ArrayList<>(5);
        tableArray[ 1 ] = new ArrayList<>(5);
        //tableArray[2] = new HashMap(5);
        tableArray[ 0 ].add((stageFlop(playersQuantity, 2, deck) +
                                 stageTurn(playersQuantity, 2, deck) +
                                 stageRiver(playersQuantity, 2, deck)));
        tableArray[ 1 ].add(tableArray[ 0 ].get(0).toString().replaceAll(FIND_CARDS_RANK_REGEXP, EMPTY_STRING));
        tableArray[ 1 ].add(Arrays.toString(Arrays.stream(tableArray[ 0 ].get(0).toString().replaceAll(FIND_CARDS_SUIT_REGEXP, " ").split("( )")).toArray()));
        System.out.println(tableArray[ 0 ].get(0));
        List<?> tableValue;// = new HashMap();
        tableValue = Arrays.stream(tableArray[ 0 ].get(0).toString().replaceAll(FIND_CARDS_SUIT_REGEXP, " ").split(COMMA)).toList();

        System.out.println("tableValue " + tableValue);
        return tableArray;
    }


    static public String dealCardsText(HashMap[] players, ArrayList<String>[][] playerCards, Integer HAND_CARDS_COUNT) {
        String currentPlayerHandText;
        StringBuilder allHandsText = new StringBuilder(EMPTY_STRING);

        for (int playerNumber = 0; playerNumber < players.length; playerNumber++) {

            StringBuilder hand = new StringBuilder();
            for (int playerCardNumber = 0; playerCardNumber < HAND_CARDS_COUNT; playerCardNumber++) {
                hand.append(playerCards[ playerNumber ][ playerCardNumber ].get(0));
            }

            currentPlayerHandText = players[ playerNumber ].get("userName") + " hand: " + hand + NEXTLINE;
            allHandsText.append(currentPlayerHandText);

        }
        return allHandsText.toString();
    }


    static public String getRoundAnnounceText(HashMap[] players) {
        return "1. @" + players[ 0 ].get("nickName").toString() + " bet: " + players[ 0 ].get("currentRoundBet").toString() + ". " +
               "Balance " + players[ 0 ].get("chips").toString() + NEXTLINE +
               "2. @" + players[ 1 ].get("nickName").toString() + " bet: " + players[ 1 ].get("currentRoundBet").toString() + ". " +
               "Balance " + players[ 1 ].get("chips").toString();
    }


}

