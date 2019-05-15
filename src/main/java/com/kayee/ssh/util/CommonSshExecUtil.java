package com.kayee.ssh.util;


import com.kayee.ssh.command.*;
import com.kayee.ssh.core.ExecResult;
import com.kayee.ssh.core.SSHConnection;
import com.kayee.ssh.exception.SSHConnectionException;
import com.kayee.ssh.exception.SSHExecuteException;
import com.kayee.ssh.handler.SSHExecResultHandler;
import com.kayee.ssh.handler.SSHExecRunningHandler;
import com.kayee.ssh.vo.ConnVo;
import com.kayee.ssh.vo.MsgVO;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhaokai
 */
public class CommonSshExecUtil {
    private static final String ROOT_USERNAME = "root";

    /**
     * 批量执行命令行、脚本、上传文件等操作：按照commands传入的顺序执行
     *
     * @param vo
     * @param commands
     * @return
     */
    public static HashMap<AbstractCommand, MsgVO> exec(final ConnVo vo, AbstractCommand... commands) throws SSHExecuteException, SSHConnectionException {
        return exec(null, null, vo, commands);
    }

    /**
     * 批量执行命令行、脚本、上传文件等操作：按照commands传入的顺序执行
     *
     * @param connInfo
     * @param commands
     * @return
     */
    public static HashMap<AbstractCommand, MsgVO> exec(SSHExecRunningHandler runningHandler, SSHExecResultHandler resultHandler, final ConnVo connInfo, AbstractCommand... commands) throws SSHExecuteException, SSHConnectionException {
        final HashMap<AbstractCommand, MsgVO> result = new HashMap<>();
        SSHConnection conn = new SSHConnection(connInfo);
        conn.openConnection();
        try {
            for (final AbstractCommand command : commands) {

                MsgVO msgVO = new MsgVO();
                if (command instanceof ConsoleCommand) {
                    ExecResult execResult = conn.exec(command, runningHandler);
                    msgVO.setSuccess(execResult.isSuccess());
                    msgVO.setSysoutMsg(execResult.getSystemOut());
                    msgVO.setErrorMsg(execResult.getErrorOut());
                } else if (command instanceof ShellCommand) {
                    ShellCommand shellCmd = (ShellCommand) command;
                    if (shellCmd.getShellContent() != null && !"".equals(shellCmd.getShellContent())) {
                        // 有脚本内容，则先要执行文件上传
                        InputStream is = IOUtils.toInputStream(shellCmd.getShellContent(), StandardCharsets.UTF_8.name());
                        String remoteFilePath = (shellCmd.getShellServerWorkDir() + "/" + shellCmd.getShellName()).replaceAll("/+", "/");
                        conn.uploadFileToServer(is, remoteFilePath);

                    }
                    ConsoleCommand chmodWithExecutable = new ConsoleCommand();
                    chmodWithExecutable.appendCommand("chmod +x " + shellCmd.getShellServerWorkDir() + "/" + shellCmd.getShellName());
                    conn.exec(chmodWithExecutable, null);
                    ExecResult execResult = conn.exec(command, runningHandler);
                    msgVO.setSuccess(execResult.isSuccess());
                    msgVO.setSysoutMsg(execResult.getSystemOut());
                    msgVO.setErrorMsg(execResult.getErrorOut());

                } else if (command instanceof UploadFileCommand) {
                    UploadFileCommand uploadFileCommand = (UploadFileCommand) command;
                    conn.uploadFileToServer(uploadFileCommand.getLocalFileAbsolutePath(), uploadFileCommand.getRemoteFileAbsolutePath());
                    msgVO.setSysoutMsg("上传文件" + uploadFileCommand.getLocalFileAbsolutePath() + "->" + uploadFileCommand.getRemoteFileAbsolutePath() + "成功！");
                    msgVO.setSuccess(true);
                } else if (command instanceof UploadContentCommand) {
                    UploadContentCommand upload = (UploadContentCommand) command;
                    conn.uploadFileToServer(IOUtils.toInputStream(upload.getContent(), StandardCharsets.UTF_8.name()), upload.getRemoteFileAbsolutePath());
                    msgVO.setSysoutMsg("写服务器文件" + upload.getRemoteFileAbsolutePath() + "成功！");
                    msgVO.setSuccess(true);
                } else if (command instanceof UploadStreamCommand) {
                    UploadStreamCommand upload = (UploadStreamCommand) command;
                    conn.uploadFileToServer(upload.getStream(), upload.getRemoteFileAbsolutePath());
                    msgVO.setSysoutMsg("写服务器文件" + upload.getRemoteFileAbsolutePath() + "成功！");
                    msgVO.setSuccess(true);
                } else if (command instanceof DownloadFileCommand) {
                    DownloadFileCommand downloadFileCommand = (DownloadFileCommand) command;
                    conn.downloadFileFromServer(downloadFileCommand.getRemoteFileAbsolutePath(), downloadFileCommand.getLocalFileAbsolutePath());
                    msgVO.setSysoutMsg("下载文件" + downloadFileCommand.getRemoteFileAbsolutePath() + "->" + downloadFileCommand.getLocalFileAbsolutePath() + "成功！");
                    msgVO.setSuccess(true);
                } else if (command instanceof UploadDirCommand) {
                    UploadDirCommand uploadDirCommand = (UploadDirCommand) command;
                    conn.uploadLocalDirToServer(uploadDirCommand.getLocalDir(), uploadDirCommand.getRemoteParentDir());
                    msgVO.setSysoutMsg("上传文件夹" + uploadDirCommand.getLocalDir() + "->" + uploadDirCommand.getRemoteParentDir() + "成功！");
                    msgVO.setSuccess(true);
                } else {
                    throw new SSHExecuteException("无效命令类型：" + command.getClass());
                }

                if (!msgVO.isSuccess()) {
                    throw new SSHExecuteException(msgVO.getErrorMsg());
                }
                result.put(command, msgVO);
                if (resultHandler != null) {
                    resultHandler.handleMessage(command, msgVO);
                }
            }
        } catch (Exception e) {
            String exceptionMsgContent = getExceptionMsgContent(e);
            String solve = solveUserPermission(exceptionMsgContent);
            String replaceWithEmptyString = "[sudo] password for " + connInfo.getUser() + ":";
            String exceptionMsg = (exceptionMsgContent + solve).replace(replaceWithEmptyString, "");
            throw new SSHExecuteException(exceptionMsg);
        } finally {
            conn.closeConnection();


        }
        return result;
    }


    public static String getExceptionMsgContent(Exception e) {
        StringWriter out = null;
        String log;
        List<String> logList = new ArrayList<>();
        try {
            out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            log = out.toString();

            String[] lines = log.split("\n");
            // 解析异常中的信息
            for (String line : lines) {
                if (line == null || "".equals(line) || line.trim().startsWith("at") || line.trim().startsWith("...")) {
                    continue;
                }
                int startIndex = line.lastIndexOf("Exception:");
                if (startIndex < 0) {
                    if (!logList.contains(line.trim())) {
                        logList.add(line.trim());
                    }
                    continue;
                } else {
                    String lineMsg = line.substring(startIndex + "Exception:".length()).trim();
                    if (!logList.contains(lineMsg)) {
                        logList.add(lineMsg);
                    }
                }
            }
            // 无信息，则返回原始堆栈信息
            if (logList.size() == 0) {
                return log;
            }
            // 有信息，则返回解析后的信息
            StringBuilder sb = new StringBuilder();
            for (String s : logList) {
                sb.append(s).append("\n");
            }

            return sb.toString().trim().replaceAll("java.lang.reflect.InvocationTargetException", "") + "\n";
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * 根据错误提示信息，获取用户权限解决方式
     *
     * @param msg
     * @return
     */
    public static String solveUserPermission(String msg) {

        String errorMsgEnglish = "you must have a tty to run sudo";
        String errorMsgChinese = "您必须拥有一个终端来执行 sudo";

        if (msg != null && (msg.contains(errorMsgEnglish) || msg.contains(errorMsgChinese))) {
            return "( 请注释掉/etc/sudoers 文件中 Defaults    requiretty 配置项 )";
        }
        String notSudoerChinese = "不在 sudoers 文件中";
        String notSudoerEnginlish = "is not in the sudoers file";
        if (msg != null && (msg.contains(notSudoerChinese) || msg.contains(notSudoerEnginlish))) {
            return "( 请在文件/etc/sudoers 中添加sudo 用户配置，型如：username     ALL=(ALL)       ALL )";
        }
        return msg;

    }

}
