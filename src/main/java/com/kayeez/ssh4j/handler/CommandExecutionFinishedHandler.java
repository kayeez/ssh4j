package com.kayeez.ssh4j.handler;


import com.kayeez.ssh4j.command.AbstractCommand;
import com.kayeez.ssh4j.entity.ExecuteResult;

/**
 * @author: zhaokai
 * @create: 2018-08-08 10:33:25
 */
public interface CommandExecutionFinishedHandler {
    void handleMessage(AbstractCommand cmd, ExecuteResult executeResult);
}
