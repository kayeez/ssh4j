package com.kayeez.ssh4j.executor;

import com.kayeez.ssh4j.command.ConsoleCommand;
import com.kayeez.ssh4j.entity.SSHLoginInformation;
import org.junit.Test;

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
