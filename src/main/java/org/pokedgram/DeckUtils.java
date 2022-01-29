package org.pokedgram;

import java.util.*;

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
                cardDeck.add(suit + rank);
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

        ArrayList[][] playersHand = new ArrayList[ playersQuantity ][ playersCardsCount ];

        //Integer[] cardRanksValue = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};

        //deal cards to players
        for (int i = 0; i < playersCardsCount; i++) { // deal card loop
            for (int y = 0; y < playersQuantity; y++) { //select player to deal card
                playersHand[ y ][ i ] = new ArrayList() {
                };
                playersHand[ y ][ i ].add(0, deck.get((y + (i * playersQuantity)))); // 0 string
                playersHand[ y ][ i ].add(1, playersHand[ y ][ i ].get(0).toString().replaceAll("[0-9JQKA]", "")); // 1 suit
                playersHand[ y ][ i ].add(2, playersHand[ y ][ i ].get(0).toString().replaceAll("[♠♣♦♥]", "")); // 2 rank
                playersHand[ y ][ i ].add(3, cardValue(playersHand[ y ][ i ].get(2).toString())); // 3 value
                //System.out.println("Player " + (y) + " card: " + deck.get((y + (i * playersQuantity))));
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

    //
    static Integer checkStraight(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        return -1;
    }

    static Integer checkOnePair(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        return -1;
    }

    static Integer checkTwoPair(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] tablee) {
        return -1;
    }

    static Integer checkThreeOf(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        return -1;
    }

    static Integer checkFourOf(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        return -1;
    }

    static Integer checkKicker(ArrayList[] players, ArrayList[][] playersCards, ArrayList[] table) {
        return -1;
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
        tableArray[0].add(0, ((phaseFlop(playersQuantity, 2, deck) +
                               phaseTurn(playersQuantity, 2, deck) +
                               phaseRiver(playersQuantity, 2, deck))));
        tableArray[1].add(0, tableArray[0].get(0).toString().replaceAll("[0-9JQKA]", ""));
        System.out.println(tableArray[0].get(0));

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
