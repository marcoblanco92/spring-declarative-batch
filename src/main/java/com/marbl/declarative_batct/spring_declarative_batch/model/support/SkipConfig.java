package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import lombok.Data;

import java.util.List;

@Data
public class SkipConfig {
    private int limit = 10; // default skip limit
    private List<Class<? extends Throwable>> exceptionsToSkip; // exceptions to skip
    private List<Class<? extends Throwable>> exceptionsNoSkip; // exceptions to  no skip
}