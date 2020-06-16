package com.kayee.command4j.core;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.kayee.command4j.entity.SessionWrapper;
import com.kayee.command4j.exception.CommandExecuteErrorException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author: zhaokai
 * @create: 2018-09-05 17:26:57
 */
@Slf4j
public class SSHSessionPool {
    private volatile static SSHSessionPool instance;

    private static final Integer DEFAULT_CONN_SIZE = 2;
    private static Map<PoolKey, List<SessionWrapper>> pool = new HashMap<>();
    private static JSch jsch = new JSch();

    static {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.execute(new MonitorSessionRunnable());
    }

    private SSHSessionPool() {
    }

    public static SSHSessionPool getInstance() {
        if (instance == null) {
            synchronized (SSHSessionPool.class) {
                if (instance == null) {
                    instance = new SSHSessionPool();
                }
            }
        }
        return instance;
    }

    public synchronized SessionWrapper dispatchSessionForCurrentThread(String ip, String username, String password, Integer port) {
        PoolKey key = PoolKey.builder()
                .ip(ip)
                .username(username)
                .port(port)
                .build();
        initializeKeySessionWrapperList(ip, username, password, port);
        SessionWrapper currentThreadBoundSession = findCurrentThreadBoundSession(pool.get(key));
        boolean currentThreadBoundSessionIsAvailable = currentThreadBoundSession != null
                && currentThreadBoundSession.getSession().isConnected();
        if (currentThreadBoundSessionIsAvailable) {
            currentThreadBoundSession.setBindTime(new Date());
            return currentThreadBoundSession;
        }
        bindWithAvailableOrNewSession(ip, username, password, port);
        return findCurrentThreadBoundSession(pool.get(key));
    }


    private SessionWrapper findCurrentThreadBoundSession(List<SessionWrapper> wrapperList) {
        long currentThreadId = Thread.currentThread().getId();
        return wrapperList.stream()
                .filter(sessionWrapper -> sessionWrapper.getThreadId() != null
                        && currentThreadId == sessionWrapper.getThreadId())
                .findFirst()
                .orElse(null);
    }

    private void initializeKeySessionWrapperList(String ip, String username, String password, Integer port) {
        PoolKey key = PoolKey.builder()
                .ip(ip)
                .username(username)
                .port(port)
                .build();
        if (pool.get(key) == null) {
            pool.put(key, new ArrayList<>());
            for (int i = 1; i <= SSHSessionPool.DEFAULT_CONN_SIZE; i++) {
                Session session = openSession(ip, username, password, port);
                SessionWrapper connWrapper = new SessionWrapper(null, session, new Date());
                pool.get(key).add(connWrapper);
            }
        }
    }

    private void bindWithAvailableOrNewSession(String ip, String username, String password, Integer port) {
        SessionWrapper availableSessionWrapper = findAvailableSessionWrapper(ip, username, port);
        if (availableSessionWrapper == null) {
            bindWithNewSession(ip, username, password, port);
        } else {
            availableSessionWrapper.setBindTime(new Date());
            availableSessionWrapper.setThreadId(Thread.currentThread().getId());
        }

    }

    private SessionWrapper findAvailableSessionWrapper(String ip, String username, Integer port) {
        PoolKey key = new PoolKey(ip, username, port);
        return pool.get(key).stream()
                .filter(sessionWrapper -> sessionWrapper != null
                        && sessionWrapper.getThreadId() == null
                        && sessionWrapper.getSession() != null
                        && sessionWrapper.getSession().isConnected())
                .findFirst()
                .orElse(null);
    }


    private Session openSession(String ip, String username, String password, Integer port) {

        try {
            Session session = jsch.getSession(username, ip, port);
            UserInfo ui = new UserInfoWithPassword(password);
            session.setUserInfo(ui);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            log.info("trying to connect " + username + "@" + ip + ":" + port + " with ssh");
            Date start = new Date();
            session.connect(ExecuteContext.sshSessionConnectTimeoutMillSeconds);
            Date end = new Date();
            double takeMs = end.getTime() - start.getTime();
            log.info("connected " + username + "@" + ip + ":" + port + " with ssh takes time " + takeMs + " milliseconds");
            return session;
        } catch (JSchException e) {
            throw new CommandExecuteErrorException(e);
        }

    }

    private void bindWithNewSession(String ip, String username, String password, Integer port) {
        PoolKey key = new PoolKey(ip, username, port);
        List<SessionWrapper> wrapperList = pool.get(key);
        Session session = openSession(ip, username, password, port);
        SessionWrapper sessionWrapper = new SessionWrapper(Thread.currentThread().getId(), session, new Date());
        wrapperList.add(sessionWrapper);
    }

    public synchronized void closeNotUsedSession(long maxLiveDuration) {
        pool.forEach(((poolKey, sessionWrappers) -> {
            if (sessionWrappers == null || sessionWrappers.isEmpty()) {
                return;
            }
            List<SessionWrapper> needRemoveSessionList = sessionWrappers.stream().filter(sessionWrapper -> {
                long sessionWrapperLiveDuration = new Date().getTime() - sessionWrapper.getBindTime().getTime();
                return sessionWrapper.getThreadId() == null && sessionWrapperLiveDuration > maxLiveDuration;
            }).collect(Collectors.toList());
            log.info(needRemoveSessionList.toString());
            needRemoveSessionList.forEach(sessionWrapper -> {
                Session session = sessionWrapper.getSession();
                log.info(session.getUserName() + "@" + session.getHost() + ":" + session.getPort() + "  session[" + session + "] disconnected ");
                session.disconnect();
                sessionWrappers.remove(sessionWrapper);
            });
        }));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PoolKey {
        private String ip;
        private String username;
        private Integer port;

        @Override
        public String toString() {
            return username + "@" + ip + ":" + port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PoolKey poolKey = (PoolKey) o;
            return Objects.equals(ip, poolKey.ip) &&
                    Objects.equals(username, poolKey.username) &&
                    Objects.equals(port, poolKey.port);
        }

        @Override
        public int hashCode() {

            return Objects.hash(ip, username, port);
        }
    }


    private static class UserInfoWithPassword implements UserInfo {

        String password;

        public UserInfoWithPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return this.password;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }
    }
}
