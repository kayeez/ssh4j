package com.kayee.ssh4j.exception;

/**
 * @author zhaokai
 * SSH 连接出错
 */
public class SSHException extends Exception {

    public SSHException() {

    }

    public SSHException(String msg) {
        super(msg);
    }

    public SSHException(Throwable t) {
        super(t);
    }

    public SSHException(String msg, Throwable t) {
        super(msg, t);
    }
}
