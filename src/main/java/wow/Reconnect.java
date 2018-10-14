package wow;

import java.util.concurrent.ThreadLocalRandom;

import auction.Account;
import auction.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.Client;
import util.Utils;
import winapi.components.WinKey;

public class Reconnect {

    private static final Logger logger = LoggerFactory.getLogger(Reconnect.class);
    private static final int SLEEP_AFTER_DISCONNECT = 1000 * 60 * 5;

    private final WowInstance instance;
    private final Account account;
    private final Client client;

    public Reconnect(WowInstance instance, Account account, Client client) {
        this.instance = instance;
        this.account = account;
        this.client = client;
    }

    public boolean isDisconnected() {
        return !instance.getPlayer().isInGame();
    }

    public boolean reconnect() {
        if (!isDisconnected()) {
            logger.error("we tried to reconnect, but we are in the game");
            return false;
        }
        logger.info("starting to reconnect");
        client.sendPhotoAndMessage("starting to reconnect");
        // sleep BEFORE typing Enter (to close disconnect message)
        Utils.sleep(5_000);
        instance.click(WinKey.ENTER);
        // sleep AFTER typing Enter (to close disconnect message)
        removeFields();
        Writer.sendMsg(instance, account.getAccountName(), 300);
        // sleep after typing account name
        Utils.sleep(5_000);
        instance.click(WinKey.TAB);
        // sleep after typing TAB
        removeFields();
        Writer.sendMsg(instance, account.getPassword(), 300);
        // sleep after typing account password
        Utils.sleep(5_000);
        instance.click(WinKey.ENTER);
        // download character
        Utils.sleep(30_000);
        instance.click(WinKey.ENTER);
        // download world
        Utils.sleep(60_000);
        logger.info("reconnect finished");
        instance.updateFields();
        logger.info("player coordinates:{}", instance.getPlayer().getCoordinates());
        client.sendPhotoAndMessage("reconnected");
        return true;
    }

    private void removeFields() {
        Utils.sleep(5_000);
        for (int i = 0; i < 50; i++) {
            //instance.clickEditing(WinKey.BACKSPACE);
            instance.click(WinKey.BACKSPACE);
            Utils.sleep(50);
        }
        Utils.sleep(5_000);
    }

    public boolean checkAndReconnect() {
        if (isDisconnected()) {
            int sleepMillis = ThreadLocalRandom.current().nextInt(SLEEP_AFTER_DISCONNECT);
            logger.info("was disconnect sleep for:{} minutes", (SLEEP_AFTER_DISCONNECT + sleepMillis) / 1000 / 60);
            client.sendPhotoAndMessage("is disconnected");
            Utils.sleep(SLEEP_AFTER_DISCONNECT);
            Utils.sleep(sleepMillis);
            return reconnect();
        }
        return false;
    }
}
