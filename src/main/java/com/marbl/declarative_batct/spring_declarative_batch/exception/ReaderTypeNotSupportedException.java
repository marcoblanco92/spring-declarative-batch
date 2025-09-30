package com.marbl.declarative_batct.spring_declarative_batch.exception;

public class ReaderTypeNotSupportedException extends RuntimeException {
    public ReaderTypeNotSupportedException(String msg) {
        super(msg);
    }
}