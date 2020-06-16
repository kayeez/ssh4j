package com.kayeez.command4j.core;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhaokai
 * @create: 2018-09-20 14:53:09
 */
@Slf4j
public class MonitorSessionRunnable implements Runnable {
    private SSHSessionPool sessionPool = SSHSessionPool.getInstance();
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(ExecuteContext.sshSessionLiveDurationMillSeconds);
                sessionPool.closeNotUsedSession(ExecuteContext.sshSessionLiveDurationMillSeconds);
            } catch (Exception e) {
                log.error("session monitoring error {}",e.getMessage(),e);
            }
        }
    }
}
