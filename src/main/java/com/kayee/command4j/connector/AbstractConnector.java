package com.kayee.command4j.connector;

import com.kayee.command4j.command.*;
import com.kayee.command4j.entity.ExecuteResult;
import com.kayee.command4j.exception.CommandExecuteErrorException;
import com.kayee.command4j.handler.CommandExecutionRunningHandler;

public abstract class AbstractConnector {

    public synchronized ExecuteResult exec(AbstractCommand command, CommandExecutionRunningHandler handler) {
        try {
            open();
            return executeCommandByType(command, handler);
        } finally {
            close();
        }
    }

    private ExecuteResult executeCommandByType(AbstractCommand command, CommandExecutionRunningHandler runningHandler) {
        if (command instanceof ConsoleCommand) {
            ConsoleCommand executeCommand = (ConsoleCommand) command;
            return executeConsoleCommand(executeCommand, runningHandler);
        } else if (command instanceof DownloadFileCommand) {
            DownloadFileCommand executeCommand = (DownloadFileCommand) command;
            return downloadFile(executeCommand, runningHandler);
        } else if (command instanceof ShellCommand) {
            ShellCommand executeCommand = (ShellCommand) command;
            return executeShellCommand(executeCommand, runningHandler);
        } else if (command instanceof UploadContentCommand) {
            UploadContentCommand executeCommand = (UploadContentCommand) command;
            return uploadContent(executeCommand, runningHandler);
        } else if (command instanceof UploadDirCommand) {
            UploadDirCommand executeCommand = (UploadDirCommand) command;
            return uploadDir(executeCommand, runningHandler);
        } else if (command instanceof UploadFileCommand) {
            UploadFileCommand executeCommand = (UploadFileCommand) command;
            return uploadFile(executeCommand, runningHandler);
        } else if (command instanceof UploadStreamCommand) {
            UploadStreamCommand executeCommand = (UploadStreamCommand) command;
            return uploadStream(executeCommand, runningHandler);
        } else {
            throw new CommandExecuteErrorException("Unsupported command type " + command.getClass());
        }
    }

    protected abstract void open();
    protected abstract void close();
    protected abstract ExecuteResult executeConsoleCommand(ConsoleCommand command, CommandExecutionRunningHandler runningHandler);
    protected abstract ExecuteResult downloadFile(DownloadFileCommand command, CommandExecutionRunningHandler runningHandler);
    protected abstract ExecuteResult executeShellCommand(ShellCommand command, CommandExecutionRunningHandler runningHandler);
    protected abstract ExecuteResult uploadContent(UploadContentCommand command, CommandExecutionRunningHandler runningHandler);
    protected abstract ExecuteResult uploadDir(UploadDirCommand command, CommandExecutionRunningHandler runningHandler);
    protected abstract ExecuteResult uploadFile(UploadFileCommand command, CommandExecutionRunningHandler runningHandler);
    protected abstract ExecuteResult uploadStream(UploadStreamCommand command, CommandExecutionRunningHandler runningHandler);
}
