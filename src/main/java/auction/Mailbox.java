package auction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;

/**
 * @author alexlovkov
 */
public class Mailbox {

    private static final Logger logger = LoggerFactory.getLogger(Mailbox.class);

    private static final int COUNT_TAKE_MAILBOX = 2;
    private static final int SLEEP_AFTER_LOGOUT = 5 * 1000;
    private static final int SLEEP_FOR_GETTING_ITEMS_FROM_MAILBOX = 5 * 1000;

    private final WowInstance wowInstance;

    public Mailbox(WowInstance wowInstance) {
        this.wowInstance = wowInstance;
    }

    public void getMail() {
        logger.info("get mail");
        for (int i = 0; i < COUNT_TAKE_MAILBOX; i++) {
            /*
            /script MailFrame:Show()
            /click OpenAllButton
            /click OpenAllButton2
             */
            wowInstance.click(WinKey.D2);
            Utils.sleep(1000);
            wowInstance.click(WinKey.D2);

            Utils.sleep(SLEEP_FOR_GETTING_ITEMS_FROM_MAILBOX);
            // /reload macros
            wowInstance.click(WinKey.D0);
            Utils.sleep(SLEEP_AFTER_LOGOUT);
        }
    }
}
