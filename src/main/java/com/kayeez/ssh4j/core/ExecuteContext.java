package com.kayeez.ssh4j.core;


import com.kayeez.ssh4j.annotation.ConnectorLoginMapping;
import com.kayeez.ssh4j.connector.AbstractConnector;
import com.kayeez.ssh4j.entity.LoginInformation;
import com.kayeez.ssh4j.exception.CommandExecuteErrorException;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExecuteContext {
    public volatile static long sshSessionLiveDurationMillSeconds = 1000 * 60 * 60;
    public volatile static int sshSessionConnectTimeoutMillSeconds = 1000 * 60 * 60;
    public volatile static int sshSessionInitializeDefaultSize = 2;
    private static Map<Class<? extends LoginInformation>, Constructor<? extends AbstractConnector>> connectorConstructorPool = new HashMap<>();

    static {
        Reflections reflections = new Reflections(AbstractConnector.class.getPackage().getName());
        Set<Class<? extends AbstractConnector>> connectorClassSet = reflections.getSubTypesOf(AbstractConnector.class);
        connectorClassSet.forEach(c -> {
            ConnectorLoginMapping connectorLoginMapping = c.getAnnotation(ConnectorLoginMapping.class);
            if (connectorLoginMapping == null) {
                throw new CommandExecuteErrorException("Connector " + c + " requires " + ConnectorLoginMapping.class + " annotation configuration");
            }
            Class<? extends LoginInformation> key = connectorLoginMapping.value();
            try {
                Constructor<? extends AbstractConnector> value = c.getConstructor(key);
                connectorConstructorPool.put(key, value);
            } catch (NoSuchMethodException e) {
                throw new CommandExecuteErrorException(e);
            }
        });
    }

    public static AbstractConnector buildConnector(LoginInformation loginInformation) {
        Constructor<? extends AbstractConnector> connectorConstructor = connectorConstructorPool.get(loginInformation.getClass());
        if (connectorConstructor == null) {
            throw new CommandExecuteErrorException("Connector login mapping for " + loginInformation.getClass() + " has not found");
        }
        try {
            return connectorConstructor.newInstance(loginInformation);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new CommandExecuteErrorException(e);
        }
    }
}
