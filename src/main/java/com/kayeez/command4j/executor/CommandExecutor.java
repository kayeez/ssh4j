package com.kayeez.command4j.executor;

import com.kayeez.command4j.command.AbstractCommand;
import com.kayeez.command4j.connector.AbstractConnector;
import com.kayeez.command4j.core.ExecuteContext;
import com.kayeez.command4j.entity.ExecuteResult;
import com.kayeez.command4j.entity.LoginInformation;
import com.kayeez.command4j.entity.SSHLoginInformation;
import com.kayeez.command4j.exception.CommandExecuteErrorException;
import com.kayeez.command4j.handler.CommandExecutionFinishedHandler;
import com.kayeez.command4j.handler.CommandExecutionRunningHandler;

import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {

    /**
     * @param sshLoginInformation ip port username password
     * @param commands            subclass of AbstractCommand
     * @return command and execute result mapping
     */
    public Map<AbstractCommand, ExecuteResult> execute(final SSHLoginInformation sshLoginInformation,
                                                       AbstractCommand... commands) {
        return execute(null, null, sshLoginInformation, commands);
    }

    /**
     * @param runningHandler      handler on command execution
     * @param sshLoginInformation ip port username password
     * @param commands            subclass of AbstractCommand
     * @return command and execute result mapping
     * @throws CommandExecuteErrorException
     */
    public Map<AbstractCommand, ExecuteResult> execute(CommandExecutionRunningHandler runningHandler,
                                                       final SSHLoginInformation sshLoginInformation,
                                                       AbstractCommand... commands) {
        return execute(runningHandler, null, sshLoginInformation, commands);
    }

    /**
     * @param runningHandler   handler on command execution
     * @param resultHandler    handler after command execution is finished
     * @param loginInformation ip port username password
     * @param commands         subclass of AbstractCommand
     * @return command and execute result mapping
     * @throws CommandExecuteErrorException
     */
    public Map<AbstractCommand, ExecuteResult> execute(CommandExecutionRunningHandler runningHandler,
                                                       CommandExecutionFinishedHandler resultHandler,
                                                       LoginInformation loginInformation,
                                                       AbstractCommand... commands) {
        final HashMap<AbstractCommand, ExecuteResult> result = new HashMap<>();
        AbstractConnector connector = ExecuteContext.buildConnector(loginInformation);
        for (final AbstractCommand command : commands) {
            ExecuteResult executeResult = connector.exec(command, runningHandler);
            result.put(command, executeResult);
            if (resultHandler != null) {
                resultHandler.handleMessage(command, executeResult);
            }
        }

        return result;
    }


}
