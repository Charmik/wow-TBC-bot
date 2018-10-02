package wow;

import auction.Writer;
import farmbot.launch.ScheduledLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import winapi.components.WinKey;

public class Reconnect {

    private WowInstance instance = WowInstance.getInstance();
    private static Logger logger = LoggerFactory.getLogger(ScheduledLauncher.class);

    public boolean isDisconnected() {
        return !instance.getPlayer().isInGame();
    }

    public void reconnect(String accountName, String password) {
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep before typing Enter (to close disconnect message)");
            e.printStackTrace();
        }
        instance.click(WinKey.ENTER);
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep after typing Enter (to close disconnect message)");
            e.printStackTrace();
        }
        Writer.sendMsg(instance, accountName);
        try {
            Thread.sleep(20_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep after typing account name");
            e.printStackTrace();
        }
        instance.click(WinKey.TAB);
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep after typing TAB");
            e.printStackTrace();
        }
        Writer.sendMsg(instance, password);
        try {
            Thread.sleep(20_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep after typing account password");
            e.printStackTrace();
        }
        instance.click(WinKey.ENTER);
        try {
            Thread.sleep(20_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep after typing Enter");
            e.printStackTrace();
        }
        instance.click(WinKey.ENTER);
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            logger.error("Missing sleep after typing Enter (pick account player)");
            e.printStackTrace();
        }
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            logger.error("Missing loading sleep");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Reconnect rec = new Reconnect();
        if (rec.isDisconnected())
            rec.reconnect("YourAccountName", "YourPassword");
    }
}
