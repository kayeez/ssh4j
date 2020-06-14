package com.kayee.ssh4j.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:22:24
 */
public class ConsoleCommand extends AbstractCommand {
    private List<String> cmdList = new ArrayList<>();

    public ConsoleCommand appendCommand(String... cmd) {
        for (String c : cmd) {
            cmdList.add(c);
        }
        return this;
    }

    @Override
    public String buildRunCmd() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cmdList.size(); ++i) {
            sb.append(cmdList.get(i));
            if (i != cmdList.size() - 1) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsoleCommand that = (ConsoleCommand) o;
        return Objects.equals(cmdList, that.cmdList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cmdList);
    }
}
