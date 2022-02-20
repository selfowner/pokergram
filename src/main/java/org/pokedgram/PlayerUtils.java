package org.pokedgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.pokedgram.DeckUtils.*;
import static org.pokedgram.SuperStrings.*;

public class PlayerUtils extends PokedgramBot {
    //TODO() create type "Player"
    public static ArrayList<?>[] addPlayerToQueue(String userId, String userName, ArrayList[] playersQueue, String nickName, int registeredPlayers, int chips, boolean ifFull) {
        //public static ArrayList<String>[] addPlayerToQueue(String userId, String userName, ArrayList[] playersQueue, String nickName, int registeredPlayers, int chips, boolean ifFull, int unregid) {
        //check if somebody unregistered
        int updateId = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);

        if (updateId > -1) {
            playersQueue[ updateId ].set(0, userId); // string userId
            playersQueue[ updateId ].set(1, userName); // string user firstname + lastname
            playersQueue[ updateId ].set(2, nickName);
            playersQueue[ updateId ].set(3, chips); //chipsQuantity
            playersQueue[ updateId ].set(4, true); // isActive for reg/unreg && autofold
            playersQueue[ updateId ].set(5, false); // FoldFlag
            playersQueue[ updateId ].set(6, false); // AllinFlag manual
            playersQueue[ updateId ].set(7, false); // isChipleader?
            playersQueue[ updateId ].set(8, 0); // currentStageBet
            playersQueue[ updateId ].set(9, 0); // currentRoundBet
            playersQueue[ updateId ].set(10, 0); // isAutoAllinOnBlind
            playersQueue[ updateId ].set(11, -1); // placeOnTableFinished, -1 = ingame
            playersQueue[ updateId ].set(12, false); // placeOnTableFinished, -1 = ingame
            playersQueue[ updateId ].set(13, -1); //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0

        } else {
            if (!ifFull) {
                playersQueue[ registeredPlayers ] = new ArrayList<>(13) {
                };
                playersQueue[ registeredPlayers ].add(0, userId); // string userId
                playersQueue[ registeredPlayers ].add(1, userName); // string user firstname + lastname
                playersQueue[ registeredPlayers ].add(2, nickName);
                playersQueue[ registeredPlayers ].add(3, chips); //chipsQuantity
                playersQueue[ registeredPlayers ].add(4, true); // isActive for reg/unreg
                playersQueue[ registeredPlayers ].add(5, false); // FoldFlag
                playersQueue[ registeredPlayers ].add(6, false); // AllinFlag manual
                playersQueue[ registeredPlayers ].add(7, false); // isChipleader?
                playersQueue[ registeredPlayers ].add(8, 0); // currentStageBet
                playersQueue[ registeredPlayers ].add(9, 0); // currentRoundBet
                playersQueue[ registeredPlayers ].add(10, 0); // isAutoAllinOnBlind
                playersQueue[ registeredPlayers ].add(11, -1); // placeOnTableFinished, -1 = ingame
                playersQueue[ registeredPlayers ].add(12, false); // check this stage, true = active
                playersQueue[ registeredPlayers ].add(13, -1); //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0


            }
        }

        return playersQueue;
    }

    public static void unsetAllPlayerBetFromStageToRound(ArrayList<Integer>[] players) {
        for (ArrayList<Integer> player : players) {
            player.set(9, 0);
            player.set(8, 0);
        }
    }

    public static void moveAllPlayerBetFromStageToRound(ArrayList<Integer>[] players) {
        for (ArrayList<Integer> player : players) {
            player.set(9, player.get(9) + player.get(8));
            player.set(8, 0);
        }
    }

    public static void setPlayerStageFlag(ArrayList<Boolean>[] players, int moveCount, String flag) {
        if (flag.equals("check flag")) {
            players[ moveCount ].set(12, true);
            return;
        }

        if (flag.equals("fold flag")) {
            players[ moveCount ].set(5, true);
            return;
        }

        if (flag.equals("allin flag")) {
            players[ moveCount ].set(6, true);
        }

    }

    public static void unsetAllPlayerStageFlag(ArrayList<Boolean>[] players, String flag) {
        for (ArrayList<Boolean> player : players) {
            if (flag.equals("check flag")) {
                player.set(12, false);
            }

            if (flag.equals("fold flag")) {
                player.set(5, false);
            }

            if (flag.equals("allin flag")) {
                player.set(6, false);
            }

            if (flag.equals("bets flag")) {
                player.set(6, false);
            }

            if (flag.equals("clean all")) {
                player.set(6, false);
                player.set(5, false);
                player.set(12, false);
                //players[ i ].set(13, -1);
            }
        }
    }

    public static boolean unsetPlayerStageFlag(ArrayList<Boolean>[] players, int moveCount, String flag) {
        if (flag.equals("check flag")) {
            players[ moveCount ].set(12, false);
            return true;
        }

        if (flag.equals("fold flag")) {
            players[ moveCount ].set(5, false);
            return true;
        }

        if (flag.equals("allin flag")) {
            players[ moveCount ].set(6, false);
            return true;
        }

        if (flag.equals("clean all")) {
            players[ moveCount ].set(6, false);
            players[ moveCount ].set(5, false);
            players[ moveCount ].set(12, false);
            return true;
        }
        return false;
    }


    public static int checkFoldsQuantityThisRound(ArrayList<?>[] playersList) {
        int foldCount = 0;
        for (int iteratePlayer = playersList.length - 1; iteratePlayer >= 0; iteratePlayer--) {
            if (!playersList[ iteratePlayer ].get(5).equals(FALSE_TEXT)) { // fold flag
                foldCount++;
            }

        }
        return foldCount;
    }

    public static int checkAllinsQuantityThisRound(ArrayList<?>[] playersList) {
        int allinCount = 0;
        for (int iteratePlayer = playersList.length - 1; iteratePlayer >= 0; iteratePlayer--) {
            if (!Objects.equals(playersList[ iteratePlayer ].get(5), FALSE_TEXT)) { // fold flag
                allinCount++;
            }
        }
        return allinCount;
    }

//get current bets
/*    public static String getCurrentBets(ArrayList[] players) {
        String output = EMPTY_STRING;

        new String(
                "1." + players[ smallBlindId ].get(2) + " bet: " + players[ smallBlindId ].get(9) + ". " +
                "Balance " + players[ smallBlindId ].get(3) + NEXT_LINE +
                "2." + players[ bigBlindId ].get(2) + " bet: " + players[ bigBlindId ].get(9) + ". " +
                "Balance " + players[ bigBlindId ].get(3));

        for (int i = 0; i < players.length; i++) {

            output += players[ smallBlindId ].get(2);



            } else {
                //all ok
            }
        }

        return players;


        return output;
    }*/

    public static ArrayList<?>[] checkLastChips(ArrayList<Integer>[] players, int bigBlindSize) {
        for (int iteratePlayer = 0; iteratePlayer < players.length; iteratePlayer++) {
            if (Integer.parseInt(players[ iteratePlayer ].get(3).toString()) < bigBlindSize) {
                players[ iteratePlayer ].set(10, 1);
                if (Integer.parseInt(players[ iteratePlayer ].get(3).toString()) < bigBlindSize / 2) {
                    players[ iteratePlayer ].set(10, 2);
                }
                if (Integer.parseInt(players[ iteratePlayer ].get(3).toString()) == 0) {
                    players[ iteratePlayer ].set(10, iteratePlayer);
                }
            } else {
                //all ok
            }
        }
        return players;
    }

    public static ArrayList<?>[] removeZeroBalance(ArrayList<Integer>[] players) {
        IntStream.range(0, players.length).filter(i -> !players[ i ].isEmpty() && Integer.parseInt(players[ i ].get(10).toString()) > -1).forEach(i -> players[ i ] = null);

        //without refactor
//        for (int i = 0; i < players.length; i++) {
//            if (!players[ i ].isEmpty() && Integer.parseInt(players[ i ].get(10).toString()) > -1) {
//                players[ i ] = null;
//            }
//        }
//        return players;

        return players;
    }

    public static int findMaxBet(ArrayList<?>[] playersList) {


        int maxBet = 0;
        for (ArrayList<?> objects : playersList) {
            if (maxBet <= Integer.parseInt(objects.get(9).toString())) {
                maxBet = Integer.parseInt(objects.get(9).toString());
            }
        }


        return maxBet;
    }

    public static boolean checkBetsEqual(ArrayList<?>[] playersList) {

        int foldCount = checkFoldsQuantityThisRound(playersList);
        int allinCount = checkAllinsQuantityThisRound(playersList);
        int maxBet = 0;
        int maxBetCount = 0;
        for (ArrayList<?> objects : playersList) {
            if (maxBet == Integer.parseInt(objects.get(9).toString())) {

                maxBetCount++;
            }

            if (maxBet < Integer.parseInt(objects.get(9).toString())) {

                maxBetCount = 0;
                maxBet      = Integer.parseInt(objects.get(9).toString());
                maxBetCount++;
            }
        }

        //System.out.println("checkBetsEqual - foldCount: " + foldCount + "\nallinCount: " + allinCount + "\nmaxBet:
        // " + maxBet + "\nmaxBetCount: " + maxBetCount);
        if (maxBetCount + foldCount >= playersList.length || allinCount == (maxBetCount + foldCount) || allinCount + foldCount == playersList.length) {

            return true;
        }

        return false;
    }

    public static ArrayList<?>[] shiftPlayerOrder(ArrayList<String>[] playersList, int offset) {
        //System.out.println("shiftPlayerOrder started. playerslist size: " + playersList.length + ". offset: " + offset);
        //      try {
        ArrayList<?>[] newPlayersList = new ArrayList[ playersList.length ];
        int last = -1;

        if (offset > 0 && playersList.length >= 2) { // no changes
            //System.out.println("offset > 0 && playersList.length >=2");

            if (offset == 1) {
                //System.out.println("offset == 1");
                if (playersList.length == 2) {

                    //System.out.println("playersList.length == 2");
                    newPlayersList[ 0 ] = playersList[ 1 ];
                    newPlayersList[ 1 ] = playersList[ 0 ];
                    //System.out.println("id [0]: " + newPlayersList[0].get(1));
                    //System.out.println("id [1]: " + newPlayersList[1].get(1));
                    return newPlayersList;
                }

/*                if (playersList.length > 2) {

                    System.out.println("playersList.length > 2");
                    for (int forward = 0; (offset + forward) < playersList.length; forward++) {
                        if ((offset+forward) == playersList.length) {
                            System.out.println("(offset+forward) == playersList.length");

                            newPlayersList[forward] = playersList[0];
                        } else {
                            System.out.println("!((offset+forward) == playersList.length)");
                            newPlayersList[forward] = playersList[offset + forward];
                        }
                        last = forward;
                        System.out.println(newPlayersList[forward].get(1) + " equals " + playersList[offset + forward].get(1));
                    }
                    if (last != playersList.length) {

                        System.out.println("last != playersList.length");
                        for (int backward = offset - 1; backward > 0; backward--) {
                            newPlayersList[last + backward] = playersList[last + backward];
                            System.out.println(newPlayersList[last+backward].get(1) + " equals " + playersList[last + backward].get(1));
                        }
                    }

                    //System.out.println("rollWinnerPlayerId > 0 && playersList.length == 2");
                    return newPlayersList;
                }*/
            }
            return playersList;
        }
        return playersList;
        //if (offset > 0 && playersList.length > 2) { //

    }


    public static int checkUserRegExistAndActive(String userId, ArrayList<?>[] playersQueue, int registeredPlayers) {

        for (int iteratePlayer = 0; iteratePlayer < registeredPlayers; iteratePlayer++) {

            if (playersQueue[ iteratePlayer ] != null && playersQueue[ iteratePlayer ].get(0).equals(userId) && playersQueue[ iteratePlayer ].get(4).equals(TRUE_TEXT)) { // && ) {
                return iteratePlayer;
            }
        }
        return -1;
    }

    public static int checkFoldCount(ArrayList<?>[] players) {
        int c = 0;
        for (ArrayList<?> player : players) {

            if (player.get(5).equals(TRUE_TEXT)) { // && ) {
                c++;
            }
        }
        return c;
    }

    public static int checkUserUnReg(ArrayList<?>[] playersQueue) {


        for (int iteratePlayer = 0; iteratePlayer < playersQueue.length - 1; iteratePlayer++) {
            if (playersQueue[ iteratePlayer ] != null && Objects.equals(playersQueue[ iteratePlayer ].get(4), FALSE_TEXT)) {

                return iteratePlayer;
            }
        }
        return -1; //ok

    }


//    public static ArrayList[] userUnreg(ArrayList[] playersQueue, String userId) {
//
//        for (int z = 0; z < playersQueue.length - 1; z++) {
//            if (playersQueue[z] != null && playersQueue[z].get(0) == userId) {
//                playersQueue[z].set(4, T);
//            }
//        }
//        return playersQueue;
//
//    }

    public static boolean checkUserLimit(int maxPlayers, int registeredPlayers) {

        if (registeredPlayers >= maxPlayers) {
            return true;
        }
        return false;
    }

    public static String getUserReg(ArrayList<?>[] playersQueue, int registeredPlayers, String userId) {
        StringBuilder users = new StringBuilder(EMPTY_STRING);
        for (int iteratePlayer = 0; iteratePlayer < registeredPlayers; iteratePlayer++) {
            if (playersQueue[ iteratePlayer ].get(0) != null && Objects.equals(playersQueue[ iteratePlayer ].get(4), userId)) {
                users.append("\n").append(playersQueue[ iteratePlayer ].get(1).toString());
            }
        }
        return users.toString();

    }

    public static ArrayList<?>[] getShowdownWinnerId(ArrayList<Integer>[] players, ArrayList<String>[][] playerCards, List<?> cardDeck) {
        int winnerId, winnerCount= -1;
       // int winnerCount = -1;

        ArrayList<?>[] tableArray = drawTable(players.length, cardDeck);
        if (false) {
            //sidepot
        }
        boolean gotWinner = false;

        //calculate winner and process chips
        while (!gotWinner) {

            //System.out.println("findWinner start");

            // TODO count kickers on table for some cases
            winnerCount += checkDistinct(playerCards, players);
            winnerCount += checkFlash(players, playerCards, tableArray);
            winnerCount += checkStraight(playerCards);

            System.out.println("checkDistinct winnerCount after checkDistinct checkFlash checkStraight: " + winnerCount);
            //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0
            if (Integer.parseInt(players[ 0 ].get(13).toString()) > Integer.parseInt(players[ 1 ].get(13).toString())) {
                System.out.println("player 0 win");
                winnerId = 0; //player 0 win
            } else if (Integer.parseInt(players[ 1 ].get(13).toString()) > Integer.parseInt(players[ 0 ].get(13).toString())) {

                System.out.println("player 1 win");

                winnerId = 1; //player 1 win
            } else if ( // comboValue ==
                    Integer.parseInt(players[ 0 ].get(13).toString()) == Integer.parseInt(players[ 1 ].get(13).toString()) && (Integer.parseInt(players[ 0 ].get(13).toString()) > -1 && Integer.parseInt(players[ 1 ].get(13).toString()) > -1)
            ) {
                System.out.println("players tie, checking kicker:");
                winnerCount = checkKicker(players, playerCards, 0);
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

        int playerIncome = splitPot(currentPot, players);
        if (winnerCount > 1) {
            for (ArrayList<Integer> player : players) {
                player.set(3, Integer.parseInt(player.get(3).toString()) + playerIncome);
                currentPot = 0;
            }
            System.out.println("winnerCount > 1; income " + playerIncome);
            gotWinner = true;
            // process chips
        } else if (winnerCount == 0) {
            System.out.println("winnerCount == 0; no combo found. Splitting pot, setting gotWinner true " + playerIncome);
            for (ArrayList<Integer> player : players) {
                player.set(3, Integer.parseInt(player.get(3).toString()) + playerIncome);
                currentPot = 0;
                gotWinner  = true;
            }
        } else if (winnerCount == 1) {
            //get winnerId
        }

        return players;
    }


    public static String getUserRegQueue(ArrayList<?>[] playersList) {
        StringBuilder users = new StringBuilder("\n");
        for (ArrayList<?> strings : playersList) {
            if (
                    strings != null &&
                    !strings.isEmpty() &&
                    strings.get(4).equals(TRUE_TEXT)
            ) {
                users = new StringBuilder("userName: " + users + strings.get(1) + "\nuserId: " + strings.get(0) + "\n\n");
            }
        }
        return users.toString();

    }


}
