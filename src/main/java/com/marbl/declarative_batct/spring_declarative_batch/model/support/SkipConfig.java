package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import lombok.Data;

import java.util.List;

@Data
public class SkipConfig {
    private int limit = 5; // default skip limit
    private List<Class<? extends Throwable>> exceptions; // exceptions to skip
}