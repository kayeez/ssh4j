package com.kayeez.ssh4j.exception;

/**
 * @author zhaokai
 */
public class CommandExecuteErrorException extends RuntimeException {

    public CommandExecuteErrorException(String msg) {
        super(msg);
    }

    public CommandExecuteErrorException(Throwable t) {
        super(t);
    }
}
