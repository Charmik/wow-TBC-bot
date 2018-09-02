package telegram;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

/**
 * @author alexlovkov
 */
public class TelegramBot {

    private static String PROXY_HOST = "35.231.32.168" /* proxy host */;
    private static Integer PROXY_PORT = 80 /* proxy port */;

    private WriterBot bot;

    public TelegramBot() {
        ApiContextInitializer.init();

        TelegramBotsApi api = new TelegramBotsApi();

        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        HttpHost httpHost = new HttpHost(PROXY_HOST, PROXY_PORT);

        RequestConfig requestConfig = RequestConfig.custom().setProxy(httpHost).setAuthenticationEnabled(true).build();
        botOptions.setRequestConfig(requestConfig);
        botOptions.setHttpProxy(httpHost);

        this.bot = new WriterBot(botOptions);

        try {
            api.registerBot(bot);
        } catch (TelegramApiRequestException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMessageToCharm(String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(150789681L); //Charm
        message.setText(msg);
        try {
            bot.execute(message);
        } catch (TelegramApiException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMessageToShumik(String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(146395526L); //Shumik
        message.setText(msg);
        try {
            bot.execute(message);
        } catch (TelegramApiException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMessageToBoth(String msg) {
        sendMessageToCharm(msg);
        sendMessageToShumik(msg);
    }
}
