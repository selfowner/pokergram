package org.pokedgram;

import java.util.ArrayList;
import java.util.List;

import static org.pokedgram.DeckUtils.*;

public class PlayerUtils extends PokedgramBot {
    //TODO() create type "Player"
    public static ArrayList[] addPlayerToQueue(String userId, String userName, ArrayList[] playersQueue, String nickName, int registeredPlayers, int chips, boolean ifFull, int unregid) {
        //check if somebody unregistered
        int updateId = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);

        if (updateId > -1) {
            playersQueue[updateId].set(0, userId); // string userId
            playersQueue[updateId].set(1, userName); // string user firstname + lastname
            playersQueue[updateId].set(2, nickName);
            playersQueue[updateId].set(3, chips); //chipsQuantity
            playersQueue[updateId].set(4, "true"); // isActive for reg/unreg && autofold
            playersQueue[updateId].set(5, false); // FoldFlag
            playersQueue[updateId].set(6, false); // AllinFlag manual
            playersQueue[updateId].set(7, "false"); // isChipleader?
            playersQueue[updateId].set(8, 0); // currentPhaseBet
            playersQueue[updateId].set(9, 0); // currentRoundBet
            playersQueue[updateId].set(10, 0); // isAutoAllinOnBlind
            playersQueue[updateId].set(11, -1); // placeOnTableFinished, -1 = ingame
            playersQueue[updateId].set(12, false); // placeOnTableFinished, -1 = ingame
        } else {
            if (!ifFull) {
                playersQueue[registeredPlayers] = new ArrayList(12) {};
                playersQueue[registeredPlayers].add(0, userId); // string userId
                playersQueue[registeredPlayers].add(1, userName); // string user firstname + lastname
                playersQueue[registeredPlayers].add(2, nickName);
                playersQueue[registeredPlayers].add(3, chips); //chipsQuantity
                playersQueue[registeredPlayers].add(4, "true"); // isActive for reg/unreg
                playersQueue[registeredPlayers].add(5, false); // FoldFlag
                playersQueue[registeredPlayers].add(6, false); // AllinFlag manual
                playersQueue[registeredPlayers].add(7, "false"); // isChipleader?
                playersQueue[registeredPlayers].add(8, 0); // currentPhaseBet
                playersQueue[registeredPlayers].add(9, 0); // currentRoundBet
                playersQueue[registeredPlayers].add(10, 0); // isAutoAllinOnBlind
                playersQueue[registeredPlayers].add(11, -1); // placeOnTableFinished, -1 = ingame
                playersQueue[registeredPlayers].add(12, false); // check this phase, true = active
            }
        }

        return playersQueue;
    }
    public static void unsetAllPlayerBetFromPhaseToRound(ArrayList[] players) {
        for (int i = 0; i <players.length; i++) {
            players[i].set(9, 0);
            players[i].set(8, 0);
        }
    }
    public static void moveAllPlayerBetFromPhaseToRound(ArrayList[] players) {
        for (int i = 0; i <players.length; i++) {
            players[i].set(9, Integer.parseInt(players[i].get(9).toString()) + Integer.parseInt(players[i].get(8).toString()));
            players[i].set(8, 0);
        }
    }

    public static boolean setPlayerPhaseFlag(ArrayList[] players, Integer moveCount, String flag) {
        if (flag.equals("check flag")) {
            players[moveCount].set(12, true);
            return true;
        }

        if (flag.equals("fold flag")) {
            players[moveCount].set(5, true);
            return true;
        }

        if (flag.equals("allin flag")) {
            players[moveCount].set(6, true);
            return true;
        }

        return false;
    }

    public static void unsetAllPlayerPhaseFlag(ArrayList[] players, String flag) {
        for (int i = 0; i <players.length; i++) {
            if (flag.equals("check flag")) {
                players[ i ].set(12, false);
            }

            if (flag.equals("fold flag")) {
                players[ i ].set(5, false);
            }

            if (flag.equals("allin flag")) {
                players[ i ].set(6, false);
            }

            if (flag.equals("clean all")) {
                players[ i ].set(6, false);
                players[ i ].set(5, false);
                players[ i ].set(12, false);
            }
        }
    }

    public static boolean unsetPlayerPhaseFlag(ArrayList[] players, Integer moveCount, String flag) {
        if (flag.equals("check flag")) {
            players[moveCount].set(12, false);
            return true;
        }

        if (flag.equals("fold flag")) {
            players[moveCount].set(5, false);
            return true;
        }

        if (flag.equals("allin flag")) {
            players[moveCount].set(6, false);
            return true;
        }

        if (flag.equals("clean all")) {
            players[moveCount].set(6, false);
            players[moveCount].set(5, false);
            players[moveCount].set(12, false);
            return true;
        }
        return false;
    }


    public static Integer checkFoldsQuantityThisRound(ArrayList[] playersList) {
        int foldCount = 0;
        for (int i = playersList.length - 1; i >= 0; i--) {
            if (playersList[i].get(5).toString() != "false") { // fold flag
                foldCount++;
            }

        }
        return foldCount;
    }

    public static Integer checkAllinsQuantityThisRound(ArrayList[] playersList) {
        int allinCount = 0;
        for (int i = playersList.length - 1; i >= 0; i--) {
            if (playersList[i].get(5).toString() != "false") { // fold flag
                allinCount++;
            }
        }
        return allinCount;
    }

//get current bets
/*    public static String getCurrentBets(ArrayList[] players) {
        String output = "";

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

    public static ArrayList<String>[] checkLastChips(ArrayList[] players, int bigBlindSize) {
        for (int i = 0; i < players.length; i++) {
            if (Integer.parseInt(players[i].get(3).toString()) < bigBlindSize) {
                players[i].set(10, 1);
                if (Integer.parseInt(players[i].get(3).toString()) < bigBlindSize/2) {
                    players[i].set(10, 2);
                }
                if (Integer.parseInt(players[i].get(3).toString()) == 0 ) {
                    players[i].set(10, i);
                }
            } else {
                //all ok
            }
        }
        return players;
    }

    public static ArrayList[] removeZeroBalance(ArrayList[] players) {
        for (int i = 0; i < players.length; i++) {
            if (!players[i].isEmpty() && Integer.parseInt(players[i].get(10).toString()) > -1 ) {
                players[i] = null;
            }
        }
        return players;
    }

    public static Integer findMaxbet(ArrayList[] playersList) {


        int maxBet = 0;
        for (int i = 0; i < playersList.length; i++) {
            if (maxBet < Integer.parseInt(playersList[i].get(9).toString())) {
                maxBet = Integer.parseInt(playersList[i].get(9).toString());
            }
        }


        return maxBet;
    }

    public static boolean checkBetsEqual(ArrayList[] playersList) {

        Integer foldCount = checkFoldsQuantityThisRound(playersList);
        Integer allinCount = checkAllinsQuantityThisRound(playersList);
        int maxBet = 0;
        int maxBetCount = 0;
        for (int i = 0; i < playersList.length; i++) {
            if (maxBet == Integer.parseInt(playersList[i].get(9).toString())) {

                maxBetCount++;
            }

            if (maxBet < Integer.parseInt(playersList[i].get(9).toString())) {

                maxBetCount = 0;
                maxBet = Integer.parseInt(playersList[i].get(9).toString());
                maxBetCount++;
            }
        }

        //System.out.println("checkBetsEqual - foldCount: " + foldCount + "\nallinCount: " + allinCount + "\nmaxBet:
        // " + maxBet + "\nmaxBetCount: " + maxBetCount);
        if (maxBetCount+foldCount>= playersList.length || allinCount == ( maxBetCount + foldCount)  ||  allinCount + foldCount== playersList.length ) {

            return true;
        }

        return false;
    }

    public static ArrayList<String>[] shiftPlayerOrder(ArrayList[] playersList, int offset) {
        //System.out.println("shiftPlayerOrder started. playerslist size: " + playersList.length + ". offset: " + offset);
        //      try {
        ArrayList[] newPlayersList = new ArrayList[playersList.length];
        int last = -1;

        if (offset > 0 && playersList.length >= 2) { // no changes
            //System.out.println("offset > 0 && playersList.length >=2");

            if (offset == 1) {
                //System.out.println("offset == 1");
                if (playersList.length == 2) {

                    //System.out.println("playersList.length == 2");
                    newPlayersList[0] = playersList[1];
                    newPlayersList[1] = playersList[0];
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


    public static int checkUserRegExistAndActive(String userId, ArrayList[] playersQueue, int registeredPlayers) {

        for (int z = 0; z < registeredPlayers; z++) {

            if (playersQueue[z] != null && playersQueue[z].get(0).toString().equals(userId) && playersQueue[z].get(4).toString().equals("true")) { // && ) {
                return z;
            }
        }
        return -1;
    }

    public static int checkFoldCount(ArrayList[] players) {
        int c = 0;
        for (int z = 0; z < players.length; z++) {

            if (players[z].get(5).toString().equals("true")) { // && ) {
                c++;
            }
        }
        return c;
    }

    public static int checkUserUnreg(ArrayList[] playersQueue) {


        for (int z = 0; z < playersQueue.length - 1; z++) {
            if (playersQueue[z] != null && playersQueue[z].get(4) == "false") {

                return z;
            }
        }
        return -1; //ok

    }


    public static ArrayList[] userUnreg(ArrayList[] playersQueue, String userId) {

        for (int z = 0; z < playersQueue.length - 1; z++) {
            if (playersQueue[z] != null && playersQueue[z].get(0) == userId) {
                playersQueue[z].set(4, "false");
            }
        }
        return playersQueue;

    }

    public static boolean checkUserLimit(int maxPlayers, int registeredPlayers) {

        if (registeredPlayers >= maxPlayers) {
            return true;
        }
        return false;
    }

    public static String getUserReg(ArrayList[] playersQueue, int registeredPlayers, String userId) {
        String users = "";
        for (int i = 0; i < registeredPlayers; i++) {
            if (playersQueue[i].get(0) != null && playersQueue[i].get(4) == userId) {
                users = users + "\n" + playersQueue[i].get(1).toString();
            }
        }
        return users;

    }

    public static Integer getShowdownWinnerId(ArrayList[] players, ArrayList[][] playerCards, List<?> cardDeck) {
        Integer winnerCount = -1;

        ArrayList[] tableArray = drawTable(players.length, cardDeck);
        if (false) {
            //sidepot
        }
        boolean gotWinner = false;


        while (!gotWinner) {



            // if 1 player left, finish game
            //find min stack

            // players = PlayerUtils.checkLastChips(players, bigBlindSize); //playersCount;
            // players = PlayerUtils.removeZeroBalance(players);
            //players = new ArrayList[playersOld.length];

            // add reward;
            //messageSendToPlayingRoom("table: " + tableCards + NEXT_LINE + "hands: " + allHandsText);

            System.out.println("findWinner start");

            winnerCount = checkDistinct(playerCards, players);
            System.out.println("checkDistinct winnerCount: " + winnerCount);
            if (winnerCount > 1) {
                //find kicker or split reward
                gotWinner = true;
                break;
            } else if (winnerCount == 1) {
                //winnerid takes pot
                gotWinner = true;
                break;
            }

            winnerCount = checkFlash(players, playerCards, tableArray);
            System.out.println("checkFlash winnerCount: " + winnerCount);
            if (winnerCount > 1) {
                //find kicker or split reward
                gotWinner = true;
                break;
            } else if (winnerCount == 1) {
                //winnerid takes pot
                gotWinner = true;
                break;
            }



            winnerCount = checkStraight(playerCards);
            System.out.println("checkStraight winnerCount: " + winnerCount);
            if (winnerCount > 1) {
                //find kicker or split reward
                gotWinner = true;
                break;
            } else if (winnerCount == 1) {
                //winnerid takes pot
                gotWinner = true;
                break;
            }

            winnerCount = checkKicker(players, playerCards, tableArray);
            System.out.println("checkKicker winnerCount: " + winnerCount);
            if (winnerCount > 1) {
                //find kicker or split reward
                gotWinner = true;
                break;
            } else if (winnerCount == 1) {
                //winnerid takes pot
                gotWinner = true;
                break;
            }
            System.out.println("findWinner end");
            break;
        }
        return winnerCount;
    }

    public static String getUserRegQueue(ArrayList[] playersList) {
        String users = "\n";
        for (int i = 0; i < playersList.length; i++) {
            if (
                    playersList[i] != null &&
                            !playersList[i].isEmpty() &&
                            playersList[i].get(4).toString().equals("true")
            ) {
                users = new StringBuilder()
                        .append(users)
                        .append("userName: ")
                        .append(playersList[i].get(1).toString())
                        .append("\nuserId: ")
                        .append(playersList[i].get(0).toString())
                        .append("\n\n").toString();
            }
        }
        return users;

    }



}
