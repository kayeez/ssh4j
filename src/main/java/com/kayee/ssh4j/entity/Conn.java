package com.kayee.ssh4j.entity;


/**
 * @author zhaokai
 */
public class Conn {
    private String ip;
    private Integer port = 22;
    private String user;
    private String pwd;


    public Conn() {

    }

    public Conn(String ip, Integer port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;

    }


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(final String pwd) {
        this.pwd = pwd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conn conn = (Conn) o;

        if (ip != null ? !ip.equals(conn.ip) : conn.ip != null) return false;
        if (port != null ? !port.equals(conn.port) : conn.port != null) return false;
        if (user != null ? !user.equals(conn.user) : conn.user != null) return false;
        return pwd != null ? pwd.equals(conn.pwd) : conn.pwd == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (pwd != null ? pwd.hashCode() : 0);
        return result;
    }

}
