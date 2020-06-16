package com.kayee.command4j.handler;


import com.kayee.command4j.command.AbstractCommand;
import com.kayee.command4j.entity.ExecuteResult;

/**
 * @author: zhaokai
 * @create: 2018-08-08 10:33:25
 */
public interface CommandExecutionFinishedHandler {
    void handleMessage(AbstractCommand cmd, ExecuteResult executeResult);
}
