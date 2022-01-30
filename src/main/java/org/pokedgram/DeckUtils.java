package org.pokedgram;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.pokedgram.SuperStrings.*;


public class DeckUtils {

    public static List<String> initializeDeck() {

        List<String> cardDeck = new ArrayList(52);
        String[] cardSuits = new String[]{"♣", "♦", "♥", "♠"};
        String[] cardRanks = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        Integer[] cardRanksValue = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        //System.out.println(List.of(cardSuits) + NEXT_LINE + List.of(cardRanks));
        for (String suit : cardSuits) {
            for (String rank : cardRanks) {
                cardDeck.add(rank + suit);
            }
        }
        return cardDeck;
    }
    public static List<?> shuffleDeck(List<?> currentDeck) {
        Collections.shuffle(currentDeck);
        //System.out.println("deck shuffled: " + currentDeck);
        System.out.println("deck shuffled.");
        return currentDeck;
    }
    static ArrayList[][] dealCards(int playersQuantity, int playersCardsCount, List deck) {

        ArrayList[][] playersHand = new ArrayList[ playersQuantity ][ playersCardsCount+5 ];

        //Integer[] cardRanksValue = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};

        //deal cards to players
        for (int i = 0; i < playersCardsCount; i++) { // deal card loop
            for (int y = 0; y < playersQuantity; y++) { //select player to deal card
                playersHand[ y ][ i ] = new ArrayList() {
                };
                playersHand[ y ][ i ].add(0, deck.get((y + (i * playersQuantity)))); // 0 string
                playersHand[ y ][ i ].add(1, playersHand[ y ][ i ].get(0).toString().replaceAll("[0-9JQKA]", "")); // 1 suit
                playersHand[ y ][ i ].add(2, playersHand[ y ][ i ].get(0).toString().replaceAll("[♠♣♦♥]", "")); // 2
                // rank
                playersHand[ y ][ i ].add(3, cardValue(playersHand[ y ][ i ].get(2).toString())); // 3 value
                //System.out.println("Player " + (y) + " card: " + deck.get((y + (i * playersQuantity))));

                if (i==1) {
                    playersHand[ y ][ 2 ] = new ArrayList() {
                    };
                    playersHand[ y ][ 3 ] = new ArrayList() {
                    };
                    playersHand[ y ][ 4 ] = new ArrayList() {
                    };
                    playersHand[ y ][ 5 ] = new ArrayList() {
                    };
                    playersHand[ y ][ 6 ] = new ArrayList() {
                    };

                    playersHand[ y ][ 2 ].add(0, deck.get((playersQuantity * playersCardsCount + 3)).toString());
                    playersHand[ y ][ 2 ].add(1, deck.get((playersQuantity * playersCardsCount + 3)).toString().replaceAll("[0-9JQKA]", ""));
                    playersHand[ y ][ 2 ].add(2, deck.get((playersQuantity * playersCardsCount + 3)).toString().replaceAll("[♠♣♦♥]", ""));
                    playersHand[ y ][ 2 ].add(3, cardValue(playersHand[ y ][ 2 ].get(2).toString()));

                    playersHand[ y ][ 3 ].add(0, deck.get((playersQuantity * playersCardsCount + 3 + 1)).toString());
                    playersHand[ y ][ 3 ].add(1, deck.get((playersQuantity * playersCardsCount + 3 + 1)).toString().replaceAll("[0-9JQKA]", ""));
                    playersHand[ y ][ 3 ].add(2, deck.get((playersQuantity * playersCardsCount + 3 + 1)).toString().replaceAll("[♠♣♦♥]", ""));
                    playersHand[ y ][ 3 ].add(3, cardValue(playersHand[ y ][ 3 ].get(2).toString()));


                    playersHand[ y ][ 4 ].add(0, deck.get((playersQuantity * playersCardsCount + 3 + 2)).toString());
                    playersHand[ y ][ 4 ].add(1, deck.get((playersQuantity * playersCardsCount + 3 + 2)).toString().replaceAll("[0-9JQKA]", ""));
                    playersHand[ y ][ 4 ].add(2, deck.get((playersQuantity * playersCardsCount + 3 + 2)).toString().replaceAll("[♠♣♦♥]", ""));
                    playersHand[ y ][ 4 ].add(3, cardValue(playersHand[ y ][ 4 ].get(2).toString()));

                    playersHand[ y ][ 5 ].add(phaseTurn(2,2,deck));
                    playersHand[ y ][ 5 ].add(1, phaseTurn(2,2,deck).replaceAll("[0-9JQKA]", ""));
                    playersHand[ y ][ 5 ].add(2, phaseTurn(2,2,deck).replaceAll("[♠♣♦♥]", ""));
                    playersHand[ y ][ 5 ].add(3, cardValue(playersHand[ y ][ 5 ].get(2).toString()));

                    playersHand[ y ][ 6 ].add(phaseRiver(2,2,deck));
                    playersHand[ y ][ 6 ].add(1, phaseRiver(2,2,deck).replaceAll("[0-9JQKA]", ""));
                    playersHand[ y ][ 6 ].add(2, phaseRiver(2,2,deck).replaceAll("[♠♣♦♥]", ""));
                    playersHand[ y ][ 6 ].add(3, cardValue(playersHand[ y ][ 6 ].get(2).toString()));
                }

            }
        }

        return playersHand;
    }
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

    //ToDo() return Highest card with players id for counting winner
    static Integer checkFlash(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        ArrayList matchedPlayers[] = new ArrayList[]{};
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
            for (Integer y = 0; y < players.length; y++) {
                playersHandWithTable = playersCards[ y ][ 0 ].get(1).toString() + playersCards[ y ][ 1 ].get(1).toString() + table[ 1 ].get(0).toString();
                // 0 string;  1 suit; 2 rank; 3 value
                if (playersHandWithTable.matches(FIND_FLASH_REGEXP)) {
                    matchedPlayers[ checkFlashCount ].add(y);
                    matchedPlayers[ checkFlashCount ].add(players[ y ].get(0));
                    matchedPlayers[ checkFlashCount ].add(playersHandWithTable);
                    checkFlashCount++;
                    System.out.println("checkFlash: " + y + ": " + ((playersCards[ y ][ 0 ].get(1).toString() + playersCards[ y ][ 1 ].get(1).toString() + table[ 1 ].get(0))));
                }
            }

            if (checkFlashCount > 1) {
                for (int z = 0; z <= checkFlashCount; z++) {

                    //playersCards[z][0].get(1) + playersCards[z][1].get(1);
                    //matchedPlayers[z].get(2);

                }
            }
        }


        return checkFlashCount;
    }
    static Integer checkStraight(ArrayList[][] playersCards) {
        return -1;
    }

    static Integer checkDistinct(ArrayList[][] playersCards, ArrayList[] players) {
        System.out.println("checkDistinct start");
        Integer countCombo= 0;

        for (int i = 0; i < players.length; i++) {
            String[] playersDistinctCards = new String[3];
            Integer distinctQuantity = 0;
            //countCombo = 0;
            String excludeSingle = "";
            System.out.println("player: " +i);
            ArrayList[] currentPlayerCards = getPlayerCardsWithTable(i, playersCards, players);
            List<Integer> cardValues = new ArrayList<Integer>();
            for (int b = 0; b < 7; b++) {
                cardValues.add(Integer.parseInt(currentPlayerCards[ 3 ].get(b).toString()));
                excludeSingle = cardValues.stream().sorted().collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting())).toString().replaceAll(COLLECT_DISTINCT_VALUES_REGEXP,"$1 ");

                distinctQuantity = excludeSingle.replaceAll(COUNT_VALUES_REGEXP, "$1").length();
                System.out.println("rank = distinct: " + excludeSingle + "; distinctQuantity: " + distinctQuantity);//


            }
            try {


                //System.out.println("COUNT_VALUES_REGEXP rank = distinct: " + excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP,"$1$2$3") + "; distinctQuantity: " + distinctQuantity);//

                //distinctQuantity = excludeSingle.replaceAll(COUNT_VALUES_REGEXP, "$1").length();
                //System.out.println("DISCARD_SINGLE_CARDS_REGEXP rank = distinct: " + excludeSingle.replaceAll(DISCARD_SINGLE_CARDS_REGEXP,"") + "; distinctQuantity: " + distinctQuantity);//

                if (distinctQuantity!=null) {
                    if(distinctQuantity==1) { //1 distinct value
                        countCombo++;
                        playersDistinctCards[0] = excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP, "$1");
                        System.out.println("playersDistinctCards:" + NEXT_LINE + playersDistinctCards[0]);
                    } else
                    if(distinctQuantity==2) { //2
                        countCombo++;
                        playersDistinctCards[0] = excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP, "$1");
                        playersDistinctCards[1] = excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP, "$2");
                        System.out.println("playersDistinctCards:" + NEXT_LINE + playersDistinctCards[0] + NEXT_LINE + playersDistinctCards[1]);
                    } else
                    if(distinctQuantity==3) { //3
                        countCombo++;
                        playersDistinctCards[0] = excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP, "$1");
                        playersDistinctCards[1] = excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP, "$2");
                        playersDistinctCards[2] = excludeSingle.replaceAll(COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP, "$3");
                        System.out.println("playersDistinctCards:" + NEXT_LINE + playersDistinctCards[0] + NEXT_LINE + playersDistinctCards[1] + NEXT_LINE + playersDistinctCards[2]);
                    }
                } else { //0
                    System.out.println("no combo found");
                    countCombo = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("checkDistinct end");
        }
        return countCombo;
    }

    static Integer checkKicker(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        return -1;
    }

    static ArrayList[] getPlayerCardsWithTable(Integer moveCount, ArrayList[][] playerCards, ArrayList[] players) { //
        //Integer.parseInt(String.valueOf(playerCards[1][].stream().distinct().count()));
        //return 1;
        ArrayList[] playerCardsWithTable = new ArrayList[4];
        playerCardsWithTable[0] = new ArrayList<>();
        playerCardsWithTable[1] = new ArrayList<>();
        playerCardsWithTable[2] = new ArrayList<>();
        playerCardsWithTable[3] = new ArrayList<>();
        List<ArrayList> playersHandWithTable = new ArrayList<>();

        for (Integer z = 0; z < HAND_CARDS_COUNT + 5; z++) {

            playerCardsWithTable[0].add(playerCards[moveCount % players.length][z].get(0)) ;
            playerCardsWithTable[1].add(playerCards[moveCount % players.length][z].get(1)) ;
            playerCardsWithTable[2].add(playerCards[moveCount % players.length][z].get(2)) ;
            playerCardsWithTable[3].add(playerCards[moveCount % players.length][z].get(3)) ;

            playersHandWithTable = List.of(playerCardsWithTable[0]);
            // 0 string;  1 suit; 2 rank; 3 value

        }
        //System.out.println("playersHandWithTable output" + NEXT_LINE + playersHandWithTable + NEXT_LINE + List.of(playerCardsWithTable[1]) + NEXT_LINE  + List.of(playerCardsWithTable[2]) + NEXT_LINE + List.of(playerCardsWithTable[3]) + NEXT_LINE + "playersHandWithTable end");
        System.out.println("playersHandWithTable output " + playerCardsWithTable[3].toString());
        return playerCardsWithTable;
    }
    static int rankRoll(int playersQuantity, ArrayList<?>[][] cards) { //get highest card, if highest quantity > 1, return -1
        int handHighest = -1;
        int winnerCount = 0;
        int winnerId = -1;
        for (int y = 0; y < playersQuantity; y++) {
            //     for (int i = 0; i < playersCardsCount; i++) {
            if (handHighest < Integer.parseInt(cards[ y ][ 0 ].get(3).toString())) {
                handHighest = Integer.parseInt(cards[ y ][ 0 ].get(3).toString());
                winnerCount = 1;
                winnerId    = y;
                //      }
            } else if (handHighest == Integer.parseInt(cards[ y ][ 0 ].get(3).toString())) {
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
    static String phaseFlop(int playersQuantity, int playersCardsCount, List<?> deck) {
        return deck.get((playersQuantity * playersCardsCount + 3)).toString() +
               deck.get((playersQuantity * playersCardsCount + 3 + 1)).toString() +
               deck.get((playersQuantity * playersCardsCount + 3 + 2)).toString();
    }
    static String phaseTurn(int playersQuantity, int playersCardsCount, List<?> deck) {
        return deck.get((playersQuantity * playersCardsCount + 3 + 2 + 1 + 1)).toString();
    }
    static String phaseRiver(int playersQuantity, int playersCardsCount, List<?> deck) {
        return deck.get((playersQuantity * playersCardsCount + 3 + 2 + 1 + 1 + 1 + 1)).toString();
    }
    public static int anyRandomIntRange(int low, int high) {
        Random random = new Random();
        return random.nextInt(high) + low + 1;
    }
    static ArrayList[] drawTable(int playersQuantity, List deck) {


        ArrayList[] tableArray = new ArrayList[playersQuantity];
        tableArray[0] = new ArrayList(5);
        tableArray[1] = new ArrayList(5);
        //tableArray[2] = new ArrayList(5);
        tableArray[0].add(0, ((phaseFlop(playersQuantity, 2, deck) +
                               phaseTurn(playersQuantity, 2, deck) +
                               phaseRiver(playersQuantity, 2, deck))));
        tableArray[1].add(0, tableArray[0].get(0).toString().replaceAll("[0-9JQKA]", ""));
        tableArray[1].add(1, Arrays.stream(tableArray[0].get(0).toString().replaceAll("[♣♦♥♠]*", " ").split("( )")).toArray());
        System.out.println(tableArray[0].get(0));
        List tableValue = new ArrayList();
        tableValue = Arrays.stream(tableArray[0].get(0).toString().replaceAll("[♣♦♥♠]*", " ").split("( )")).toList();

        System.out.println("tableValue " + tableValue);
        return tableArray;
    }
}

//TODO() decide drop this legacy
    /*static void initializePlayers(int playersQuantity, int playerCardsCount, int playersStartingToken, List deck) {

        Map<Integer, String> playersName = new HashMap<>();
        Map<Integer, Integer> playersToken = new HashMap<>();
        HashMap players = new HashMap(playersQuantity);

        for (int i = 0; i < playersQuantity; i++) {
            playersName.put(i, getUniqueName());
            playersToken.put(i, playersStartingToken);
            players.put(i, "Player " + (i + 1));
            System.out.println("\"" + playersName.get(i) + "\" registered to the tournament! " + playersToken.get(i) + " tokens granted! gl hf :>");

        }
        System.out.println("initializePlayers OK");

    }*/

//    public static int maxValue(int array[]) {
//        List<Integer> list = new ArrayList<Integer>();
//        for (int i = 0; i < array.length; i++) {
//            list.add(array[i]);
//        }
//
//        return Collections.max(list);
//
//
//    }

//        while (winnerCount > 1) {
//            for (int y = 0; y < winnerCount; y++) {
//                if (handHighest > playerHighest) {
//                    playerHighest = handHighest;
//                    winnerId = y;
//                    winnerCount = 1 ;
//                } else if (handHighest[y] == playerHighest) {
//                    winnerCount++;
//                    winnerId = -1;
//                }
//
//            }
//        }
//int maxValue = Collections.max(numbers);
// List<Integer> maxValues = numbers.stream().filter(number -> number == max).collect(Collectors.toList());


//List<String> deckSuits = new ArrayList<>(4);
//deckSuits.addAll(List.of(cardSuits));
//deckRanks.addAll(List.of(cardRanks));
//deckRanks.remove(13);
//from initializeDeck method
//--
//class Table {
//
//    int bigBlindId;
//    int potSize;
//    int seats;
//    private Boolean preflop = true;
//    private Boolean flop, turn, river = false;
//
//    Boolean[] currentRound = new Boolean[]{preflop, flop, turn, river};
//
//
//}
