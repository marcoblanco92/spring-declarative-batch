package com.marbl.declarative_batct.spring_declarative_batch.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BulkBatchReader {
    String name();
}
