package com.kayee.ssh4j.handler;


import com.kayee.ssh4j.command.AbstractCommand;
import com.kayee.ssh4j.entity.Msg;

/**
 * @author: zhaokai
 * @create: 2018-08-08 10:33:25
 */
public interface SSHExecResultHandler {
    void handleMessage(AbstractCommand cmd, Msg execResult);
}
