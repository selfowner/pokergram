package org.pokedgram;

import java.util.ArrayList;

public class SuperStrings extends PokedgramBot {

    public final String title;

    public SuperStrings(String title) {
        this.title = title;
    }

    public static final String REG_CLOSED = "Registration is not open. ";
    public static final String REG_FAILED = "Register failed. ";
    public static final String REG_FINISHED = "Players registered. Lets roll. ";


    public static final String DOT = ".";



    public static final String PHASE_RIVER_TEXT = "river phase: ";
    public static final String PHASE_FLOP_TEXT = "flop phase: ";
    public static final String PHASE_TURN_TEXT = "turn phase: ";

    public static final String CURRENT_POT_TEXT = "current Pot: ";

    public static final String DECK_SHUFFLE_TEXT = "*Dealer shuffling deck and deals cards*";
    public static final String CARDS_DEALED_TEXT = "cards have been dealed (check pm from @pokedgram"+DOT;

    public static final String ARGUMENT_REGEXP = "^.([a-zA-Z@_ ]*)(.*)$";
    public static final String COMMAND_REGEXP = "^(.[a-zA-Z@_]*) ([0-9]*)(.*)$";
    public static final String BET_REGEXP = "^((allin|number|bet) )([0-9]*)$";

    public static final String AUTODELETE_REGEXP = "^(DELETE_ME)$";
    public static final String AUTODELETE_TEXT = "DELETE_ME";
    public static final String FIND_FLASH_REGEXP = "((.*(♠).*){5}|(.*(♣).*){5}|(.*(♥).*){5}|(.*(♦).*){5})";
    public static final String FIND_FLASH_TABLE_REGEXP = "((.*(♠).*){3}|(.*(♣).*){3}|(.*(♥).*){3}|(.*(♦).*){3})";

    public static final String DISCARD_SINGLE_CARDS_REGEXP = "^\\{.*([0-9]{1,2}=1, ).*([0-9]{1,2}=1, ).*([0-9]{1,2}=1, ).*([0-9]{1,2}=1, ).*([0-9]{1,2}=1, ).*\\}$";

    public static final String COLLECT_DISTINCT_VALUES_WO_BRACES_REGEXP = "^.*([1234567890]{1,2}=[234]).*([1234567890]{1,2}=[234]).*([1234567890]{1,2}=[234]).*$"; // without { }
    //public static final String COLLECT_DISTINCT_VALUES_REGEXP = "^\\{.*([123456789]+=[234]).*\\}$"; //with {}
    //public static final String COLLECT_DISTINCT_VALUES_REGEXP = "^.*([1234567890]+=[234]).*$"; //with {}
    //public static final String COUNT_VALUES_REGEXP = "^.*(=).*$";
    public static final String EXTRACT_DISTINCT_VALUES_REGEXP = "(?:(\\d+=[^234], |\\d+=[^234]))";
    public static final String EXTRACT_VALUES_DIRTY_REGEXP = "(?:([^=]|))";
    public static final String EXTRACT_DISTINCT_COUNT_REGEXP = "\\d+=(?:(\\d+|)),";

    public static final String FIND_FLASH_DRAW_SPADES_REGEXP = "(.*(♠).*){3}";
    public static final String FIND_FLASH_DRAW_CLUBS_REGEXP = "(.*(♣).*){3}";
    public static final String FIND_FLASH_DRAW_HEARTS_REGEXP = "(.*(♥).*){3}";
    public static final String FIND_FLASH_DRAW_DIAMONDS_REGEXP = "(.*(♦).*){3}";

    public static final String NEXT_LINE = "\n";
    public static final String DOUBLE_NEXTLINE = "\n" + "\n";
    public static final String TRIPLE_NEXT_LINE = "\n" + "\n" + "\n";

    public static final boolean ROLL_VISIBILITY = true;
    public static final boolean DEAL_VISIBILITY = false;
    public static final boolean CHECK_PLAYERS_INTERACTION = true;
    public static final int START_CHIPS = 20000;
    public static final int HAND_CARDS_COUNT = 2; // holdem
    public static final int SLEEP_MS = 2000;
    public static final int BLIND_INCREASE_RATE_DEALS = 5;
    public static final int START_SMALL_BLIND_SIZE = 250;
    public static String text = "", currentPlayerHandText = "", allHandsText = "";//, userIdMessage = "", userIdCallback = "";
    public static ArrayList[] playersQueue;
    public static int buttonId = -1;
    public static int registeredPlayers = 0;
    public static int extractNumber;
    public static int maxPlayers;
    public static int bigBlindId = -1;
    public static int smallBlindId = -1;
    public static int smallBlindSize, bigBlindSize;
    public static int dealNumber;
    public static boolean registrationStarted = false;
    public static boolean preGameStarted = false;
    public static boolean gameStarted = false;
    public static boolean phaseStarted = true;
    @Override
    public String toString() {
        return title;
    }


}
