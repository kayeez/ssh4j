package com.kayeez.command4j.handler;


import com.kayeez.command4j.command.AbstractCommand;
import com.kayeez.command4j.entity.ExecuteResult;

/**
 * @author: zhaokai
 * @create: 2018-08-08 10:33:25
 */
public interface CommandExecutionFinishedHandler {
    void handleMessage(AbstractCommand cmd, ExecuteResult executeResult);
}
