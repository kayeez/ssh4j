package com.kayee.ssh4j.core;

import com.jcraft.jsch.*;
import com.kayee.ssh4j.command.*;
import com.kayee.ssh4j.entity.SSHLoginInformation;
import com.kayee.ssh4j.exception.SSHException;
import com.kayee.ssh4j.handler.SSHExecRunningHandler;
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
public class SSHConnection {

    private Session session;
    private Long taskExecuteTimeInterval = 10L;
    private Channel channel;
    private ChannelSftp sftpChannel;
    private String ip;
    private String username;
    private String password;
    private Integer port = 22;
    private SessionWrapper sessionWrapper;

    public SSHConnection(SSHLoginInformation SSHLoginInformation) {
        this.ip = SSHLoginInformation.getIp();
        this.username = SSHLoginInformation.getUser();
        this.password = SSHLoginInformation.getPwd();
        if (SSHLoginInformation.getPort() != null) {
            this.port = SSHLoginInformation.getPort();
        }

    }

    private void checkParam() throws SSHException {
        if (ip == null) {
            throw new SSHException("ssh connection info host ip  can't be empty!");
        }
        if (username == null) {
            throw new SSHException("ssh connection info username  can't be empty!");
        }
        if (password == null) {
            throw new SSHException("ssh connection info password  can't be empty!");
        }

    }

    public void openConnection() throws SSHException {
        checkParam();
        sessionWrapper = SSHSessionPool.dispatchSessionForCurrentThread(ip, username, password, port);
        session = sessionWrapper.getSession();
    }

    private void checkOpened() throws SSHException {
        if (session == null || !session.isConnected()) {
            throw new SSHException("Haven't open ssh session yet");
        }

    }


    public void releaseConnection() {
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


    public synchronized ExecResult exec(AbstractCommand command, SSHExecRunningHandler handler) throws SSHException {
        checkOpened();
        try {
            return executeCommandByType(command, handler);
        } finally {
            closeChannel();
        }
    }

    private void closeChannel() {
        if (channel != null) {
            channel.disconnect();
            log.info("Connection channel disconnect");
        }
        if (sftpChannel != null) {
            sftpChannel.disconnect();
            log.info("Sftp channel disconnect");
        }

    }

    private ExecResult executeCommandByType(AbstractCommand command, SSHExecRunningHandler handler) throws SSHException {
        if (command instanceof ConsoleCommand) {
            ConsoleCommand executeCommand = (ConsoleCommand) command;
            return executeConsoleCommand(executeCommand, handler);
        } else if (command instanceof DownloadFileCommand) {
            DownloadFileCommand executeCommand = (DownloadFileCommand) command;
            return downloadFileFromServer(executeCommand);
        } else if (command instanceof ShellCommand) {
            ShellCommand executeCommand = (ShellCommand) command;
            return executeShellCommand(executeCommand, handler);
        } else if (command instanceof UploadContentCommand) {
            UploadContentCommand executeCommand = (UploadContentCommand) command;
            return uploadContent(executeCommand);
        } else if (command instanceof UploadDirCommand) {
            UploadDirCommand executeCommand = (UploadDirCommand) command;
            return uploadDir(executeCommand, handler);
        } else if (command instanceof UploadFileCommand) {
            UploadFileCommand executeCommand = (UploadFileCommand) command;
            return uploadFile(executeCommand);
        } else if (command instanceof UploadStreamCommand) {
            UploadStreamCommand executeCommand = (UploadStreamCommand) command;
            return uploadStream(executeCommand);
        } else {
            throw new SSHException("Unsupported command type " + command.getClass());
        }
    }

    private ExecResult uploadStream(UploadStreamCommand command) throws SSHException {
        openSftpChannel();
        String remoteFileAbsolutePath = command.getRemoteFileAbsolutePath();
        SlashPath filePath = new SlashPath(remoteFileAbsolutePath);
        String parentAbsolutePath = makeRemoteDirs(filePath.getParentPath());
        String remoteFullPath = new SlashPath(parentAbsolutePath, filePath.getName()).getFullPath();
        try {
            sftpChannel.put(command.getStream(), remoteFullPath);
        } catch (SftpException e) {
            throw new SSHException(e);
        }
        return ExecResult.builder()
                .standardOutputMessage("upload success , remote path is " + remoteFullPath)
                .success(true).build();
    }


    private ExecResult uploadFile(UploadFileCommand command) throws SSHException {
        openSftpChannel();
        String remoteFileAbsolutePath = command.getRemoteFileAbsolutePath();
        SlashPath filePath = new SlashPath(remoteFileAbsolutePath);
        String parentAbsolutePath = makeRemoteDirs(filePath.getParentPath());
        String remoteFullPath = new SlashPath(parentAbsolutePath, filePath.getName()).getFullPath();
        try {
            sftpChannel.put(command.getLocalFileAbsolutePath(), remoteFullPath);
        } catch (SftpException e) {
            throw new SSHException(e);
        }
        return ExecResult.builder()
                .standardOutputMessage("upload local file " + command.getLocalFileAbsolutePath() + " success , remote path is " + remoteFullPath)
                .success(true).build();
    }

    private ExecResult uploadDir(UploadDirCommand command, SSHExecRunningHandler handler) throws SSHException {
        openSftpChannel();
        String localDir = command.getLocalDir();
        String remoteParentDir = command.getRemoteParentDir();
        File localDirFile = new File(localDir);
        if (!localDirFile.exists()) {
            throw new SSHException("local directory doesn't exist!");
        }
        if (!localDirFile.isDirectory()) {
            throw new SSHException(localDir + " is a file!");
        }
        SlashPath remoteBaseDirPath = new SlashPath(remoteParentDir, localDirFile.getName());
        makeRemoteDirs(remoteBaseDirPath.getFullPath());
        Collection<File> files = FileUtils.listFiles(localDirFile, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
        for (File f : files) {
            String localDirPath = localDirFile.getAbsolutePath();
            String currentFileParentPath = f.getParentFile().getAbsolutePath();
            String subParent = currentFileParentPath.substring(localDirPath.length());
            try {
                SlashPath rp = new SlashPath(remoteBaseDirPath, subParent);
                String rpAbsolute = makeRemoteDirs(rp.getFullPath());
                String remoteFullPath = new SlashPath(rpAbsolute, f.getName()).getFullPath();
                sftpChannel.put(f.getAbsolutePath(), remoteFullPath);
            } catch (SftpException e1) {
                throw new SSHException(e1);
            }
        }
        return ExecResult.builder()
                .standardOutputMessage("upload local dir " + localDir + " success , remote path is " + remoteParentDir)
                .success(true).build();
    }

    private ExecResult uploadContent(UploadContentCommand command) throws SSHException {
        try {
            UploadStreamCommand uploadStreamCommand = new UploadStreamCommand(command.getRemoteFileAbsolutePath(), IOUtils.toInputStream(command.getContent(), StandardCharsets.UTF_8.name()));
            return uploadStream(uploadStreamCommand);
        } catch (IOException e) {
            throw new SSHException(e);
        }

    }

    private ExecResult executeShellCommand(ShellCommand command, SSHExecRunningHandler handler) throws SSHException {
        SlashPath fullShellPath = new SlashPath(command.getShellServerWorkDir(), command.getShellName());
        if (command.getShellContent() != null && !"".equals(command.getShellContent())) {
            try {
                InputStream is = IOUtils.toInputStream(command.getShellContent(), StandardCharsets.UTF_8.name());
                UploadStreamCommand uploadStreamCommand = new UploadStreamCommand(fullShellPath.getFullPath(), is);
                uploadStream(uploadStreamCommand);
            } catch (IOException e) {
                throw new SSHException(e);
            }

        }
        ConsoleCommand executeCommand = new ConsoleCommand()
                .appendCommand("chmod +x " + fullShellPath.getFullPath()).appendCommand(command.buildRunCmd());
        executeCommand.setWithSudo(command.isWithSudo());
        return executeConsoleCommand(executeCommand, handler);
    }

    private ExecResult downloadFileFromServer(DownloadFileCommand command) throws SSHException {
        openSftpChannel();
        String localFileAbsolutePath = command.getLocalFileAbsolutePath();
        File parentFile = new File(localFileAbsolutePath).getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new SSHException("create local dir " + parentFile + " failed");
        }
        try {
            sftpChannel.get(command.getRemoteFileAbsolutePath(), localFileAbsolutePath);
        } catch (SftpException e) {
            throw new SSHException(e);
        }
        return ExecResult.builder()
                .standardOutputMessage("download remote file " + command.getRemoteFileAbsolutePath() + " success , local path is " + localFileAbsolutePath)
                .success(true).build();
    }

    private void openExecuteChannel() throws SSHException {
        try {
            channel = session.openChannel("exec");
        } catch (JSchException e) {
            throw new SSHException(e);
        }
    }

    public boolean executeSuccess(int exitStatus) {
        return exitStatus == 0;
    }

    private ExecResult executeConsoleCommand(ConsoleCommand command, SSHExecRunningHandler handler) throws SSHException {
        openExecuteChannel();
        configureChannelCommand(command);
        ExecResult execResult;
        try (ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {
            configureChannelStream(errorStream);
            connectChannel();
            execResult = waitUntilFinish(command, handler);
            execResult.setErrorMessage(new String(errorStream.toByteArray(), StandardCharsets.UTF_8));
            return execResult;
        } catch (IOException e) {
            throw new SSHException(e);
        }
    }

    private void connectChannel() throws SSHException {
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new SSHException(e);
        }
    }

    private ExecResult waitUntilFinish(ConsoleCommand command, SSHExecRunningHandler handler) throws SSHException {
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
                    return ExecResult.builder()
                            .success(executeSuccess(channel.getExitStatus()))
                            .standardOutputMessage(standardOutputMessage.toString())
                            .build();

                }
            }
        } catch (IOException e) {
            throw new SSHException(e);
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


    private void openSftpChannel() throws SSHException {
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
        } catch (JSchException e) {
            throw new SSHException(e);
        }
    }


    public synchronized void uploadFileToServer(InputStream is, String remoteFileAbsolutePath) throws SSHException {
        checkOpened();


    }

    private void closeSftpChannel() {
        if (sftpChannel != null) {
            sftpChannel.disconnect();
            sftpChannel = null;
        }
    }


    private String makeRemoteDirs(String remoteParentDir) throws SSHException {
        ConsoleCommand mkdir = new ConsoleCommand()
                .appendCommand("mkdir -p " + remoteParentDir)
                .appendCommand("cd " + remoteParentDir).appendCommand("pwd");
        ExecResult result = executeConsoleCommand(mkdir, null);
        if (result.isSuccess()) {
            return result.getStandardOutputMessage().trim();
        }
        throw new SSHException("make dir " + remoteParentDir + " error " + result.getErrorMessage());

    }

}
