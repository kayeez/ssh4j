package com.kayee.ssh.handler;


import com.kayee.ssh.command.AbstractCommand;

/**
 * @author: zhaokai
 * @create: 2018-08-14 11:15:30
 */
public interface SSHExecRunningHandler {

    void handle(AbstractCommand cmd, String outOneLineLog);
}
