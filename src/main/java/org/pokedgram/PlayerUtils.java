package org.pokedgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.pokedgram.DeckUtils.*;
import static org.pokedgram.SuperStrings.*;

public class PlayerUtils extends PokedgramBot {
    //TODO() create type "Player"
    public static HashMap[] addPlayerToQueue(String userId, String userName, HashMap[] playerDataPregame, String userNick, int registeredPlayers, int chips, boolean ifFull) {

        int updateId = PlayerUtils.checkUserRegExistAndActive(userId, playerDataPregame, registeredPlayers);
        int playerRegId;
        if (updateId > -1) {
            playerRegId = updateId;
        } else {
            playerRegId = registeredPlayers;

        }

        if (!ifFull) {
            //playerInfoPregame[ playerRegId ] = new HashMap<String, String>() {};
            playerDataPregame[ playerRegId ] = new HashMap<String, Integer>() {};
            playerDataPregame[ playerRegId ].put(USER_ID, userId); //0 string userId
            playerDataPregame[ playerRegId ].put(USER_NAME, userName); //1 string user firstname + lastname
            playerDataPregame[ playerRegId ].put(USER_NICKNAME, userNick); //2
            playerDataPregame[ playerRegId ].put(CHIPS_VALUE, chips); //3 chipsQuantity
            playerDataPregame[ playerRegId ].put(ISACTIVE, 1); //4 isActive for reg/unreg
            playerDataPregame[ playerRegId ].put(FOLD_FLAG, 0); //5 FoldFlag
            playerDataPregame[ playerRegId ].put(ALLIN_FLAG, 0); //6 AllinFlag manual
            playerDataPregame[ playerRegId ].put(RAISE_FLAG, 0); //7 isChipleader?
            playerDataPregame[ playerRegId ].put(CHECK_FLAG, 0); //12 check this stage, true = active
            playerDataPregame[ playerRegId ].put(BET_STAGE, 0); //8 currentStageBet
            playerDataPregame[ playerRegId ].put(BET_ROUND, 0); //9 currentRoundBet
            playerDataPregame[ playerRegId ].put(COMBOPOWER_VALUE, -1); //13 str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0
            playerDataPregame[ playerRegId ].put(KICKER1_VALUE, -1); //14
            playerDataPregame[ playerRegId ].put(KICKER2_VALUE, -1); //15
            playerDataPregame[ playerRegId ].put(ISAUTOALLINONBLIND, 0); //10 isAutoAllinOnBlind
            playerDataPregame[ playerRegId ].put(PLACEONTABLEFINISHED, -1); //11 placeOnTableFinished, -1 = ingame


        }
        return playerDataPregame;
    }

    public static void clearPlayerBets(HashMap[] playerDataIngame) {
        for (HashMap player : playerDataIngame) {
            player.put(BET_ROUND, 0);
            player.put(BET_STAGE, 0);
        }
    }

    public static void processPlayerBetsFromStageToRound(HashMap[] playerDataIngame) {
        for (HashMap player : playerDataIngame) {
            player.put(BET_ROUND, Integer.parseInt(player.get(BET_ROUND).toString()) + Integer.parseInt(player.get(BET_STAGE).toString()));
            player.put(BET_STAGE, 0);
        }
    }

    public static void setPlayerStageFlag(HashMap[] playerDataIngame, int moveCount, String flag) {
        if (flag.equals("check flag")) {
            playerDataIngame[ moveCount ].put(CHECK_FLAG, 1);
            return;
        }

        if (flag.equals("fold flag")) {
            playerDataIngame[ moveCount ].put(FOLD_FLAG, 1);
            return;
        }

        if (flag.equals("allin flag")) {
            playerDataIngame[ moveCount ].put(ALLIN_FLAG, 1);
        }
        if (flag.equals("raise flag")) {
            playerDataIngame[ moveCount ].put(RAISE_FLAG, 1);
        }

    }


    public static void unsetRoundResult(HashMap[] playerDataIngame) {
        //for (ArrayList player : playerDataIngame) {
        for (int playerNumber = 0; playerNumber < playerDataIngame.length; playerNumber++) {
            playerDataIngame[ playerNumber ].put(COMBOPOWER_VALUE, -1);
            System.out.println("unsetRoundResult: " + playerNumber + NEXTLINE + playerDataIngame[ playerNumber ].get(COMBOPOWER_VALUE));
        }
    }


    public static void unsetAllPlayerStageFlag(HashMap[] playerDataIngame, String flag) {

        System.out.println("unsetAllPlayerStageFlag: ");
        //for (ArrayList player : playerDataIngame) {
        for (int playerNumber = 0; playerNumber < playerDataIngame.length; playerNumber++) {
            if (flag.equals("check flag")) {
                playerDataIngame[ playerNumber ].put(CHECK_FLAG, 0);
                System.out.println("unset check flag (12): player " + playerNumber + NEXTLINE + playerDataIngame[ playerNumber ].get(CHECK_FLAG));
            }

            if (flag.equals("fold flag")) {
                playerDataIngame[ playerNumber ].put(FOLD_FLAG, 0);
                System.out.println("unset fold flag (5): player " + playerNumber + NEXTLINE + playerDataIngame[ playerNumber ].get(FOLD_FLAG));
            }

            if (flag.equals("allin flag")) {
                playerDataIngame[ playerNumber ].put(ALLIN_FLAG, 0);
                System.out.println("unset allin flag (6): player " + playerNumber + NEXTLINE + playerDataIngame[ playerNumber ].get(ALLIN_FLAG));
            }

            if (flag.equals("bets flag")) {
                playerDataIngame[ playerNumber ].put(ALLIN_FLAG, 0);
                System.out.println("unset bets flag (6): player " + playerNumber + NEXTLINE + playerDataIngame[ playerNumber ].get(ALLIN_FLAG));
            }

            if (flag.equals("clean all")) {
                playerDataIngame[ playerNumber ].put(ALLIN_FLAG, 0);
                playerDataIngame[ playerNumber ].put(FOLD_FLAG, 0);
                playerDataIngame[ playerNumber ].put(CHECK_FLAG, 0);
                System.out.println(
                        "unset clean all (6 5 12): player " + playerNumber + NEXTLINE +
                        playerDataIngame[ playerNumber ].get(ALLIN_FLAG) + NEXTLINE +
                        playerDataIngame[ playerNumber ].get(FOLD_FLAG) + NEXTLINE +
                        playerDataIngame[ playerNumber ].get(CHECK_FLAG)
                );
                //playerDataIngame[ i ].put(COMBOPOWER_VALUE, -1);
            }
        }
    }

    public static boolean unsetPlayerStageFlag(HashMap[] playerDataIngame, int moveCount, String flag) {
        if (flag.equals("check flag")) {
            playerDataIngame[ moveCount ].put(CHECK_FLAG, 0);
            return true;
        }

        if (flag.equals("fold flag")) {
            playerDataIngame[ moveCount ].put(FOLD_FLAG, 0);
            return true;
        }

        if (flag.equals("allin flag")) {
            playerDataIngame[ moveCount ].put(ALLIN_FLAG, 0);
            return true;
        }

        if (flag.equals("clean all")) {
            playerDataIngame[ moveCount ].put(ALLIN_FLAG, 0);
            playerDataIngame[ moveCount ].put(FOLD_FLAG, 0);
            playerDataIngame[ moveCount ].put(CHECK_FLAG, 0);
            return true;
        }
        return false;
    }


    public static int checkFoldsQuantityThisRound(HashMap[] playerDataIngameList) {
        int foldCount = 0;
        for (int iteratePlayer = playerDataIngameList.length - 1; iteratePlayer >= 0; iteratePlayer--) {
            if (Objects.equals(playerDataIngameList[ iteratePlayer ].get(FOLD_FLAG).toString(), "1")) { // fold flag
                foldCount++;
            }

        }
        return foldCount;
    }

    public static int checkAllinsQuantityThisRound(HashMap[] playerDataIngameList) {
        int allinCount = 0;
        for (int iteratePlayer = playerDataIngameList.length - 1; iteratePlayer >= 0; iteratePlayer--) {
            if (Objects.equals(playerDataIngameList[ iteratePlayer ].get(ALLIN_FLAG).toString(), "1")) { // fold flag
                allinCount++;
            }
        }
        return allinCount;
    }

//get current bets
/*    public static String getCurrentBets(ArrayList[] playerDataIngame) {
        String output = EMPTY_STRING;

        new String(
                "1." + playerDataIngame[ smallBlindId ].get(2) + " bet: " + playerDataIngame[ smallBlindId ].get(BET_ROUND) + ". " +
                "Balance " + playerDataIngame[ smallBlindId ].get(CHIPS_VALUE) + NEXT_LINE +
                "2." + playerDataIngame[ bigBlindId ].get(2) + " bet: " + playerDataIngame[ bigBlindId ].get(BET_ROUND) + ". " +
                "Balance " + playerDataIngame[ bigBlindId ].get(CHIPS_VALUE));

        for (int i = 0; i < playerDataIngame.length; i++) {

            output += playerDataIngame[ smallBlindId ].get(2);



            } else {
                //all ok
            }
        }

        return playerDataIngame;


        return output;
    }*/

    public static HashMap[] checkLastChips(HashMap[] playerDataIngame, int bigBlindSize) {
        for (int iteratePlayer = 0; iteratePlayer < playerDataIngame.length; iteratePlayer++) {
            if (Integer.parseInt(playerDataIngame[ iteratePlayer ].get(CHIPS_VALUE).toString()) < bigBlindSize) {
                playerDataIngame[ iteratePlayer ].put(ISAUTOALLINONBLIND, 1);
                if (Integer.parseInt(playerDataIngame[ iteratePlayer ].get(CHIPS_VALUE).toString()) < bigBlindSize / 2) {
                    playerDataIngame[ iteratePlayer ].put(ISAUTOALLINONBLIND, 2);
                }
                if (Integer.parseInt(playerDataIngame[ iteratePlayer ].get(CHIPS_VALUE).toString()) == 0) {
                    playerDataIngame[ iteratePlayer ].put(ISAUTOALLINONBLIND, iteratePlayer);
                }
            } else {
                //all ok
            }
        }
        return playerDataIngame;
    }

    public static ArrayList<?>[] removeZeroBalance(ArrayList<Integer>[] playerDataIngame) {
        IntStream.range(0, playerDataIngame.length).filter(i -> !playerDataIngame[ i ].isEmpty() && Integer.parseInt(playerDataIngame[ i ].get(Integer.parseInt(ISAUTOALLINONBLIND)).toString()) > -1).forEach(i -> playerDataIngame[ i ] = null);

        //without refactor
//        for (int i = 0; i < playerDataIngame.length; i++) {
//            if (!playerDataIngame[ i ].isEmpty() && Integer.parseInt(playerDataIngame[ i ].get(ISAUTOALLINONBLIND).toString()) > -1) {
//                playerDataIngame[ i ] = null;
//            }
//        }
//        return playerDataIngame;

        return playerDataIngame;
    }

    public static int findMaxBet(HashMap[] playerDataIngameList) {


        int maxBet = 0;
        for (HashMap objects : playerDataIngameList) {
            if (maxBet <= Integer.parseInt(objects.get(BET_ROUND).toString())) {
                maxBet = Integer.parseInt(objects.get(BET_ROUND).toString());
            }
        }


        return maxBet;
    }

    public static boolean checkBetsEqual(HashMap[] playerDataIngameList) {

        int foldCount = checkFoldsQuantityThisRound(playerDataIngameList);
        int allinCount = checkAllinsQuantityThisRound(playerDataIngameList);
        int maxBet = 0;
        int maxBetCount = 0;
        for (HashMap objects : playerDataIngameList) {
            if (maxBet == Integer.parseInt(objects.get(BET_ROUND).toString())) {

                maxBetCount++;
            }

            if (maxBet < Integer.parseInt(objects.get(BET_ROUND).toString())) {

                maxBetCount = 0;
                maxBet      = Integer.parseInt(objects.get(BET_ROUND).toString());
                maxBetCount++;
            }
        }

        //System.out.println("checkBetsEqual - foldCount: " + foldCount + "\nallinCount: " + allinCount + "\nmaxBet:
        // " + maxBet + "\nmaxBetCount: " + maxBetCount);
        if (maxBetCount + foldCount >= playerDataIngameList.length || allinCount == (maxBetCount + foldCount) || allinCount + foldCount == playerDataIngameList.length) {

            return true;
        }

        return false;
    }

    public static HashMap[] shiftPlayerOrder(HashMap[] playerDataIngameList, int offset) {
        //System.out.println("shiftPlayerOrder started. playerDataIngamelist size: " + playerDataIngameList.length + ". offset: " + offset);
        //      try {
        HashMap[] newPlayersList = new java.util.HashMap[ playerDataIngameList.length ];
        int last = -1;

        if (offset > 0 && playerDataIngameList.length >= 2) { // no changes
            //System.out.println("offset > 0 && playerDataIngameList.length >=2");

            if (offset == 1) {
                //System.out.println("offset == 1");
                if (playerDataIngameList.length == 2) {

                    //System.out.println("playerDataIngameList.length == 2");
                    newPlayersList[ 0 ] = playerDataIngameList[ 1 ];
                    newPlayersList[ 1 ] = playerDataIngameList[ 0 ];
                    //System.out.println("id [0]: " + newPlayersList[0].get(USERNAME));
                    //System.out.println("id [1]: " + newPlayersList[1].get(USERNAME));
                    return newPlayersList;
                }

/*                if (playerDataIngameList.length > 2) {

                    System.out.println("playerDataIngameList.length > 2");
                    for (int forward = 0; (offset + forward) < playerDataIngameList.length; forward++) {
                        if ((offset+forward) == playerDataIngameList.length) {
                            System.out.println("(offset+forward) == playerDataIngameList.length");

                            newPlayersList[forward] = playerDataIngameList[0];
                        } else {
                            System.out.println("!((offset+forward) == playerDataIngameList.length)");
                            newPlayersList[forward] = playerDataIngameList[offset + forward];
                        }
                        last = forward;
                        System.out.println(newPlayersList[forward].get(USERNAME) + " equals " + playerDataIngameList[offset + forward].get(USERNAME));
                    }
                    if (last != playerDataIngameList.length) {

                        System.out.println("last != playerDataIngameList.length");
                        for (int backward = offset - 1; backward > 0; backward--) {
                            newPlayersList[last + backward] = playerDataIngameList[last + backward];
                            System.out.println(newPlayersList[last+backward].get(USERNAME) + " equals " + playerDataIngameList[last + backward].get(USERNAME));
                        }
                    }

                    //System.out.println("rollWinnerPlayerId > 0 && playerDataIngameList.length == 2");
                    return newPlayersList;
                }*/
            }
            return playerDataIngameList;
        }
        return playerDataIngameList;
        //if (offset > 0 && playerDataIngameList.length > 2) { //

    }



    public static int checkFoldCount(HashMap[] playerDataIngame) {
        int c = 0;
        for (HashMap player : playerDataIngame) {

            if (player.get(FOLD_FLAG).equals("1")) { // && ) {
                c++;
            }
        }
        return c;
    }




    public static ArrayList[] userUnreg(ArrayList[] playerDataIngameQueue, String userId) {

        for (int z = 0; z < playerDataIngameQueue.length - 1; z++) {
            if (playerDataIngameQueue[z] != null && playerDataIngameQueue[z].get(0) == userId) {
                //playerDataIngameQueue[z].put(4, T);
            }
        }
        return playerDataIngameQueue;

    }
    public static int checkUserRegExistAndActive(String userId, HashMap[] playerDataIngameQueue, int registeredPlayers) {

        for (int iteratePlayer = 0; iteratePlayer < registeredPlayers; iteratePlayer++) {

            if (playerDataIngameQueue[ iteratePlayer ] != null && playerDataIngameQueue[ iteratePlayer ].get(USER_ID).equals(userId) && playerDataIngameQueue[ iteratePlayer ].get(ISACTIVE).equals("1")) { // && ) {
                return iteratePlayer;
            }
        }
        return -1;
    }
    public static int checkUserUnReg(HashMap[] playerDataIngameQueue) {


        for (int iteratePlayer = 0; iteratePlayer < playerDataIngameQueue.length - 1; iteratePlayer++) {
            if (playerDataIngameQueue[ iteratePlayer ] != null && Objects.equals(playerDataIngameQueue[ iteratePlayer ].get(ISACTIVE), "0")) {

                return iteratePlayer;
            }
        }
        return -1; //ok

    }
    public static boolean checkUserLimit(int maxPlayers, int registeredPlayers) {

        return registeredPlayers >= maxPlayers;
    }
    public static String getUserReg(ArrayList<?>[] playerDataIngameQueue, int registeredPlayersCount, String userId) {
        StringBuilder users = new StringBuilder(EMPTY_STRING);
        for (int iteratePlayer = 0; iteratePlayer < registeredPlayersCount; iteratePlayer++) {
            if (playerDataIngameQueue[ iteratePlayer ].get(Integer.parseInt(USER_ID)) != null &&
                Objects.equals(playerDataIngameQueue[ iteratePlayer ].get(Integer.parseInt(userId)), Integer.parseInt(userId))) {
                users.append("\n").append(playerDataIngameQueue[ iteratePlayer ].get(Integer.parseInt(USER_NAME)).toString());
            }
        }
        return users.toString();

    }

    public static HashMap[] getShowdownWinnerId(HashMap[] playerDataIngame, ArrayList<String>[][] playerCards, List<?> cardDeck) {
        int winnerId, winnerCount = -1;
        // int winnerCount = -1;

        ArrayList<?>[] tableArray = drawTable(playerDataIngame.length, cardDeck);
        if (false) {
            //sidepot
        }
        boolean gotWinner = false;

        //calculate winner and process chips
        while (!gotWinner) {

            //System.out.println("findWinner start");

            // TODO count kickers on table for some cases
            winnerCount += checkDistinct(playerCards, playerDataIngame);
            winnerCount += checkFlash(playerDataIngame, playerCards, tableArray);
            winnerCount += checkStraight(playerCards);

            System.out.println("checkDistinct winnerCount after checkDistinct checkFlash checkStraight: " + winnerCount);
            //str fl 8 //quad 7 //fullhouse 6 //flash 5 //straight 4 //triple 3 //two pair 2 //pair 1 //high card 0
            if (Integer.parseInt(playerDataIngame[ 0 ].get(COMBOPOWER_VALUE).toString()) >
                Integer.parseInt(playerDataIngame[ 1 ].get(COMBOPOWER_VALUE).toString())) {
                System.out.println("player 0 win");
                winnerId = 0; //player 0 win
            } else if (Integer.parseInt(playerDataIngame[ 1 ].get(COMBOPOWER_VALUE).toString()) >
                       Integer.parseInt(playerDataIngame[ 0 ].get(COMBOPOWER_VALUE).toString())) {

                System.out.println("player 1 win");

                winnerId = 1; //player 1 win
            } else if ( // comboValue ==
                    Integer.parseInt(playerDataIngame[ 0 ].get(COMBOPOWER_VALUE).toString()) ==
                    Integer.parseInt(playerDataIngame[ 1 ].get(COMBOPOWER_VALUE).toString()) &&
                    (Integer.parseInt(playerDataIngame[ 0 ].get(COMBOPOWER_VALUE).toString()) > -1 &&
                     Integer.parseInt(playerDataIngame[ 1 ].get(COMBOPOWER_VALUE).toString()) > -1)
            ) {
                System.out.println("playerDataIngame tie, checking kicker:");
                try {
                    winnerCount = checkKicker(playerDataIngame, playerCards, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // winnerCount = checkKicker(playerDataIngame, playerCards, tableArray);


//            if (winnerCount > 0) {
//                System.out.println("checkDistinct winnerCount: " + winnerCount);
//                //find kicker or split reward
//                gotWinner = true;
//                break;
//            }

            //System.out.println("findWinner end");
            break;
        }

        int playerIncome = getSplitRewardSize(currentPot, playerDataIngame);
        if (winnerCount > 1) {
            for (HashMap player : playerDataIngame) {
                player.put(CHIPS_VALUE, Integer.parseInt(player.get(CHIPS_VALUE).toString()) + playerIncome);
                currentPot = 0;
            }
            System.out.println("winnerCount > 1; income " + playerIncome);
            gotWinner = true;
            // process chips
        } else if (winnerCount == 0) {
            System.out.println("winnerCount == 0; no combo found. Splitting pot, setting gotWinner true " + playerIncome);
            for (HashMap player : playerDataIngame) {
                player.put(CHIPS_VALUE, Integer.parseInt(player.get(CHIPS_VALUE).toString()) + playerIncome);
                currentPot = 0;
                gotWinner  = true;
            }
        } else if (winnerCount == 1) {
            System.out.println("winnerCount == 1; we have winner");
            //get winnerId
        }

        return playerDataIngame;
    }


    public static String getUserRegQueue(HashMap[] playerDataIngameList) {
        StringBuilder users = new StringBuilder("\n");
        for (HashMap strings : playerDataIngameList) {
            if (
                    strings != null &&
                    !strings.isEmpty() &&
                    strings.get(ISACTIVE).equals("1")
            ) {
                users = new StringBuilder("userName: " + users + strings.get(USER_NAME) + "\nuserId: " + strings.get(USER_ID) + "\n\n");
            }
        }
        return users.toString();

    }


}
