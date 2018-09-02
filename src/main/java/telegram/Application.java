package telegram;

public class Application {

    public static void main(String... args) {
        TelegramBot bot = new TelegramBot();
        bot.sendMessageToCharm("ti lox");
    }
}
