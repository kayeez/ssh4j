package com.kayee.ssh4j.util;


import com.kayee.ssh4j.command.AbstractCommand;
import com.kayee.ssh4j.core.ExecResult;
import com.kayee.ssh4j.core.SSHConnection;
import com.kayee.ssh4j.entity.SSHLoginInformation;
import com.kayee.ssh4j.exception.SSHException;
import com.kayee.ssh4j.handler.SSHExecResultHandler;
import com.kayee.ssh4j.handler.SSHExecRunningHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaokai
 */
public class CommonSSHExecUtil {
    private static final String ROOT_USERNAME = "root";

    /**
     * 批量执行命令行、脚本、上传文件等操作：按照commands传入的顺序执行
     *
     * @param sshLoginInformation
     * @param commands
     * @return
     */
    public static Map<AbstractCommand, ExecResult> exec(final SSHLoginInformation sshLoginInformation, AbstractCommand... commands) throws SSHException {
        return exec(null, null, sshLoginInformation, commands);
    }

    public static Map<AbstractCommand, ExecResult> exec(SSHExecRunningHandler runningHandler, final SSHLoginInformation sshLoginInformation, AbstractCommand... commands) throws SSHException {
        return exec(runningHandler, null, sshLoginInformation, commands);
    }

    /**
     * 批量执行命令行、脚本、上传文件等操作：按照commands传入的顺序执行
     *
     * @param sshLoginInformation
     * @param commands
     * @return
     */
    public static Map<AbstractCommand, ExecResult> exec(SSHExecRunningHandler runningHandler, SSHExecResultHandler resultHandler, final SSHLoginInformation sshLoginInformation, AbstractCommand... commands) throws SSHException {
        final HashMap<AbstractCommand, ExecResult> result = new HashMap<>();
        SSHConnection conn = new SSHConnection(sshLoginInformation);
        conn.openConnection();
        try {
            for (final AbstractCommand command : commands) {
                ExecResult execResult = conn.exec(command, runningHandler);
                result.put(command, execResult);
                if (resultHandler != null) {
                    resultHandler.handleMessage(command, execResult);
                }
            }
        } catch (Exception e) {
            String exceptionMsgContent = getExceptionMsgContent(e);
            String solve = solveUserPermission(exceptionMsgContent);
            String replaceWithEmptyString = "[sudo] password for " + sshLoginInformation.getUser() + ":";
            String exceptionMsg = (exceptionMsgContent + solve).replace(replaceWithEmptyString, "");
            throw new SSHException(exceptionMsg);
        } finally {
            conn.releaseConnection();
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
