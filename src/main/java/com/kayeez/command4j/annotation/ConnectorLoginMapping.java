package com.kayeez.command4j.annotation;


import com.kayeez.command4j.entity.LoginInformation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorLoginMapping {
    Class<? extends LoginInformation> value();
}
