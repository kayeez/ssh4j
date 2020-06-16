package com.kayeez.command4j.command;

import java.util.Objects;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:22:24
 */
public class UploadContentCommand extends AbstractCommand {
    private final String tag = "UploadContentCommand";
    private String remoteFileAbsolutePath;
    private String content;


    public UploadContentCommand(String remoteFileAbsolutePath, String content) {
        this.remoteFileAbsolutePath = remoteFileAbsolutePath;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadContentCommand that = (UploadContentCommand) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(remoteFileAbsolutePath, that.remoteFileAbsolutePath) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tag, remoteFileAbsolutePath, content);
    }
}
