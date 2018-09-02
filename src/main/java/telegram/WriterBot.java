package telegram;


import java.util.concurrent.ThreadLocalRandom;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class WriterBot extends AbilityBot {
    public static final String BOT_TOKEN = "605258699:AAG3EeFM-ETkvJ0NivCGs7K8YpNWAwtYqUE";
    public static final String BOT_USERNAME = "wowTbcBot" + ThreadLocalRandom.current().nextInt();

    public WriterBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    public WriterBot(DefaultBotOptions botOptions) {
        super(BOT_TOKEN, BOT_USERNAME, botOptions);
    }

    @Override
    public int creatorId() {
        return 605258699;
    }

    public Ability sayHelloWorld() {
        return Ability
            .builder()
            .name("hello")
            .info("says hello world!")
            .locality(ALL)
            .privacy(PUBLIC)
//            .action(ctx -> sender.sender.send("Hello world!", ctx.chatId()))
            .action(ctx -> {
                System.out.println("ctx.chatId() " + ctx.chatId());
                silent.send("Hello world!", ctx.chatId());
            })
            .build();
    }


}
