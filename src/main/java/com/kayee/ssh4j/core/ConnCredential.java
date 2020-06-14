package com.kayee.ssh4j.core;

import com.jcraft.jsch.UserInfo;

public class ConnCredential implements UserInfo {
    String passwd;

    public ConnCredential(String passwd) {
        this.passwd = passwd;
    }

    public String getPasswd() {
        return this.passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getPassword() {
        return this.passwd;
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
