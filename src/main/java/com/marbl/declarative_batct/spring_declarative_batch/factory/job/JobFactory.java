package com.marbl.declarative_batct.spring_declarative_batch.factory.job;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.BatchJobConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ListenerConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.StepConditionConfig;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobFactory {

    private final JobRepository jobRepository;
    private final ApplicationContext context;

    /**
     * Create a Job dynamically from YAML config using AbstractSteplet beans
     */
    public Job createJob(BatchJobConfig jobConfig) throws Exception {
        if (jobConfig == null || jobConfig.getSteps() == null || jobConfig.getSteps().isEmpty()) {
            throw new IllegalArgumentException("Job config must contain at least one step");
        }

        log.info("Creating job '{}'", jobConfig.getName());

        // --- Create map for unused step ---
        Map<String, AbstractSteplet<?, ?>> usedSteplets = new HashMap<>();


        // --- Create all steps using steplet beans ---
        Map<String, Step> stepsMap = new HashMap<>();
        for (StepsConfig stepConfig : jobConfig.getSteps()) {
            AbstractSteplet<?, ?> steplet = resolveStepletBean(stepConfig);
            steplet.setConfig(stepConfig);
            Step step = steplet.buildStep();
            stepsMap.put(stepConfig.getName(), step);
            usedSteplets.put(stepConfig.getName(), steplet);
            log.info("Step '{}' created via steplet '{}'", stepConfig.getName(), steplet.getClass().getSimpleName());
        }

        // --- Check for unused annotated steplet beans ---
        Map<String, Object> allAnnotatedBeans = context.getBeansWithAnnotation(BulkBatchSteplet.class);
        for (Object bean : allAnnotatedBeans.values()) {
            BulkBatchSteplet ann = bean.getClass().getAnnotation(BulkBatchSteplet.class);
            if (!usedSteplets.containsKey(ann.name())) {
                log.warn("Annotated steplet bean '{}' ({}) is not used in any YAML step",
                        ann.name(), bean.getClass().getSimpleName());
            }
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
                for (StepConditionConfig transition : stepConfig.getTransitions()) {
                    Step nextStep = stepsMap.get(transition.getToStep());
                    if (nextStep == null) {
                        throw new IllegalArgumentException("Transition references unknown step: " + transition.getToStep());
                    }

                    SimpleFlow flow = new FlowBuilder<SimpleFlow>("flow-" + currentStep.getName() + "-" + nextStep.getName())
                            .start(currentStep)
                            .on(transition.getOnCondition())
                            .to(nextStep)
                            .end();
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
                        .end();
                jobBuilder.next((JobExecutionDecider) flow);
            }
        }

        // --- Attach JobExecutionListener if defined ---
        ListenerConfig jobListenerConfig = jobConfig.getListener();
        if (jobListenerConfig != null && jobListenerConfig.getName() != null && !jobListenerConfig.getName().isEmpty()) {
            try {
                var listener = context.getBean(jobListenerConfig.getName(), org.springframework.batch.core.JobExecutionListener.class);
                jobBuilder.listener(listener);
                log.info("Attached JobExecutionListener '{}' to job '{}'", jobListenerConfig.getName(), jobConfig.getName());
            } catch (Exception e) {
                log.error("Failed to attach JobExecutionListener '{}'", jobListenerConfig.getName(), e);
                throw new IllegalArgumentException("Invalid JobExecutionListener bean: " + jobListenerConfig.getName(), e);
            }
        }

        // --- Build the job ---
        Job job = jobBuilder.build();
        log.info("Job '{}' created successfully with {} steps", jobConfig.getName(), stepsMap.size());

        return job;
    }

    /**
     * Resolve AbstractSteplet bean from Spring context using the step name
     */
    private AbstractSteplet<?, ?> resolveStepletBean(StepsConfig config) {
        Map<String, Object> beans = context.getBeansWithAnnotation(BulkBatchSteplet.class);
        for (Object bean : beans.values()) {
            BulkBatchSteplet ann = bean.getClass().getAnnotation(BulkBatchSteplet.class);
            if (config.getName().equals(ann.name())) {
                if (!(bean instanceof AbstractSteplet<?, ?>)) {
                    throw new IllegalStateException("Bean annotated with @BulkBatchSteplet must extend AbstractSteplet: " + bean.getClass());
                }
                return (AbstractSteplet<?, ?>) bean;
            }
        }
        throw new IllegalArgumentException("No steplet bean found for step name: " + config.getName());
    }

}
