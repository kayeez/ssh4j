package com.kayee.ssh4j.entity;

import java.io.Serializable;

/**
 * Linux返回信息实体类
 * Created by zhaokai on 2018/5/11.
 */
public class Msg implements Serializable {

    private static final long serialVersionUID = -368802240567746261L;
    //命令执行成功标识
    private boolean success;

    //执行命令时控制台输出的信息
    private String sysoutMsg;


    private String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSysoutMsg() {
        return sysoutMsg;
    }

    public void setSysoutMsg(String sysoutMsg) {
        this.sysoutMsg = sysoutMsg;
    }
}
