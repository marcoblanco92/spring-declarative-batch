package com.marbl.declarative_batct.spring_declarative_batch.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BulkBatchProcessor {
    String name();
}
