package com.kayee.ssh4j.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.kayee.ssh4j.exception.SSHConnectionException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author: zhaokai
 * @create: 2018-09-05 17:26:57
 */
@Slf4j
public class SSHSessionPool {

    private static final Integer DEFAULT_CONN_SIZE = 2;
    private static HashMap<PoolKey, List<SessionWrapper>> pool = new HashMap<>();
    private static JSch jsch = new JSch();

    static {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("task-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(1, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        pool.execute(new MonitorSessionRunnable());
    }

    /**
     * 为当前线程分发一个ssh session
     *
     * @param ip
     * @param username
     * @param password
     * @param port
     * @return
     * @throws SSHConnectionException
     */
    public static synchronized SessionWrapper dispatchSessionForCurrentThread(String ip, String username, String password, Integer port) throws SSHConnectionException {

        PoolKey key = new PoolKey(ip, username, port);
        List<SessionWrapper> wrapperList = pool.get(key);
        if (wrapperList == null) {
            Integer firstConnCount = initMultipleSessionForHost(ip, username, password, port, DEFAULT_CONN_SIZE);
            wrapperList = pool.get(key);
            if (firstConnCount == 0) {
                throw new SSHConnectionException("SSH Connection pool connect " + key + " failed");
            }
        }
        long currentThreadId = Thread.currentThread().getId();

        // 获取当前线程已经绑定的连接
        SessionWrapper currentThreadConnWrapper = null;
        for (SessionWrapper w : wrapperList) {
            if (w.getThreadId() != null && currentThreadId == w.getThreadId()) {
                currentThreadConnWrapper = w;
                break;
            }
        }

        // 当前线程没有建立连接，原因：1.新的线程，2.线程释放了该连接，3.当前线程的连接被关闭了

        // 1.判断是否有已经创建好的连接
        //         |
        //          |-> （满足条件：处于连接状态、不予其他线程绑定）->取出来，与当前线程绑定
        // 2.没有已经创建好的连接
        //         |
        //         |-> 新建一个连接，并与当前线程绑定，放入连接池中


        // 新的线程，或者线程释放了该连接
        if (currentThreadConnWrapper == null) {
            // 没有找到当前线程已经绑定的连接，绑定一个
            bindConn(ip, username, password, port);

        } else if (!currentThreadConnWrapper.getSession().isConnected()) {
            // 未处于连接状态
            bindConn(ip, username, password, port);
        } else {
            currentThreadConnWrapper.setBindTime(new Date());
            return currentThreadConnWrapper;
        }
        SessionWrapper newWrapper = null;
        for (SessionWrapper w : pool.get(key)) {
            if (w.getThreadId() != null && currentThreadId == w.getThreadId()) {
                newWrapper = w;
                break;
            }

        }
        if (newWrapper == null) {
            throw new SSHConnectionException(("SSH Connection pool connect " + key + " failed"));
        }

        return newWrapper;


    }

    /**
     * 绑定连接
     *
     * @param ip
     * @param username
     * @param password
     * @param port
     * @throws SSHConnectionException
     */
    private static void bindConn(String ip, String username, String password, Integer port) throws SSHConnectionException {
        PoolKey key = new PoolKey(ip, username, port);
        List<SessionWrapper> wrapperList = pool.get(key);
        SessionWrapper existedConn = null;
        for (SessionWrapper w : wrapperList) {
            if (w != null && w.getThreadId() == null && w.getSession() != null && w.getSession().isConnected()) {
                existedConn = w;
                break;
            }
        }
        if (existedConn == null) {
            // 新建一个连接
            createNewConnForCurrentThread(ip, username, password, port);
        } else {
            existedConn.setBindTime(new Date());
            existedConn.setThreadId(Thread.currentThread().getId());
        }

    }

    /**
     * 打开一个ssh session
     *
     * @param ip
     * @param username
     * @param password
     * @param port
     * @return
     * @throws SSHConnectionException
     */
    private static Session openNewSession(String ip, String username, String password, Integer port) throws SSHConnectionException {

        try {
            Session session = jsch.getSession(username, ip, port);
            UserInfo ui = new ConnCredential(password);
            session.setUserInfo(ui);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            log.info("trying to connect " + username + "@" + ip + ":" + port + " with ssh");
            Date start = new Date();
            session.connect(3600000);
            Date end = new Date();
            double takeMs = end.getTime() - start.getTime();
            log.info("connected " + username + "@" + ip + ":" + port + " with ssh takes time " + takeMs + " Milliseconds");
            return session;
        } catch (JSchException e) {
            throw new SSHConnectionException(e);
        }

    }

    /**
     * 创建一个新的ssh session，并与当前线程进行绑定
     *
     * @param ip
     * @param username
     * @param password
     * @param port
     * @throws SSHConnectionException
     */
    private static void createNewConnForCurrentThread(String ip, String username, String password, Integer port) throws SSHConnectionException {
        PoolKey key = new PoolKey(ip, username, port);
        List<SessionWrapper> wrapperList = pool.get(key);
        Session session = openNewSession(ip, username, password, port);
        SessionWrapper sessionWrapper = new SessionWrapper(Thread.currentThread().getId(), session, new Date());
        wrapperList.add(sessionWrapper);


    }

    /**
     * 为指定的host初始化多个ssh session
     *
     * @param ip
     * @param username
     * @param password
     * @param port
     * @param size
     * @return
     */
    private static Integer initMultipleSessionForHost(String ip, String username, String password, Integer port, Integer size) {
        Integer successfulConnCount = 0;

        PoolKey key = new PoolKey(ip, username, port);
        pool.put(key, new ArrayList<SessionWrapper>());
        List<SessionWrapper> wrapperList = pool.get(key);
        for (int i = 1; i <= size; i++) {
            try {
                Session session = openNewSession(ip, username, password, port);
                successfulConnCount++;
                SessionWrapper connWrapper = new SessionWrapper(null, session, new Date());
                wrapperList.add(connWrapper);

            } catch (SSHConnectionException e) {
                break;
            }


        }
        return successfulConnCount;
    }

    /**
     * 关闭存活时间超过maxLiveDuration的，且未与线程进行绑定的session
     *
     * @param maxLiveDuration
     */
    public synchronized static void closeNotUsedSession(long maxLiveDuration) {
        Set<Map.Entry<PoolKey, List<SessionWrapper>>> entries = pool.entrySet();
        for (Map.Entry<PoolKey, List<SessionWrapper>> e : entries) {
            List<SessionWrapper> sessionWrapperList = e.getValue();
            if (sessionWrapperList == null || sessionWrapperList.size() == 0) {
                continue;
            }
            HashSet<SessionWrapper> removeSet = new HashSet<>();

            for (int i = 0; i < sessionWrapperList.size(); i++) {
                SessionWrapper sw = sessionWrapperList.get(i);

                long swLiveDuration = new Date().getTime() - sw.getBindTime().getTime();
                // 未处于使用状态，且存活时间大于最大允许生存时间
                boolean needClose = sw.getThreadId() == null && swLiveDuration > maxLiveDuration;
                if (needClose) {
                    log.info(sw.getSession().getUserName() + "@" + sw.getSession().getHost() + ":" + sw.getSession().getPort() + "  session[" + sw.getSession() + "] disconnected ");
                    sw.getSession().disconnect();
                    removeSet.add(sw);
                }
            }
            for (SessionWrapper remove : removeSet) {
                sessionWrapperList.remove(remove);
            }
        }
    }


    public static class PoolKey {
        private String ip;
        private String username;
        private Integer port;

        public PoolKey(String ip, String username, Integer port) {
            this.ip = ip;
            this.username = username;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;

        }

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

}
