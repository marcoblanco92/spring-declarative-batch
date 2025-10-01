package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import lombok.Data;

import java.util.List;

@Data
public class RetryConfig {
    private int limit = 3; // default retry limit
    private List<Class<? extends Throwable>> exceptions; // exceptions to retry
}