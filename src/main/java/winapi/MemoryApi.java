package winapi;

import java.util.Date;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import winapi.components.AdvApi32Extended;
import winapi.components.Kernel32Extended;

import static com.sun.jna.platform.win32.WinError.ERROR_NOT_ALL_ASSIGNED;

/**
 * @author Cargeh
 */
public final class MemoryApi {
    private static final Kernel32Extended kernel32 = Kernel32Extended.SINSTANCE;
    private static final AdvApi32Extended advApi32 = AdvApi32Extended.SINSTANCE;
    private static Logger log = LoggerFactory.getLogger(MemoryApi.class);

    static {
        enableProcessDebug();
    }

    private static void enableProcessDebug() {
        WinNT.HANDLEByReference hToken = new WinNT.HANDLEByReference();
        HANDLE handle = kernel32.GetCurrentProcess();

        boolean openProcessTokenSuccess = advApi32.OpenProcessToken(handle, AdvApi32Extended.TOKEN_ALL_ACCESS, hToken);
        if (openProcessTokenSuccess) {
            setPrivilege(hToken.getValue(), AdvApi32Extended.SE_DEBUG_NAME, true);
            kernel32.CloseHandle(handle);
        } else {
            throw new Win32Exception(kernel32.GetLastError());
        }
    }

    private static void setPrivilege(
        HANDLE hToken,
        String lpszPrivilege,
        boolean bEnablePrivilege)
    {
        WinNT.LUID luid = new WinNT.LUID();
        boolean lookupPrivilegeSuccessful = advApi32.LookupPrivilegeValue(null, lpszPrivilege, luid);
        if (!lookupPrivilegeSuccessful)
            throw new Win32Exception(kernel32.GetLastError());

        WinNT.TOKEN_PRIVILEGES tp = new WinNT.TOKEN_PRIVILEGES(1);
        tp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES();
        tp.Privileges[0].Luid = luid;
        tp.Privileges[0].Attributes = bEnablePrivilege ? new WinDef.DWORD(AdvApi32Extended.SE_PRIVILEGE_ENABLED) : new WinDef.DWORD(0);

        boolean adjustTokenPrivilegesSuccessful = advApi32.AdjustTokenPrivileges(hToken, false, tp, tp.size(), null, null);
        if (!adjustTokenPrivilegesSuccessful)
            throw new Win32Exception(kernel32.GetLastError());

        if (kernel32.GetLastError() == ERROR_NOT_ALL_ASSIGNED)
            throw new Win32Exception(kernel32.GetLastError());
    }

    public static HANDLE openProcess(
        int permissions,
        int pid)
    {
        HANDLE handle = kernel32.OpenProcess(permissions, true, pid);
        if (handle == null)
            throw new Win32Exception(kernel32.GetLastError());
        return handle;
    }

    public static Memory readMemory(
        HANDLE process,
        Pointer address,
        int bytesToRead)
    {
        Memory output = new Memory(bytesToRead);
        boolean successful;
        successful = kernel32.ReadProcessMemory(process, address, output, bytesToRead, null);
        if (!successful) {
            //log.info(String.format("%s: RPM fail on args %s, %s, %s",
            //    new Date().toString(), process, address, bytesToRead));
        }
        return output;
    }

    public static void writeFloat(
        HANDLE process,
        Pointer address,
        float data,
        int bytesToWrite)
    {
        boolean successful;
        successful = kernel32.WriteProcessMemory(process, address, new float[]{data}, bytesToWrite, null);
        if (!successful) {
            log.info(String.format("%s: WPM fail on args %s, %s, %s, %s",
                new Date().toString(), process, address, data, bytesToWrite));
        }
    }

    public static void writeInt(
        HANDLE process,
        Pointer address,
        int data,
        int bytesToWrite)
    {
        boolean successful;
        successful = kernel32.WriteProcessMemory(process, address, new int[]{data}, bytesToWrite, null);
        if (!successful) {
            log.info(String.format("%s: WPM fail on args %s, %s, %s, %s",
                new Date().toString(), process, address, data, bytesToWrite));
        }
    }

    public static void writeLong(
        HANDLE process,
        Pointer address,
        long data,
        int bytesToWrite)
    {
        boolean successful;
        successful = kernel32.WriteProcessMemory(process, address, new long[]{data}, bytesToWrite, null);
        if (!successful) {
            log.info(String.format("%s: WPM fail on args %s, %s, %s, %s",
                new Date().toString(), process, address, data, bytesToWrite));
        }
    }

    public static int getTickCount() {
        return kernel32.GetTickCount();
    }
}
