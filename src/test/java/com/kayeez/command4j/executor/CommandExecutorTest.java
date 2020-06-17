package com.kayeez.command4j.executor;

import com.kayeez.command4j.command.AbstractCommand;
import com.kayeez.command4j.command.ConsoleCommand;
import com.kayeez.command4j.entity.ExecuteResult;
import com.kayeez.command4j.entity.SSHLoginInformation;
import com.kayeez.command4j.handler.CommandExecutionRunningHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandExecutorTest {

    @Test
    public void test_normal_command() throws Exception {
        ConsoleCommand consoleCommand1 = new ConsoleCommand().appendCommand("ls /opt");
        SSHLoginInformation loginInformation = SSHLoginInformation.builder()
                .ip("192.168.1.11")
                .username("root")
                .port(22)
                .password("1234")
                .build();
        /*Map<AbstractCommand, ExecuteResult> exec = new CommandExecutor().execute(new CommandExecutionRunningHandler() {
            @Override
            public void handle(AbstractCommand cmd, String outOneLineLog) {
                System.out.println(outOneLineLog);
            }
        }, loginInformation, consoleCommand1);
        exec.forEach((key, val) -> {
            System.out.println(val.isSuccess()+"\n"+val.getErrorMessage()+"\n"+val.getStandardOutputMessage());
        });*/
    }

}
