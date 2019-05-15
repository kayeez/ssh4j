package com.kayee.ssh.core;

/**
 * @author: zhaokai
 * @create: 2018-09-20 14:53:09
 */
public class MonitorSessionRunnable implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000 * 60 * 60);
                long maxLiveDuration = 1000 * 60 * 60;
                SSHSessionPool.closeNotUsedSession(maxLiveDuration);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
