package com.kayee.ssh4j.exception;

/**
 * @author zhaokai
 * SSH 连接出错
 */
public class SSHConnectionException extends Exception {

    public SSHConnectionException() {

    }

    public SSHConnectionException(String msg) {
        super(msg);
    }

    public SSHConnectionException(Throwable t) {
        super(t);
    }

    public SSHConnectionException(String msg, Throwable t) {
        super(msg, t);
    }
}
