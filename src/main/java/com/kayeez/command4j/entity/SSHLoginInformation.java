package com.kayeez.command4j.entity;


import lombok.*;

/**
 * @author zhaokai
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SSHLoginInformation implements LoginInformation {
    private String ip;
    private Integer port;
    private String username;
    private String password;
}
