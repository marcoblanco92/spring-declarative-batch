package com.marbl.declarative_batch.spring_declarative_batch.poc.step;

import com.marbl.declarative_batch.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batch.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batch.spring_declarative_batch.factory.step.StepFactory;
import com.marbl.declarative_batch.spring_declarative_batch.poc.dto.ClientTransactionsDTO;
import com.marbl.declarative_batch.spring_declarative_batch.poc.entity.UserAuxEntity;
import com.marbl.declarative_batch.spring_declarative_batch.poc.step.processor.CustomProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@BulkBatchSteplet(name = "customStep")
public class CustomSteplet extends AbstractSteplet<ClientTransactionsDTO, UserAuxEntity> implements StepExecutionListener {

    public CustomSteplet(StepFactory stepFactory, FlatFileItemReader<ClientTransactionsDTO> customFileReader, CustomProcessor customProcessor, ItemWriter<UserAuxEntity>  tracingItemWriter) {
        super(stepFactory);
        this.customFileReader = customFileReader;
        this.customProcessor = customProcessor;
        this.tracingItemWriter = tracingItemWriter;
    }

    private final FlatFileItemReader<ClientTransactionsDTO> customFileReader;
    private final CustomProcessor customProcessor;
    private final ItemWriter<UserAuxEntity> tracingItemWriter;

    @Override
    public ItemReader<ClientTransactionsDTO> reader() {return customFileReader;}

    @Override
    public ItemProcessor<ClientTransactionsDTO,UserAuxEntity> processor() {
        return customProcessor;
    }

    @Override
    public ItemWriter<UserAuxEntity> writer() {return tracingItemWriter;}

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step execution started for {}", stepExecution.getStepName());
    }


}
