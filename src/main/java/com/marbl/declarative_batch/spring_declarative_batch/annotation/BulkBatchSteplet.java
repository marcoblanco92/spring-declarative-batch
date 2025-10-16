package com.marbl.declarative_batch.spring_declarative_batch.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BulkBatchSteplet {
    String name();
}
