package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import lombok.Data;

import java.util.List;

@Data
public class TransactionConfig {

    private boolean isReaderInTransaction = false;
    private List<Class<? extends Throwable>> noRollbackExceptions; // exceptions to not rollback
}