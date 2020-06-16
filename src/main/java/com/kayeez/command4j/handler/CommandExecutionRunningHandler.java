package com.kayeez.command4j.handler;


import com.kayeez.command4j.command.AbstractCommand;

/**
 * @author: zhaokai
 * @create: 2018-08-14 11:15:30
 */
public interface CommandExecutionRunningHandler {

    void handle(AbstractCommand cmd, String outOneLineLog);
}
