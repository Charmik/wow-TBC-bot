package wow;

import com.sun.jna.platform.win32.WinDef.HWND;
import util.Utils;
import winapi.Win32api;
import winapi.components.WinKey;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.WowMemory;
import wow.memory.objects.Player;

/**
 * @author Cargeh
 */
public final class WowInstance {
    private final HWND hwnd;
    private final WowMemory wowMemory;

    public WowInstance(String windowName) {
        hwnd = Win32api.getProcessHwnd(windowName);
        int processId = Win32api.getProcessId(hwnd);

        this.wowMemory = new WowMemory(processId);
    }

    public Player getPlayer() {
        return wowMemory.getPlayer();
    }

    public ObjectManager getObjectManager() {
        return wowMemory.getObjectManager();
    }

    public CtmManager getCtmManager() {
        return wowMemory.getCtmManager();
    }

    public void click(WinKey key) {
        keyDown(key);
        Utils.sleep(50);
        keyUp(key);
    }

    public void click(
        WinKey key,
        long duration)
    {
        keyDown(key);
        Utils.sleep(duration);
        keyUp(key);
    }

    public void keyDown(WinKey key) {
        Win32api.keyDown(hwnd, key);
    }

    public void keyUp(WinKey key) {
        Win32api.keyUp(hwnd, key);
    }

    public void keyDown(int key) {
        Win32api.keyDown(hwnd, key);
    }

    public void keyUp(int key) {
        Win32api.keyUp(hwnd, key);
    }
}
