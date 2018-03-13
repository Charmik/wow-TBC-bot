package farmbot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Charm on 26/07/2017.
 */
public class Launcher {

    private static Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) throws IOException {
        logger.info(Launcher.class + " started");
        Bot bot = new Bot(args);
        bot.run();
    }
}
