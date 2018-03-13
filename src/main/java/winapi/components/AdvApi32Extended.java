package winapi.components;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.win32.W32APIOptions;

public interface AdvApi32Extended extends Advapi32 {
    AdvApi32Extended INSTANCE = Native.loadLibrary("Advapi32", AdvApi32Extended.class, W32APIOptions.DEFAULT_OPTIONS);
    AdvApi32Extended SINSTANCE = (AdvApi32Extended) Native.synchronizedLibrary(INSTANCE);

    String SE_DEBUG_NAME = "SeDebugPrivilege";

    int SE_PRIVILEGE_ENABLED = 2;
    int TOKEN_ASSIGN_PRIMARY = 0x0001;
    int TOKEN_DUPLICATE = 0x0002;
    int TOKEN_IMPERSONATE = 0x0004;
    int TOKEN_QUERY = 0x0008;
    int TOKEN_QUERY_SOURCE = 0x0010;
    int TOKEN_ADJUST_PRIVILEGES = 0x0020;
    int TOKEN_ADJUST_GROUPS = 0x0040;
    int TOKEN_ADJUST_DEFAULT = 0x0080;
    int TOKEN_ADJUST_SESSIONID = 0x0100;

    int TOKEN_ALL_ACCESS = Kernel32.
        STANDARD_RIGHTS_REQUIRED |
        TOKEN_ASSIGN_PRIMARY |
        TOKEN_DUPLICATE |
        TOKEN_IMPERSONATE |
        TOKEN_QUERY |
        TOKEN_QUERY_SOURCE |
        TOKEN_ADJUST_PRIVILEGES |
        TOKEN_ADJUST_GROUPS |
        TOKEN_ADJUST_DEFAULT;

    int TOKEN_READ = Kernel32.
        STANDARD_RIGHTS_READ |
        TOKEN_QUERY;

    int TOKEN_WRITE = Kernel32.
        STANDARD_RIGHTS_WRITE |
        TOKEN_ADJUST_PRIVILEGES |
        TOKEN_ADJUST_GROUPS |
        TOKEN_ADJUST_DEFAULT;

    int TOKEN_EXECUTE = Kernel32.STANDARD_RIGHTS_EXECUTE;
}
