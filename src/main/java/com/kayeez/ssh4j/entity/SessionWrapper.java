package com.kayeez.ssh4j.entity;


import com.jcraft.jsch.Session;
import lombok.*;

import java.util.Date;

/**
 * @author: zhaokai
 * @create: 2018-09-05 18:13:35
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionWrapper {
    private Long threadId;
    private Session session;
    private Date bindTime;
}
