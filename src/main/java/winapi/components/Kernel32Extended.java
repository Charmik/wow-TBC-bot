package winapi.components;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;

public interface Kernel32Extended extends Kernel32 {
    Kernel32Extended INSTANCE = (Kernel32Extended) Native.loadLibrary("kernel32", Kernel32Extended.class);
    Kernel32Extended SINSTANCE = (Kernel32Extended) Native.synchronizedLibrary(INSTANCE);

    boolean WriteProcessMemory(
        HANDLE hProcess,
        Pointer address,
        float[] data,
        int size,
        IntByReference lpNumberOfBytesWritten);

    boolean WriteProcessMemory(
        HANDLE hProcess,
        Pointer address,
        int[] data,
        int size,
        IntByReference lpNumberOfBytesWritten);

    boolean WriteProcessMemory(
        HANDLE hProcess,
        Pointer address,
        long[] data,
        int size,
        IntByReference lpNumberOfBytesWritten);
}
