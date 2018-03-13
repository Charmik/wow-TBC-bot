package farmbot;

import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;

/**
 * Created by charm on 10.08.2017.
 */
public class Account {


    private WowInstance wowInstance;

    public Account(WowInstance wowInstance) {

        this.wowInstance = wowInstance;
    }

    public void writeName() {
        //TODO: try to make by using String, (can using reflection for example) + use config
        Utils.sleep(100);
        wowInstance.click(WinKey.S);
        Utils.sleep(100);
        wowInstance.click(WinKey.P);
        Utils.sleep(100);
        wowInstance.click(WinKey.A);
        Utils.sleep(100);
        wowInstance.click(WinKey.L);
        Utils.sleep(100);
        wowInstance.click(WinKey.O);
        Utils.sleep(100);
        wowInstance.click(WinKey.N);
        Utils.sleep(100);
        wowInstance.click(WinKey.A);
        Utils.sleep(100);
        wowInstance.click(WinKey.K);
        Utils.sleep(100);
        wowInstance.click(WinKey.U);
        Utils.sleep(100);
        wowInstance.click(WinKey.R);
        Utils.sleep(100);
        wowInstance.click(WinKey.A);
        Utils.sleep(100);
    }

    public void writePassword() {
        Utils.sleep(100);
        wowInstance.click(WinKey.C);
        Utils.sleep(100);
        wowInstance.click(WinKey.H);
        Utils.sleep(100);
        wowInstance.click(WinKey.A);
        Utils.sleep(100);
        wowInstance.click(WinKey.R);
        Utils.sleep(100);
        wowInstance.click(WinKey.M);
        Utils.sleep(100);
        wowInstance.click(WinKey.I);
        Utils.sleep(100);
        wowInstance.click(WinKey.K);
        Utils.sleep(100);
        wowInstance.click(WinKey.D1);
        Utils.sleep(100);
    }
}
