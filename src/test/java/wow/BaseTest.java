package wow;

import winapi.MemoryApi;
import winapi.Win32api;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.WowMemory;
import wow.memory.objects.Player;

import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinNT.HANDLE;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_OPERATION;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_READ;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_WRITE;

/**
 * @author Cargeh
 */
public abstract class BaseTest {
    protected static final String WOW_WINDOW_NAME = "World of Warcraft";
    protected static final Player player;
    protected static final ObjectManager objectManager;
    protected static final CtmManager ctmManager;
    protected static HWND hwnd;
    protected static int processId;
    protected static HANDLE handle;
    protected static WowMemory memory;
    protected static WowInstance wowInstance;

    static {
        hwnd = Win32api.getProcessHwnd(WOW_WINDOW_NAME);
        processId = Win32api.getProcessId(hwnd);
        handle = MemoryApi.openProcess(PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION, processId);
        memory = new WowMemory(processId);
        player = memory.getPlayer();
        ctmManager = memory.getCtmManager();
        objectManager = memory.getObjectManager();

        objectManager.scanForNewObjects();
        wowInstance = new WowInstance("World of Warcraft");
    }
}
