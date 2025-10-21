package com.marbl.declarative_batch.spring_declarative_batch.poc.step.tasklet;


import com.marbl.declarative_batch.spring_declarative_batch.annotation.BulkBatchTasklet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@BulkBatchTasklet(name= "pocTasklet")
public class PocTasklet implements Tasklet {


    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("Executed step: {}", chunkContext.getStepContext().getStepName());
        log.info("isComplete: {}",chunkContext.isComplete());

        return RepeatStatus.FINISHED;
    }
}
