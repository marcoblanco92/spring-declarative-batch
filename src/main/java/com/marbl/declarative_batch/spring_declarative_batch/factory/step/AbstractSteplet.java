package com.marbl.declarative_batch.spring_declarative_batch.factory.step;

import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.StepsConfig;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.Step;

@RequiredArgsConstructor
public abstract class AbstractSteplet<I, O> implements StepComponent<I, O> {

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


    protected StepsConfig getConfig() {
        if (config == null) throw new IllegalStateException("StepsConfig not set");
        return config;
    }

}


