package com.kayee.ssh.core;

import java.io.File;

public class SysConfigOption {
    public static long TIMEOUT = 100000L;
    public static boolean HALT_ON_FAILURE = false;
    public static long INTEVAL_TIME_BETWEEN_TASKS = 5000L;
    public static int SSH_PORT_NUMBER = 22;
    public static String ERROR_MSG_BUFFER_TEMP_FILE_PATH;

    static {
        ERROR_MSG_BUFFER_TEMP_FILE_PATH = System.getProperty("user.home") + File.separator + "sshxcute_err.msg";
    }

    public SysConfigOption() {
    }
}
