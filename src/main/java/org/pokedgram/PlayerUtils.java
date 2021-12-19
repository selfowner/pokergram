package org.pokedgram;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PlayerUtils extends MyBot {

    public static ArrayList[] addPlayerToQueue(String userId, String userName, ArrayList[] playersQueue, String messageSourceId, int registeredPlayers, int chips, boolean ifFull, int unregid) {
        //check if somebody unregistered
        int updateId = PlayerUtils.checkUserRegExistAndActive(userId, playersQueue, registeredPlayers);

        if (updateId > -1) {
            //boolean checkForUpdate = true;
            //int queueId = ;

            playersQueue[updateId].set(0, userId); // string userId
            playersQueue[updateId].set(1, userName); // string user firstname + lastname
            playersQueue[updateId].set(2, messageSourceId); //chatid
            playersQueue[updateId].set(3, chips); //chipsQuantity
            playersQueue[updateId].set(4, "true"); // isActive for reg/unreg && autofold
            playersQueue[updateId].set(5, "false"); // FoldFlag
            playersQueue[updateId].set(6, "false"); // AllinFlag manual
            playersQueue[updateId].set(7, "false"); // isChipleader?
            playersQueue[updateId].set(8, 0); // currentPhaseBet
            playersQueue[updateId].set(9, 0); // currentRoundBet
            playersQueue[updateId].set(10, 0); // isAutoAllinOnBlind
            playersQueue[updateId].set(11, -1); // placeOnTableFinished, -1 = ingame
        } else {
            if (!ifFull) {
                playersQueue[registeredPlayers] = new ArrayList(12) {
                };

                playersQueue[registeredPlayers].add(0, userId); // string userId
                playersQueue[registeredPlayers].add(1, userName); // string user firstname + lastname
                playersQueue[registeredPlayers].add(2, messageSourceId); //chatid
                playersQueue[registeredPlayers].add(3, chips); //chipsQuantity
                playersQueue[registeredPlayers].add(4, "true"); // isActive for reg/unreg
                playersQueue[registeredPlayers].add(5, "false"); // FoldFlag
                playersQueue[registeredPlayers].add(6, "false"); // AllinFlag manual
                playersQueue[registeredPlayers].add(7, "false"); // isChipleader?
                playersQueue[registeredPlayers].add(8, 0); // currentPhaseBet
                playersQueue[registeredPlayers].add(9, 0); // currentRoundBet
                playersQueue[registeredPlayers].add(10, 0); // isAutoAllinOnBlind
                playersQueue[registeredPlayers].add(11, -1); // placeOnTableFinished, -1 = ingame

            }

        }


        //  ListIterator<String> listIterator = playersQueue[9].listIterator();
        return playersQueue;
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

    public static ArrayList[] checkLastChips(ArrayList[] players, int bigBlindSize) {
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

        System.out.println("foldCount: " + foldCount + "\nallinCount: " + allinCount + "\nmaxBet: " + maxBet + "\nmaxBetCount: " + maxBetCount);
        if (maxBetCount+foldCount>= playersList.length || allinCount == ( maxBetCount + foldCount)  ||  allinCount + foldCount== playersList.length ) {

            return true;
        }

        return false;
    }

    public static ArrayList[] shiftPlayerOrder(ArrayList[] playersList, int offset) {
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


    public static String getUniqueName() {

        ArrayList<String> names = new ArrayList<>(List.of("Aleksandr", "Aleksej", "Andrej", "Artem", "Viktor", "Daniil", "Dmitrij", "Egor", "Ilya", "Kirill", "Maksim", "Mark", "Mihail", "Roman", "Stepan", "Timofej", "Jaroslav", "Bendzhamin", "Dzhejdan", "Dzhejkob", "Dzhejms", "Dzhek", "Itan", "Liam", "Logan", "Lukas", "Mejson", "Mjetju", "Noi", "Oliver", "Ouen", "Rajan", "Jelija", "Aleksandr", "Viljam", "Dzhejdan", "Dzhejkob", "Dzhekson", "Diego", "Dilan", "Djevid", "Djeniel", "Itan", "Liam", "Luis", "Majkl", "Noi", "Sebastjan", "Jedrian", "Jelija", "Amir", "Arsenij", "Artem", "Artur", "Damir", "Danil", "Egor", "Kamil", "Karim", "Kirill", "Maksim", "Marat", "Radmir", "Renat", "Roman", "Rustam", "Samir", "Timur", "Aleksandra", "Alisa", "Anastasija", "Anna", "Arina", "Valerija", "Varvara", "Veronika", "Viktorija", "Darja", "Eva", "Ekaterina", "Elizaveta", "Kira", "Margarita", "Marija", "Polina", "Sofija", "Taisija", "Uljana", "Abigejl", "Ava", "Amelija", "Arija", "Grejs", "Zoi", "Izabella", "Lili", "Medison", "Mija", "Nora", "Obri", "Odri", "Olivija", "Skarlet", "Sofija", "Harper", "Hloja", "Sharlotta", "Jevelin", "Jemili", "Jemma", "Abigejl", "Ava", "Amelija", "Anna", "Viktorija", "Grejs", "Izabella", "Medison", "Mija", "Natali", "Olivija", "Penelopa", "Sofija", "Harper", "Hloja", "Sharlotta", "Jevelin", "Jeveri", "Jelizabet", "Jella", "Jemili", "Jemma", "Azalija", "Alisa", "Alfija", "Amelija", "Amina", "Arina", "Asija", "Valerija", "Viktorija", "Gulnaz", "Darina", "Zarina", "Marijam", "Milana", "Samira", "Sofija", "Polina", "Ralina", "Regina", "Jelvira", "Jasmina"));
        int randomNumb = DeckUtils.anyRandomIntRange(0, names.size() - 1);

        String name = names.get(randomNumb);

        names.remove(randomNumb);
        return name;
    }
}

/*    initArray(playersQueue, maxPlayers);
    public void initArray (ArrayList[] playersQueue, int maxPlayers) {
        */


//    public void sendInlineKeyBoardMessage(Update update) {
//        long chatId = -632153045;
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(); //Создаем объект разметки клавиатуры
//        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton(); //Создаем кнопку
//        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton(); //Создаем кнопку
//        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton(); //Создаем кнопку
//
//        inlineKeyboardButton1.setText("fold"); //Текст самой кнопки
//        inlineKeyboardButton1.setCallbackData("1"); //Отклик на нажатие кнопки
//        inlineKeyboardButton2.setText("check"); //Текст самой кнопки
//        inlineKeyboardButton2.setCallbackData("2"); //Отклик на нажатие кнопки
//        inlineKeyboardButton3.setText("raise"); //Текст самой кнопки
//        inlineKeyboardButton3.setCallbackData("3"); //Отклик на нажатие кнопки
//
//        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
//        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
//        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
//
//        keyboardButtonsRow1.add(inlineKeyboardButton1);
//        keyboardButtonsRow2.add(inlineKeyboardButton2);
//        keyboardButtonsRow3.add(inlineKeyboardButton3);
//
//        List<List<InlineKeyboardButton>> rowList = new ArrayList<>(); //Создаём ряд
//        rowList.add(keyboardButtonsRow1);
//        rowList.add(keyboardButtonsRow2);
//        rowList.add(keyboardButtonsRow3);
//
//        inlineKeyboardMarkup.setKeyboard(rowList);
//
//        //new SendMessage(update.message().chat().id(), getAqi()).replyMarkup(simpleKeyboard());
//        //sendToPlayingRoom(SendMessage()chatId,"123 " + inlineKeyboardButton3.getCallbackData()));
//        //.setText("zxc").setReplyMarkup(inlineKeyboardMarkup);
//
//    }