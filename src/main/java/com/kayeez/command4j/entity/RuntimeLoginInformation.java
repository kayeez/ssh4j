package com.kayeez.command4j.entity;

import lombok.*;

@Builder
@Getter
public class RuntimeLoginInformation implements LoginInformation {
    private String sudoPassword;

}
