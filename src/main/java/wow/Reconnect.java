package wow;

import auction.Writer;
import farmbot.launch.ScheduledLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;

public class Reconnect {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledLauncher.class);

    private final WowInstance instance;
    private final String accountName;
    private final String password;

    public Reconnect(WowInstance instance, String accountName, String password) {
        this.instance = instance;
        this.accountName = accountName;
        this.password = password;
    }

    public boolean isDisconnected() {
        return !instance.getPlayer().isInGame();
    }

    public void reconnect() {
        if (!isDisconnected()) {
            logger.error("we tried to reconnect, but we are in the game");
        }
        logger.info("trying to reconnect");
        // sleep BEFORE typing Enter (to close disconnect message)
        Utils.sleep(5_000);
        instance.click(WinKey.ENTER);
        // sleep AFTER typing Enter (to close disconnect message)
        Utils.sleep(5_000);
        Writer.sendMsg(instance, accountName);
        // sleep after typing account name
        Utils.sleep(5_000);
        instance.click(WinKey.TAB);
        // sleep after typing TAB
        Utils.sleep(5_000);
        Writer.sendMsg(instance, password);
        // sleep after typing account password
        Utils.sleep(5_000);
        instance.click(WinKey.ENTER);
        // download character
        Utils.sleep(30_000);
        instance.click(WinKey.ENTER);
        // download world
        Utils.sleep(60_000);
        logger.info("reconnect finished");
    }
}
