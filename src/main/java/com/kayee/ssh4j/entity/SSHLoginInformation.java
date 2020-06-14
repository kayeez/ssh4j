package com.kayee.ssh4j.entity;


import lombok.*;

/**
 * @author zhaokai
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SSHLoginInformation {
    private String ip;
    private Integer port;
    private String user;
    private String pwd;
}
