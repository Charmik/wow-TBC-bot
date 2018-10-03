package wow;

import auction.Writer;
import farmbot.launch.ScheduledLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;

public class Reconnect {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledLauncher.class);
    private final WowInstance instance = WowInstance.getInstance();
    private final String accountName;
    private final String password;

    public Reconnect(String accountName, String password) {
        this.accountName = accountName;
        this.password = password;
    }

    public boolean isDisconnected() {
        return !instance.getPlayer().isInGame();
    }

    public void reconnect() {
        // sleep BEFORE typing Enter (to close disconnect message)
        Utils.sleep(5_000);
        instance.click(WinKey.ENTER);
        // sleep AFTER typing Enter (to close disconnect message)
        Utils.sleep(5_000);
        Writer.sendMsg(instance, accountName);
        // sleep after typing account name
        Utils.sleep(20_000);
        instance.click(WinKey.TAB);
        // sleep after typing TAB
        Utils.sleep(5_000);
        Writer.sendMsg(instance, password);
        // sleep after typing account password
        Utils.sleep(20_000);
        instance.click(WinKey.ENTER);
        // sleep after typing Enter
        Utils.sleep(20_000);
        instance.click(WinKey.ENTER);
        // sleep after typing Enter (pick account player)
        Utils.sleep(5_000);
        // loading sleep
        Utils.sleep(30_000);
    }

    public static void main(String[] args) {
        Reconnect rec = new Reconnect("YourAccountName", "YourPassword");
        if (rec.isDisconnected())
            rec.reconnect();
    }
}
