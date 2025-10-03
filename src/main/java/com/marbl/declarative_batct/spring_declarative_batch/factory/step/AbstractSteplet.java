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

    public Step buildStep() throws Exception {
        return stepFactory.createStep(
                getConfig(),
                reader(),
                processor(),
                writer()
        );
    }

    protected ItemReader<I> reader() { return null; }
    protected ItemProcessor<I, O> processor() { return null; }
    protected ItemWriter<O> writer() { return null; }

    protected StepsConfig getConfig() {
        if (config == null) throw new IllegalStateException("StepsConfig not set");
        return config;
    }
}


