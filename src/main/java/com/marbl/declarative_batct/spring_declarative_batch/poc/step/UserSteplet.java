package com.marbl.declarative_batct.spring_declarative_batch.poc.step;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.StepFactory;
import com.marbl.declarative_batct.spring_declarative_batch.poc.entity.UserEntity;
import com.marbl.declarative_batct.spring_declarative_batch.poc.model.UserCsv;
import com.marbl.declarative_batct.spring_declarative_batch.poc.step.processor.PocProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@BulkBatchSteplet(name = "pocStep")
public class UserSteplet extends AbstractSteplet<UserCsv, UserEntity> implements StepExecutionListener {

    public UserSteplet(StepFactory stepFactory, PocProcessor pocProcessor) {
        super(stepFactory);
        this.pocProcessor = pocProcessor;
    }

    private final PocProcessor pocProcessor;

    @Override
    public ItemProcessor<UserCsv,UserEntity> processor() {
        return pocProcessor;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step execution started for {}", stepExecution.getStepName());
    }
}
