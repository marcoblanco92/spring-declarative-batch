package com.marbl.declarative_batch.spring_declarative_batch.poc.step;

import com.marbl.declarative_batch.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batch.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batch.spring_declarative_batch.factory.step.StepFactory;
import com.marbl.declarative_batch.spring_declarative_batch.poc.dto.ClientTransactionsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@BulkBatchSteplet(name = "pocStep2")
public class TransactionSteplet extends AbstractSteplet<ClientTransactionsDTO, ClientTransactionsDTO> implements StepExecutionListener {

    public TransactionSteplet(StepFactory stepFactory) {
        super(stepFactory);
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step execution started for {}", stepExecution.getStepName());

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step execution finished for {}", stepExecution.getStepName());
        return new ExitStatus("COMPLETED_WITH_INTEGRATION", stepExecution.getStepName());
    }
}
