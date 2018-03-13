package wow.memory;

import com.sun.jna.platform.win32.WinNT.HANDLE;
import winapi.MemoryApi;
import wow.memory.objects.Player;

import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_OPERATION;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_READ;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_WRITE;

/**
 * @author Cargeh
 */
public final class WowMemory {
    private final HANDLE processMemoryHandle;
    private final Player player;
    private final ObjectManager objectManager;
    private final CtmManager ctmManager;

    public WowMemory(int processId) {
        this.processMemoryHandle = MemoryApi.openProcess(PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION, processId);

        this.player = new Player(this);
        this.objectManager = new ObjectManager(this);
        this.ctmManager = new CtmManager(this);
    }

    public Player getPlayer() {
        return player;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public CtmManager getCtmManager() {
        return ctmManager;
    }

    public HANDLE getProcessMemoryHandle() {
        return processMemoryHandle;
    }
}
