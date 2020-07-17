package com.kayeez.ssh4j.entity;

import lombok.*;

/**
 * @author: zhaokai
 * @create: 2018-08-22 14:45:29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteResult {
    private String standardOutputMessage;
    private String errorMessage;
    private boolean success;
}
