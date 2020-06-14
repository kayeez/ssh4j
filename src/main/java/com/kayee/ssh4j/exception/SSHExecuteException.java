package com.kayee.ssh4j.exception;

/**
 * @author zhaokai
 * SSH执行出错异常
 */
public class SSHExecuteException extends Exception {

    public SSHExecuteException() {

    }

    public SSHExecuteException(String msg) {
        super(msg);
    }

    public SSHExecuteException(Throwable t) {
        super(t);
    }

    public SSHExecuteException(String msg, Throwable t) {
        super(msg, t);
    }
}
