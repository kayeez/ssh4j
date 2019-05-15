package com.kayee.ssh.core;


import com.jcraft.jsch.Session;

import java.util.Date;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:13:35
 */
public class SessionWrapper {

    private Long threadId;
    private Session session;
    private Date bindTime;

    public SessionWrapper(Long threadId, Session session, Date bindTime) {
        this.threadId = threadId;
        this.session = session;
        this.bindTime = bindTime;
    }

    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionWrapper that = (SessionWrapper) o;

        if (threadId != null ? !threadId.equals(that.threadId) : that.threadId != null) return false;
        if (session != null ? !session.equals(that.session) : that.session != null) return false;
        return bindTime != null ? bindTime.equals(that.bindTime) : that.bindTime == null;
    }

    @Override
    public int hashCode() {
        int result = threadId != null ? threadId.hashCode() : 0;
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (bindTime != null ? bindTime.hashCode() : 0);
        return result;
    }
}
