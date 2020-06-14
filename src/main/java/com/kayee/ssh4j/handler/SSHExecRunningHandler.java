package com.kayee.ssh4j.handler;


import com.kayee.ssh4j.command.AbstractCommand;

/**
 * @author: zhaokai
 * @create: 2018-08-14 11:15:30
 */
public interface SSHExecRunningHandler {

    void handle(AbstractCommand cmd, String outOneLineLog);
}
