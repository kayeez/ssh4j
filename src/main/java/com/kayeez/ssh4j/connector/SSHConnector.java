package com.kayeez.ssh4j.connector;

import com.jcraft.jsch.*;
import com.kayeez.ssh4j.annotation.ConnectorLoginMapping;
import com.kayeez.ssh4j.command.*;
import com.kayeez.ssh4j.core.SSHSessionPool;
import com.kayeez.ssh4j.entity.ExecuteResult;
import com.kayeez.ssh4j.entity.Path;
import com.kayeez.ssh4j.entity.SSHLoginInformation;
import com.kayeez.ssh4j.entity.SessionWrapper;
import com.kayeez.ssh4j.exception.CommandExecuteErrorException;
import com.kayeez.ssh4j.handler.CommandExecutionRunningHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * @author: zhaokai
 * @create: 2018-08-16 10:18:12
 */
@Slf4j
@ConnectorLoginMapping(SSHLoginInformation.class)
public class SSHConnector extends AbstractConnector {

    private Session session;
    private Channel channel;
    private ChannelSftp sftpChannel;
    private String ip;
    private String username;
    private String password;
    private Integer port = 22;
    private SessionWrapper sessionWrapper;
    private SSHSessionPool sessionPool = SSHSessionPool.getInstance();

    public SSHConnector(SSHLoginInformation SSHLoginInformation) {
        this.ip = SSHLoginInformation.getIp();
        this.username = SSHLoginInformation.getUsername();
        this.password = SSHLoginInformation.getPassword();
        if (SSHLoginInformation.getPort() != null) {
            this.port = SSHLoginInformation.getPort();
        }

    }

    private void checkParam() {
        if (ip == null) {
            throw new CommandExecuteErrorException("ssh connection info host ip  can't be empty!");
        }
        if (username == null) {
            throw new CommandExecuteErrorException("ssh connection info username  can't be empty!");
        }
        if (password == null) {
            throw new CommandExecuteErrorException("ssh connection info password  can't be empty!");
        }

    }

    protected void open() {
        checkParam();
        sessionWrapper = sessionPool.dispatchSessionForCurrentThread(ip, username, password, port);
        session = sessionWrapper.getSession();
        checkOpened();
    }

    private void checkOpened() {
        if (session == null || !session.isConnected()) {
            throw new CommandExecuteErrorException("Haven't open ssh session yet");
        }

    }


    protected void close() {
        try {
            if (sessionWrapper != null) {
                sessionWrapper.setThreadId(null);
                session = null;
            }
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
                channel = null;
            }


            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
                sftpChannel = null;
            }
            log.info("SSH connection released!");
        } catch (Exception e) {
            log.error("SSH connection release  failed with the following error: " + e);
        }
    }

    protected ExecuteResult uploadStream(UploadStreamCommand command, CommandExecutionRunningHandler handler) {
        openSftpChannel();
        String remoteFileAbsolutePath = command.getRemoteFileAbsolutePath();
        Path filePath = new Path(remoteFileAbsolutePath);
        String parentAbsolutePath = makeRemoteDirs(filePath.getParentPath());
        String remoteFullPath = new Path(parentAbsolutePath, filePath.getName()).getFullPath();
        try {
            sftpChannel.put(command.getStream(), remoteFullPath);
        } catch (SftpException e) {
            throw new CommandExecuteErrorException(e);
        }
        return ExecuteResult.builder()
                .standardOutputMessage("upload success , remote path is " + remoteFullPath)
                .success(true).build();
    }


    protected ExecuteResult uploadFile(UploadFileCommand command, CommandExecutionRunningHandler handler) {
        openSftpChannel();
        String remoteFileAbsolutePath = command.getRemoteFileAbsolutePath();
        Path filePath = new Path(remoteFileAbsolutePath);
        String parentAbsolutePath = makeRemoteDirs(filePath.getParentPath());
        String remoteFullPath = new Path(parentAbsolutePath, filePath.getName()).getFullPath();
        try {
            sftpChannel.put(command.getLocalFileAbsolutePath(), remoteFullPath);
        } catch (SftpException e) {
            throw new CommandExecuteErrorException(e);
        }
        return ExecuteResult.builder()
                .standardOutputMessage("upload local file " + command.getLocalFileAbsolutePath() + " success , remote path is " + remoteFullPath)
                .success(true).build();
    }

    protected ExecuteResult uploadDir(UploadDirCommand command, CommandExecutionRunningHandler handler) {
        openSftpChannel();
        String localDir = command.getLocalDir();
        String remoteParentDir = command.getRemoteParentDir();
        File localDirFile = new File(localDir);
        if (!localDirFile.exists()) {
            throw new CommandExecuteErrorException("local directory doesn't exist!");
        }
        if (!localDirFile.isDirectory()) {
            throw new CommandExecuteErrorException(localDir + " is a file!");
        }
        Path remoteBaseDirPath = new Path(remoteParentDir, localDirFile.getName());
        makeRemoteDirs(remoteBaseDirPath.getFullPath());
        Collection<File> files = FileUtils.listFiles(localDirFile, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
        for (File f : files) {
            String localDirPath = localDirFile.getAbsolutePath();
            String currentFileParentPath = f.getParentFile().getAbsolutePath();
            String subParent = currentFileParentPath.substring(localDirPath.length());
            try {
                Path rp = new Path(remoteBaseDirPath, subParent);
                String rpAbsolute = makeRemoteDirs(rp.getFullPath());
                String remoteFullPath = new Path(rpAbsolute, f.getName()).getFullPath();
                sftpChannel.put(f.getAbsolutePath(), remoteFullPath);
            } catch (SftpException e1) {
                throw new CommandExecuteErrorException(e1);
            }
        }
        return ExecuteResult.builder()
                .standardOutputMessage("upload local dir " + localDir + " success , remote path is " + remoteParentDir)
                .success(true).build();
    }

    protected ExecuteResult uploadContent(UploadContentCommand command, CommandExecutionRunningHandler runningHandler) {
        try {
            UploadStreamCommand uploadStreamCommand = new UploadStreamCommand(command.getRemoteFileAbsolutePath(), IOUtils.toInputStream(command.getContent(), StandardCharsets.UTF_8.name()));
            return uploadStream(uploadStreamCommand, runningHandler);
        } catch (IOException e) {
            throw new CommandExecuteErrorException(e);
        }

    }

    protected ExecuteResult executeShellCommand(ShellCommand command, CommandExecutionRunningHandler runningHandler) {
        Path fullShellPath = new Path(command.getShellServerWorkDir(), command.getShellName());
        if (command.getShellContent() != null && !"".equals(command.getShellContent())) {
            try {
                InputStream is = IOUtils.toInputStream(command.getShellContent(), StandardCharsets.UTF_8.name());
                UploadStreamCommand uploadStreamCommand = new UploadStreamCommand(fullShellPath.getFullPath(), is);
                uploadStream(uploadStreamCommand, runningHandler);
            } catch (IOException e) {
                throw new CommandExecuteErrorException(e);
            }

        }
        ConsoleCommand executeCommand = new ConsoleCommand()
                .appendCommand("chmod +x " + fullShellPath.getFullPath()).appendCommand(command.buildRunCmd());
        executeCommand.setWithSudo(command.isWithSudo());
        return executeConsoleCommand(executeCommand, runningHandler);
    }

    protected ExecuteResult downloadFile(DownloadFileCommand command, CommandExecutionRunningHandler handler) {
        openSftpChannel();
        String localFileAbsolutePath = command.getLocalFileAbsolutePath();
        File parentFile = new File(localFileAbsolutePath).getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new CommandExecuteErrorException("create local dir " + parentFile + " failed");
        }
        try {
            sftpChannel.get(command.getRemoteFileAbsolutePath(), localFileAbsolutePath);
        } catch (SftpException e) {
            throw new CommandExecuteErrorException(e);
        }
        return ExecuteResult.builder()
                .standardOutputMessage("download remote file " + command.getRemoteFileAbsolutePath() + " success , local path is " + localFileAbsolutePath)
                .success(true).build();
    }

    private void openExecuteChannel() {
        try {
            channel = session.openChannel("exec");
        } catch (JSchException e) {
            throw new CommandExecuteErrorException(e);
        }
    }

    private boolean executeSuccess(int exitStatus) {
        return exitStatus == 0;
    }

    protected ExecuteResult executeConsoleCommand(ConsoleCommand command, CommandExecutionRunningHandler handler) {
        openExecuteChannel();
        configureChannelCommand(command);
        ExecuteResult executeResult;
        try (ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {
            configureChannelStream(errorStream);
            connectChannel();
            executeResult = waitUntilFinish(command, handler);
            executeResult.setErrorMessage(new String(errorStream.toByteArray(), StandardCharsets.UTF_8));
            return executeResult;
        } catch (IOException e) {
            throw new CommandExecuteErrorException(e);
        }
    }

    private void connectChannel() {
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new CommandExecuteErrorException(e);
        }
    }

    private ExecuteResult waitUntilFinish(ConsoleCommand command, CommandExecutionRunningHandler handler) {
        try (BufferedReader info = new BufferedReader(new InputStreamReader(channel.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder standardOutputMessage = new StringBuilder();
            while (true) {
                String line;
                while ((line = info.readLine()) != null) {
                    standardOutputMessage.append(line).append("\n");
                    if (handler != null) {
                        handler.handle(command, line);
                    }
                }
                if (channel.isClosed()) {
                    return ExecuteResult.builder()
                            .success(executeSuccess(channel.getExitStatus()))
                            .standardOutputMessage(standardOutputMessage.toString())
                            .build();

                }
            }
        } catch (IOException e) {
            throw new CommandExecuteErrorException(e);
        }

    }

    private void configureChannelStream(OutputStream errorStream) {
        channel.setInputStream(null);
        channel.setOutputStream(System.out);
        ((ChannelExec) channel).setErrStream(errorStream);
    }

    private void configureChannelCommand(ConsoleCommand command) {
        String displayCommand = command.buildRunCmd();
        String executeCommand = displayCommand;
        if (command.isWithSudo()) {
            executeCommand = command.buildRunCmdWithSudo(this.password);
        }
        ((ChannelExec) channel).setCommand(executeCommand);
        log.info("Session[" + session + "] Start to run command [ " + displayCommand + " ] at " + ip);
    }


    private void openSftpChannel() {
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
        } catch (JSchException e) {
            throw new CommandExecuteErrorException(e);
        }
    }

    private String makeRemoteDirs(String remoteParentDir) {
        ConsoleCommand mkdir = new ConsoleCommand()
                .appendCommand("mkdir -p " + remoteParentDir)
                .appendCommand("cd " + remoteParentDir).appendCommand("pwd");
        ExecuteResult result = executeConsoleCommand(mkdir, null);
        if (result.isSuccess()) {
            return result.getStandardOutputMessage().trim();
        }
        throw new CommandExecuteErrorException("make dir " + remoteParentDir + " error " + result.getErrorMessage());

    }

}
