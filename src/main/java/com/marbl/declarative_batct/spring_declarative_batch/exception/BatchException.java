package com.marbl.declarative_batct.spring_declarative_batch.exception;

public class BatchException extends RuntimeException {
        public BatchException(String msg, Exception exception) { super(msg, exception); }
    }