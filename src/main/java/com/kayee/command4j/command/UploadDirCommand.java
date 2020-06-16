package com.kayee.command4j.command;

import java.util.Objects;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:22:24
 */
public class UploadDirCommand extends AbstractCommand {
    private final String tag = "UploadDirCommand";
    private String remoteParentDir;
    private String localDir;

    public UploadDirCommand(String localDir, String remoteParentDir) {
        this.remoteParentDir = remoteParentDir;
        this.localDir = localDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadDirCommand that = (UploadDirCommand) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(remoteParentDir, that.remoteParentDir) &&
                Objects.equals(localDir, that.localDir);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tag, remoteParentDir, localDir);
    }

    public String getRemoteParentDir() {

        return remoteParentDir;
    }

    public void setRemoteParentDir(String remoteParentDir) {
        this.remoteParentDir = remoteParentDir;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    @Override
    public String buildRunCmd() {
        return null;
    }
}
