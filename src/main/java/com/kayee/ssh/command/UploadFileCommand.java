package com.kayee.ssh.command;

import java.util.Objects;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:22:24
 */
public class UploadFileCommand extends AbstractCommand {
    private final String tag = "UploadFileCommand";
    private String remoteFileAbsolutePath;
    private String localFileAbsolutePath;


    public UploadFileCommand(String localFileAbsolutePath, String remoteFileAbsolutePath) {
        this.remoteFileAbsolutePath = remoteFileAbsolutePath;
        this.localFileAbsolutePath = localFileAbsolutePath;
    }

    public String getRemoteFileAbsolutePath() {
        return remoteFileAbsolutePath;
    }

    public void setRemoteFileAbsolutePath(String remoteFileAbsolutePath) {
        this.remoteFileAbsolutePath = remoteFileAbsolutePath;
    }

    public String getLocalFileAbsolutePath() {
        return localFileAbsolutePath;
    }

    public void setLocalFileAbsolutePath(String localFileAbsolutePath) {
        this.localFileAbsolutePath = localFileAbsolutePath;
    }

    @Override
    public String buildRunCmd() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadFileCommand that = (UploadFileCommand) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(remoteFileAbsolutePath, that.remoteFileAbsolutePath) &&
                Objects.equals(localFileAbsolutePath, that.localFileAbsolutePath);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tag, remoteFileAbsolutePath, localFileAbsolutePath);
    }
}
