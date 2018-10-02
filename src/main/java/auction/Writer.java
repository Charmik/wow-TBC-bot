package auction;

import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;

import java.util.List;

public class Writer {

    public static void buyItem(WowInstance wowInstance, int index, int price, boolean scanFullAuction) {
        if (scanFullAuction) {
            wowInstance.setMillisForSleeping(300);
        }
        wowInstance.click(WinKey.ENTER);
        if (scanFullAuction) {
            Utils.sleep(300);
        }
        wowInstance.clickEditing(WinKey.SLASH);
        sendMsg(wowInstance, "run");
        wowInstance.clickEditing(WinKey.SPACEBAR);
        sendMsg(wowInstance, "PlaceAuctionBid");
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        wowInstance.clickEditing(WinKey.DOUBLE_QUOTES);
        sendMsg(wowInstance, "list");
        wowInstance.clickEditing(WinKey.DOUBLE_QUOTES);
        wowInstance.clickEditing(WinKey.COMMA);
        Writer.sendMsg(wowInstance, index);
        wowInstance.clickEditing(WinKey.COMMA);
        Writer.sendMsg(wowInstance, price);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);
    }

    public static void useMacroForGettingItemId(WowInstance wowInstance, int bag, int slot) {
        Utils.sleep(2000);
        wowInstance.click(WinKey.ENTER);
        wowInstance.clickEditing(WinKey.SLASH);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.h);
        wowInstance.clickEditing(WinKey.a);
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.m);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        Writer.sendMsg(wowInstance, bag);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        Writer.sendMsg(wowInstance, slot);
        wowInstance.click(WinKey.ENTER);
        Utils.sleep(2000);
    }

    static public void sellItem(WowInstance wowInstance, int bag, int slot, int price) {
        //720 mins - 12hour
        //1440 mins - 24 hour
        //2880 mins - 48 hour

        putItemInAuction(wowInstance, bag, slot);
        Utils.sleep(100);
        wowInstance.click(WinKey.ENTER);
        wowInstance.clickEditing(WinKey.SLASH);
        sendMsg(wowInstance, "run");
        wowInstance.clickEditing(WinKey.SPACEBAR);
        sendMsg(wowInstance, "StartAuction");
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        sendMsg(wowInstance, price);
        wowInstance.clickEditing(WinKey.COMMA);
        sendMsg(wowInstance, price);
        wowInstance.clickEditing(WinKey.COMMA);
        sendMsg(wowInstance, 2880);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);
    }

    private static void putItemInAuction(WowInstance wowInstance, int bag, int slot) {
        ///run PickupContainerItem(bag, slot);
        wowInstance.click(WinKey.ENTER);
        wowInstance.clickEditing(WinKey.SLASH);
        sendMsg(wowInstance, "run");
        wowInstance.clickEditing(WinKey.SPACEBAR);
        sendMsg(wowInstance, "PickupContainerItem");
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        sendMsg(wowInstance, bag);
        wowInstance.clickEditing(WinKey.COMMA);
        sendMsg(wowInstance, slot);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);

        ///run ClickAuctionSellItemButton()
        wowInstance.click(WinKey.ENTER);
        wowInstance.clickEditing(WinKey.SLASH);
        sendMsg(wowInstance, "run");
        wowInstance.clickEditing(WinKey.SPACEBAR);
        sendMsg(wowInstance, "ClickAuctionSellItemButton");
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);
    }


    public static void sendMsg(WowInstance wowInstance, int number) {
        List<WinKey> winKeys = WinKey.mapIntToWinkeys(number);
        for (WinKey winKey : winKeys) {
            wowInstance.clickEditing(winKey);
        }
    }

    public static void sendMsg(WowInstance wowInstance, String message) {
        List<WinKey> winKeys = WinKey.mapStringToWinkeys(message);
        for (WinKey winKey : winKeys) {
            wowInstance.clickEditing(winKey);
        }
    }

    public static void sendMsg(WowInstance wowInstance, char character) {
        wowInstance.clickEditing(WinKey.charToWinKey(character));
    }

    public static void getItemsFromMailbox() {
        // TODO: macros - talk with mailbox -> press take items button
    }
}