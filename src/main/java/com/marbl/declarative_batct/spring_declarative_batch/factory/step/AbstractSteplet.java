package com.marbl.declarative_batct.spring_declarative_batch.factory.step;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.StepsConfig;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public abstract class AbstractSteplet<I, O> {

    private final StepFactory stepFactory;

    @Setter
    private StepsConfig config;


    protected StepsConfig getConfig() {
        if (config == null) throw new IllegalStateException("StepsConfig not set");
        return config;
    }

    public Step buildStep() throws Exception {
        return stepFactory.<I, O>createStep(getConfig());
    }

//    protected abstract ItemReader<I> reader();
//    protected abstract ItemProcessor<I,O> processor();
//    protected abstract ItemWriter<O> writer();

}
