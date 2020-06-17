package com.kayeez.command4j.connector;

import com.kayeez.command4j.annotation.ConnectorLoginMapping;
import com.kayeez.command4j.command.*;
import com.kayeez.command4j.entity.ExecuteResult;
import com.kayeez.command4j.entity.RuntimeLoginInformation;
import com.kayeez.command4j.exception.CommandExecuteErrorException;
import com.kayeez.command4j.handler.CommandExecutionRunningHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

@ConnectorLoginMapping(RuntimeLoginInformation.class)
public class RuntimeConnector extends AbstractConnector {

    private RuntimeLoginInformation runtimeLoginInformation;

    public RuntimeConnector(RuntimeLoginInformation runtimeLoginInformation) {
        this.runtimeLoginInformation = runtimeLoginInformation;
    }


    @Override
    protected void open() {

    }

    @Override
    protected void close() {

    }

    @Override
    protected ExecuteResult executeConsoleCommand(ConsoleCommand command, CommandExecutionRunningHandler runningHandler) {
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows()) {
            builder.command("cmd.exe", "/c", command.buildRunCmd());
        } else {
            String executeCommandContent;
            if (command.isWithSudo()) {
                String sudoPassword = runtimeLoginInformation.getSudoPassword();
                if (sudoPassword == null || "".equals(sudoPassword)) {
                    throw new CommandExecuteErrorException("sudo password is required");
                }
                executeCommandContent = command.buildRunCmdWithSudo(runtimeLoginInformation.getSudoPassword());
            } else {
                executeCommandContent = command.buildRunCmd();
            }
            builder.command("sh", "-c", executeCommandContent);
        }
        try {
            Process process = builder.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(command, process.getInputStream(), runningHandler);

            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            return ExecuteResult.builder().success(exitCode == 0).standardOutputMessage(streamGobbler.message.toString()).build();
        } catch (IOException | InterruptedException e) {
            throw new CommandExecuteErrorException(e);
        }
    }


    @Override
    protected ExecuteResult executeShellCommand(ShellCommand command, CommandExecutionRunningHandler runningHandler) {
        return null;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private CommandExecutionRunningHandler handler;
        private AbstractCommand command;
        private StringBuilder message;

        public StreamGobbler(AbstractCommand command, InputStream inputStream, CommandExecutionRunningHandler handler) {
            this.inputStream = inputStream;
            this.handler = handler;
            this.command = command;
            message = new StringBuilder();
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(line -> {
                        message.append(line).append("\n");
                        if (handler != null) {
                            handler.handle(command, line);
                        }
                    });
        }

    }
}
