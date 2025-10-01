package com.marbl.declarative_batct.spring_declarative_batch.factory.job;

import com.marbl.declarative_batct.spring_declarative_batch.factory.step.StepFactory;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.BatchJobConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.StepTransitionConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.StepsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class JobFactory {

    private final StepFactory stepFactory;
    private final JobRepository jobRepository;

    /**
     * Create a Job dynamically from YAML config
     */
    public Job createJob(BatchJobConfig jobConfig) throws Exception {
        if (jobConfig == null || jobConfig.getSteps() == null || jobConfig.getSteps().isEmpty()) {
            throw new IllegalArgumentException("Job config must contain at least one step");
        }

        log.info("Creating job '{}'", jobConfig.getName());

        // --- Create all steps ---
        Map<String, Step> stepsMap = new HashMap<>();
        for (StepsConfig stepConfig : jobConfig.getSteps()) {
            Step step = stepFactory.createStep(stepConfig);
            stepsMap.put(stepConfig.getName(), step);
            log.info("Step '{}' created", stepConfig.getName());
        }

        // --- Initialize JobBuilder with first step ---
        Step firstStep = stepsMap.get(jobConfig.getSteps().get(0).getName());
        SimpleJobBuilder jobBuilder = new JobBuilder(jobConfig.getName(), jobRepository)
                .start(firstStep);

        // --- Link steps and flows dynamically ---
        for (StepsConfig stepConfig : jobConfig.getSteps()) {
            Step currentStep = stepsMap.get(stepConfig.getName());

            // Conditional transitions
            if (stepConfig.getTransitions() != null && !stepConfig.getTransitions().isEmpty()) {
                for (StepTransitionConfig transition : stepConfig.getTransitions()) {
                    Step nextStep = stepsMap.get(transition.getToStep());
                    if (nextStep == null) {
                        throw new IllegalArgumentException("Transition references unknown step: " + transition.getToStep());
                    }

                    SimpleFlow flow = new FlowBuilder<SimpleFlow>("flow-" + currentStep.getName() + "-" + nextStep.getName())
                            .start(currentStep)
                            .on(transition.getOnCondition())
                            .to(nextStep)
                            .end(); // flow-level end
                    jobBuilder.next((JobExecutionDecider) flow);
                }
            }
            // Linear next
            else if (stepConfig.getNext() != null) {
                Step nextStep = stepsMap.get(stepConfig.getNext());
                if (nextStep == null) {
                    throw new IllegalArgumentException("Next step not found: " + stepConfig.getNext());
                }

                SimpleFlow flow = new FlowBuilder<SimpleFlow>("flow-" + currentStep.getName() + "-" + nextStep.getName())
                        .start(currentStep)
                        .next(nextStep)
                        .end(); // flow-level end
                jobBuilder.next((JobExecutionDecider) flow);
            }
        }

        // --- Build the job (no .end() on job itself) ---
        Job job = jobBuilder.build();
        log.info("Job '{}' created successfully with {} steps", jobConfig.getName(), stepsMap.size());

        return job;
    }
}
