package com.oj.judge.sandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SandboxResult {

    private boolean timedOut;
    private Integer exitCode;
    private String stdout;
    private String stderr;
    private long runtimeMs;
    private boolean compilationFailed;
}
