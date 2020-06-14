package com.kayee.ssh4j.util;

import com.kayee.ssh4j.command.AbstractCommand;
import com.kayee.ssh4j.command.ConsoleCommand;
import com.kayee.ssh4j.core.ExecResult;
import com.kayee.ssh4j.entity.SSHLoginInformation;
import com.kayee.ssh4j.handler.SSHExecRunningHandler;
import org.junit.Test;

import java.util.Map;

public class CommonSSHExecUtilTest {

    @Test
    public void test_normal_command() throws Exception {
        ConsoleCommand consoleCommand = new ConsoleCommand().appendCommand("ls /abc");
        SSHLoginInformation loginInformation = SSHLoginInformation.builder()
                .ip("192.168.1.11")
                .user("root")
                .port(22)
                .pwd("1234")
                .build();
        Map<AbstractCommand, ExecResult> exec = CommonSSHExecUtil.exec(new SSHExecRunningHandler() {
            @Override
            public void handle(AbstractCommand cmd, String outOneLineLog) {
                System.out.println(outOneLineLog);
            }
        }, loginInformation, consoleCommand);
        exec.forEach((key, val) -> {
            System.out.println(val.isSuccess());
        });

    }
}
