package com.kayee.ssh.core;

/**
 * @author: zhaokai
 * @create: 2018-08-22 14:45:29
 */
public class ExecResult {

    private String systemOut;
    private String errorOut;
    private boolean success;

    public ExecResult() {
    }

    public ExecResult(String systemOut, String errorOut, boolean success) {

        this.systemOut = systemOut;
        this.errorOut = errorOut;
        this.success = success;
    }


    public String getSystemOut() {
        return systemOut;
    }

    public void setSystemOut(String systemOut) {
        this.systemOut = systemOut;
    }

    public String getErrorOut() {
        return errorOut;
    }

    public void setErrorOut(String errorOut) {
        this.errorOut = errorOut;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
