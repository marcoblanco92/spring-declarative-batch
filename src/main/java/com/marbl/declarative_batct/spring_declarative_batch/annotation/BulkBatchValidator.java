package com.marbl.declarative_batct.spring_declarative_batch.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface BulkBatchValidator {
}
