package com.kayeez.command4j.exception;

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
