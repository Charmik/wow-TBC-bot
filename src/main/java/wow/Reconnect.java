package wow;

import auction.Account;
import auction.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;

public class Reconnect {

    private static final Logger logger = LoggerFactory.getLogger(Reconnect.class);
    private static final long SLEEP_AFTER_DISCONNECT = 1000 * 60 * 15;

    private final WowInstance instance;
    private final Account account;

    public Reconnect(WowInstance instance, Account account) {
        this.instance = instance;
        this.account = account;
    }

    public boolean isDisconnected() {
        return !instance.getPlayer().isInGame();
    }

    public void reconnect() {
        if (!isDisconnected()) {
            logger.error("we tried to reconnect, but we are in the game");
            return;
        }
        logger.info("trying to reconnect");
        // sleep BEFORE typing Enter (to close disconnect message)
        Utils.sleep(5_000);
        instance.click(WinKey.ENTER);
        // sleep AFTER typing Enter (to close disconnect message)
        removeFields();
        Writer.sendMsg(instance, account.getAccountName(), 200);
        // sleep after typing account name
        Utils.sleep(5_000);
        instance.click(WinKey.TAB);
        // sleep after typing TAB
        removeFields();
        Writer.sendMsg(instance, account.getPassword(), 200);
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

    private void removeFields() {
        Utils.sleep(5_000);
        for (int i = 0; i < 30; i++) {
            instance.clickEditing(WinKey.BACKSPACE);
        }
        Utils.sleep(5_000);
    }

    public void checkAndReconnect() {
        if (isDisconnected()) {
            logger.info("was disconnect");
            Utils.sleep(SLEEP_AFTER_DISCONNECT);
            reconnect();
        }
    }
}
