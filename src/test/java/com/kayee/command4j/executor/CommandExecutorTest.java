package com.kayee.command4j.executor;

import com.kayee.command4j.command.AbstractCommand;
import com.kayee.command4j.command.ConsoleCommand;
import com.kayee.command4j.entity.ExecuteResult;
import com.kayee.command4j.entity.SSHLoginInformation;
import com.kayee.command4j.handler.CommandExecutionRunningHandler;
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
        Map<AbstractCommand, ExecuteResult> exec = new CommandExecutor().execute(new CommandExecutionRunningHandler() {
            @Override
            public void handle(AbstractCommand cmd, String outOneLineLog) {
                System.out.println(outOneLineLog);
            }
        }, loginInformation, consoleCommand1);
        exec.forEach((key, val) -> {
            System.out.println(val.isSuccess()+"\n"+val.getErrorMessage()+"\n"+val.getStandardOutputMessage());
        });
    }

    @Test
    public void test_list_remove(){
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");

        List<String> collect = list.stream().filter(n -> Integer.parseInt(n) >= 3).collect(Collectors.toList());
        System.out.println(collect);
        collect.forEach(list::remove);
        System.out.println(list);

    }
}
