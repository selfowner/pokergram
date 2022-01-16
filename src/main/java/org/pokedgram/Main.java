package org.pokedgram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {

    public void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new PokedgramBot());

        /*TODO() start bots by request
        botsApi.registerBot(new PokedgramCashierBot());
        botsApi.registerBot(new AssCinema());*/
    }
}
