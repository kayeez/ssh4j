package com.kayee.ssh.exception;

/**
 * @author zhaokai
 * SSH 文件传输出错
 */
public class SSHFileTransferException extends Exception {

    public SSHFileTransferException() {

    }

    public SSHFileTransferException(String msg) {
        super(msg);
    }

    public SSHFileTransferException(Throwable t) {
        super(t);
    }

    public SSHFileTransferException(String msg, Throwable t) {
        super(msg, t);
    }
}
