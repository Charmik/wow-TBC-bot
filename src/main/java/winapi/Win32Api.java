package winapi;

import java.awt.*;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WINDOWINFO;
import com.sun.jna.ptr.IntByReference;
import winapi.components.User32Extended;
import winapi.components.WinKey;

import static com.sun.jna.platform.win32.WinUser.WM_KEYDOWN;
import static com.sun.jna.platform.win32.WinUser.WM_KEYUP;

/**
 * @author Cargeh
 */
public final class Win32api {
    private static final User32Extended user32 = User32Extended.SINSTANCE;

    private Win32api() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot instantiate Win32Api");
    }

    public static HWND getProcessHwnd(String windowName) {
        HWND hwnd = user32.FindWindow(null, windowName);
        if (hwnd == null)
            throw new RuntimeException("Cannot find window with name " + windowName);
        return hwnd;
    }

    public static int getProcessId(HWND hwnd) {
        IntByReference pid = new IntByReference(0);
        user32.GetWindowThreadProcessId(hwnd, pid);
        return pid.getValue();
    }

    public static void keyDown(
        HWND hwnd,
        WinKey key)
    {
        user32.SendMessage(hwnd, WM_KEYDOWN, key.getWParam(), null);
    }

    public static void keyUp(
        HWND hwnd,
        WinKey key)
    {
        user32.SendMessage(hwnd, WM_KEYUP, key.getWParam(), null);
    }

    public static void keyDown(
        HWND hwnd,
        int key)
    {
        user32.SendMessage(hwnd, WM_KEYDOWN, key, null);

    }

    public static void keyUp(
        HWND hwnd,
        int key)
    {
        user32.SendMessage(hwnd, WM_KEYUP, key, null);
    }

    public static Point getWindowResolution(HWND hwnd) {
        WINDOWINFO windowinfo = new WINDOWINFO();

        boolean success = user32.GetWindowInfo(hwnd, windowinfo);
        if (!success)
            throw new Win32Exception(user32.GetLastError());

        RECT rcClient = windowinfo.rcClient;
        int width = rcClient.right - rcClient.left;
        int height = rcClient.bottom - rcClient.top;

        return new Point(width, height);
    }
}
