package winapi.components;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author Cargeh
 */
public interface User32Extended extends User32 {
    User32Extended INSTANCE = (User32Extended) Native.loadLibrary("user32", User32Extended.class, W32APIOptions.DEFAULT_OPTIONS);
    User32Extended SINSTANCE = (User32Extended) Native.synchronizedLibrary(INSTANCE);

    int WM_LBUTTONDOWN = 0x201;
    int WM_LBUTTONUP = 0x202;
    int WM_LBUTTONDBLCLK = 0x203;

    int WM_RBUTTONDOWN = 0x204;
    int WM_RBUTTONUP = 0x205;
    int WM_RBUTTONDBLCLK = 0x206;

    int WM_MBUTTONDOWN = 0x207;
    int WM_MBUTTONUP = 0x208;
    int WM_MBUTTONDBLCLK = 0x209;

    int WM_MOUSEWHEEL = 0x20A;
    int WM_MOUSEHWHEEL = 0x20E;

    WinDef.LRESULT SendMessage(
        WinDef.HWND hWnd,
        int Msg,
        WinDef.WPARAM wParam,
        WinDef.LPARAM lParam);

    WinDef.LRESULT SendMessage(
        WinDef.HWND hWnd,
        int Msg,
        int key,
        WinDef.LPARAM lParam);

    int GetLastError();
}
