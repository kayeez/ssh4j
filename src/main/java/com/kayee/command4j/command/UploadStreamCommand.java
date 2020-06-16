package com.kayee.command4j.command;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:22:24
 */
public class UploadStreamCommand extends AbstractCommand {
    private final String tag = "UploadStreamCommand";
    private String remoteFileAbsolutePath;
    private InputStream stream;


    public UploadStreamCommand(String remoteFileAbsolutePath, InputStream stream) {
        this.remoteFileAbsolutePath = remoteFileAbsolutePath;
        this.stream = stream;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public String buildRunCmd() {
        return null;
    }

    public String getRemoteFileAbsolutePath() {
        return remoteFileAbsolutePath;
    }

    public void setRemoteFileAbsolutePath(String remoteFileAbsolutePath) {
        this.remoteFileAbsolutePath = remoteFileAbsolutePath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadStreamCommand that = (UploadStreamCommand) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(remoteFileAbsolutePath, that.remoteFileAbsolutePath) &&
                Objects.equals(stream, that.stream);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tag, remoteFileAbsolutePath, stream);
    }
}
