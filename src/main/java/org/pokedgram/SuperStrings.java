package org.pokedgram;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.core.util.Loader.getResourceBySelfClassLoader;

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
    public static final String BET_REGEXP = "^(number )([0-9]*)$";
    public static final String AUTODELETE_REGEXP = "^(DELETE_ME.*)$";


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
    public static boolean tradePreflopDone = false;
    public static boolean tradeFlopDone = false;
    public static boolean tradeTurnDone = false;
    public static boolean tradeRiverDone = false;
    public static boolean isTradePreflopDone = false;
    public static boolean isTradeFlopDone = false;
    public static boolean isTradeRiverDone = false;

    @Override
    public String toString() {
        return title;
    }


}
