package wow.memory;

import com.sun.jna.platform.win32.WinNT.HANDLE;
import winapi.MemoryApi;
import wow.memory.objects.AuctionManager;
import wow.memory.objects.Player;

import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_OPERATION;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_READ;
import static com.sun.jna.platform.win32.WinNT.PROCESS_VM_WRITE;

/**
 * @author Cargeh
 */
public final class WowMemory {

    private final HANDLE processMemoryHandle;
    private Player player;
    private ObjectManager objectManager;
    private CtmManager ctmManager;

    public WowMemory(int processId) {
        this.processMemoryHandle = MemoryApi.openProcess(PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION, processId);
        this.player = new Player(this);
        this.objectManager = new ObjectManager(this);
        this.ctmManager = new CtmManager(this);
    }

    public Player getPlayer() {
        return new Player(this);
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

    public AuctionManager getAuctionManager() {
        return new AuctionManager(this);
    }

    public void updateFields() {
        this.player.updatePlayer();
        this.objectManager = new ObjectManager(this);
        this.ctmManager = new CtmManager(this);
    }
}
