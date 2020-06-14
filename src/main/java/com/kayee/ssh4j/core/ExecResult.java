package com.kayee.ssh4j.core;

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
public class ExecResult {
    private String standardOutputMessage;
    private String errorMessage;
    private boolean success;
}
