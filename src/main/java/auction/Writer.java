package auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;

public class Writer {

    public static void buyItem(WowInstance wowInstance, int index, int price, boolean scanFullAuction) {
        if (scanFullAuction) {
            wowInstance.setMillisForSleeping(300);
        }
        if (scanFullAuction) {
            Utils.sleep(300);
        }
        wowInstance.click(WinKey.ENTER);
        if (scanFullAuction) {
            Utils.sleep(300);
        }
        wowInstance.clickEditing(WinKey.SLASH);
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        wowInstance.clickEditing(WinKey.P);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.a);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.e);
        wowInstance.clickEditing(WinKey.A);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.o);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.B);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.d);
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        wowInstance.clickEditing(WinKey.DOUBLE_QUOTES);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.s);
        wowInstance.clickEditing(WinKey.t);
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
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        wowInstance.clickEditing(WinKey.S);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.a);
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.A);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.o);
        wowInstance.clickEditing(WinKey.n);
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
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        wowInstance.clickEditing(WinKey.P);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.k);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.p);
        wowInstance.clickEditing(WinKey.C);
        wowInstance.clickEditing(WinKey.o);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.a);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.e);
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.I);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.e);
        wowInstance.clickEditing(WinKey.m);
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        sendMsg(wowInstance, bag);
        wowInstance.clickEditing(WinKey.COMMA);
        sendMsg(wowInstance, slot);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);

        ///run ClickAuctionSellItemButton()
        wowInstance.click(WinKey.ENTER);
        wowInstance.clickEditing(WinKey.SLASH);
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        wowInstance.clickEditing(WinKey.C);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.k);
        wowInstance.clickEditing(WinKey.A);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.o);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.S);
        wowInstance.clickEditing(WinKey.e);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.I);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.e);
        wowInstance.clickEditing(WinKey.m);
        wowInstance.clickEditing(WinKey.B);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.o);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);
    }


    private static void sendMsg (WowInstance wowInstance, int number){
        List<Integer> list = new ArrayList<>();
        if (number == 0) {
            list.add(0);
        }
        while (number > 0) {
            list.add(number % 10);
            number /= 10;
        }
        Collections.reverse(list);
        for (Integer digit : list) {
            wowInstance.clickEditing(WinKey.charToWinKey(Character.forDigit(digit,10)));
        }
    }
    private static void sendMsg (WowInstance wowInstance, String message){
        for (int i = 0; i < message.length(); i++) {
            wowInstance.clickEditing(WinKey.charToWinKey(message.charAt(i)));
        }
    }
    private static void sendMsg (WowInstance wowInstance, char character){
        wowInstance.clickEditing(WinKey.charToWinKey(character));
    }

    public static void getItemsFromMailbox() {
        // TODO: macros - talk with mailbox -> press take items button
    }
}
