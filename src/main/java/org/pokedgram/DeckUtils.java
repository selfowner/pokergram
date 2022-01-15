package org.pokedgram;

import java.util.*;


public class DeckUtils {
    //private static int burn = 1;

    public static List<?> initializeDeck() {

        List<String> deck52 = new ArrayList< >();

        String[] cardSuits = new String[]{"♣", "♦", "♥", "♠"};
        String[] cardRanks = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
/*        Integer[] cardRanksValue = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};ArrayList<String> deckRanks = new ArrayList<>(13);*/


        for (int i = 0; i < cardSuits.length; i++) {
            for (int y = 0; y < cardRanks.length; y++) {
                deck52.add(i + y, cardSuits[i] + cardRanks[y]);
                //System.out.print(deck52.get(i+y));
            }
        }

        //System.out.println("\n" + deck52 + "\ninitializeDeck done" + "\n");
        // TODO() уточнить про тип данных
        return deck52;
    }


    public static List<?> shuffleDeck(List<?> currentDeck) {
        Collections.shuffle(currentDeck);
        return currentDeck;
    }

    public static int cardValue(String card) {
        int cardValue;
        if (Objects.equals(card, "A")) {
            cardValue = 14;
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


    static int rankRoll(int playersQuantity, ArrayList<?>[][] cards) { //get highest card, if highest quantity > 1, return -1
        int handHighest = -1;
        int winnerCount = 0;
        int winnerId = -1;
        for (int y = 0; y < playersQuantity; y++) {
            //     for (int i = 0; i < playersCardsCount; i++) {
            if (handHighest < Integer.parseInt(cards[y][0].get(3).toString())) {
                handHighest = Integer.parseInt(cards[y][0].get(3).toString());
                winnerCount = 1;
                winnerId = y;
                //      }
            } else if (handHighest == Integer.parseInt(cards[y][0].get(3).toString())) {
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
}


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
//    static ArrayList[] drawTable(int playersQuantity, List deck) {
//
//
//        ArrayList[] tableArray = new ArrayList[playersQuantity];
//        tableArray[0] = new ArrayList(5);
//        tableArray[1] = new ArrayList(5);
//        tableArray[0].add(0, ((phaseFlop(playersQuantity, 2, deck) +
//                phaseTurn(playersQuantity, 2, deck) +
//                phaseRiver(playersQuantity, 2, deck))));
//        tableArray[1].add(0, tableArray[0].get(0).toString().replaceAll("[0-9JQKA]", ""));
//        System.out.println(tableArray[0].get(0));
//
//        System.out.println("drawTable OK");
//        System.out.println("");
//        return tableArray;
//    }