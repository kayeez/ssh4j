package com.kayee.ssh.handler;


import com.kayee.ssh.command.AbstractCommand;
import com.kayee.ssh.vo.MsgVO;

/**
 * @author: zhaokai
 * @create: 2018-08-08 10:33:25
 */
public interface SSHExecResultHandler {
    void handleMessage(AbstractCommand cmd, MsgVO execResult);
}
