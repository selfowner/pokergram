package org.pokedgram;


import java.util.*;

import static org.pokedgram.DeckUtils.cardValue;
import static org.pokedgram.PlayerUtils.getUniqueName;

public class Draw {



    static ArrayList[][] dealCards(int playersQuantity, int playersCardsCount, List deck) {

        ArrayList[][] playersHand = new ArrayList[playersQuantity][playersCardsCount];

        //Integer[] cardRanksValue = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};

        //players card init
        for (int i = 0; i < playersCardsCount; i++) { // deal card loop
            for (int y = 0; y < playersQuantity; y++) { //select player to deal card
                playersHand[y][i] = new ArrayList() {};
                playersHand[y][i].add(0, deck.get((y + (i * playersQuantity)))); // 0 string
                playersHand[y][i].add(1, playersHand[y][i].get(0).toString().replaceAll("[0-9JQKA]", "")); // 1 suit
                playersHand[y][i].add(2, playersHand[y][i].get(0).toString().replaceAll("[♠♣♦♥]", "")); // 2 rank
                playersHand[y][i].add(3, cardValue(playersHand[y][i].get(2).toString())); // 3 value
                //System.out.println("Player " + (y) + " card: " + deck.get((y + (i * playersQuantity))));
            }

        }

        //System.out.println("dealCards OK ");

        return playersHand;
    }


    static void checkFlash(int playersQuantity, ArrayList[][] playersArray, ArrayList[] tableArray) {
        boolean[] playersFlashValue = new boolean[playersQuantity];
        boolean reqs;
        for (Integer y = 0; y < playersQuantity; y++) {
            // 0 string;  1 suit; 2 rank; 3 value
            reqs = (playersArray[y][0].get(1).toString() + playersArray[y][1].get(1).toString() + tableArray[1].get(0).toString()).matches("((.*(♠).*){5}|(.*(♣).*){5}|(.*(♥).*){5}|(.*(♦).*){5})");
            if (reqs) {
                System.out.println("flash player " + y + ": " + ((playersArray[y][0].get(1).toString() + playersArray[y][1].get(1).toString() + tableArray[1].get(0))));
                playersFlashValue[y] = true;
            } else {
                System.out.println("player " + y + ": " + ((playersArray[y][0].get(1).toString() + playersArray[y][1].get(1) + tableArray[1].get(0))));
            }
        }
        System.out.println("checkFlash OK \n");
    }


    static void initializePlayers(int playersQuantity, int playerCardsCount, int playersStartingToken, List deck) {

        Map<Integer, String> playersName = new HashMap<>();
        Map<Integer, Integer> playersToken = new HashMap<>();
        HashMap players = new HashMap(playersQuantity);


        System.out.println();
        //Random random = new Random();

        for (int i = 0; i < playersQuantity; i++) {
            playersName.put(i, getUniqueName());
            playersToken.put(i, playersStartingToken);
            players.put(i, "Player " + (i + 1));
            //System.out.println("\"" + playersName.get(i) + "\" registered to the tournament! " + playersToken.get(i) + " tokens granted! gl hf :>");

        }

//        System.out.println("");
//        System.out.println("initializePlayers OK");
//
//        System.out.println("");

    }

}
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
