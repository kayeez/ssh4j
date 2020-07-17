package com.kayeez.ssh4j.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RuntimeLoginInformation implements LoginInformation {
    private String sudoPassword;

}
