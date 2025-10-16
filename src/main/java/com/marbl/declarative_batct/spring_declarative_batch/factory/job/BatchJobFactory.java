package com.marbl.declarative_batct.spring_declarative_batch.factory.job;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchValidator;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.*;
import com.marbl.declarative_batct.spring_declarative_batch.exception.BatchException;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.support.incrementer.DatabaseRunIdIncrementer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobFactory implements JobFactory {

    private final BatchJobConfig jobConfig;
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;
    private final ApplicationContext context;
    private final @Nullable RunIdIncrementer runIdIncrementer;

    @Override
    public String getJobName() {
        return jobConfig.getName();
    }

    @Override
    public Job createJob() {
        if (jobConfig == null || jobConfig.getSteps() == null || jobConfig.getSteps().isEmpty()) {
            throw new IllegalArgumentException("Job config must contain at least one step");
        }

        log.info("Creating job '{}'", jobConfig.getName());

        // --- Create steps dynamically from steplets ---
        Map<String, Step> stepsMap = createStepsFromSteplets(jobConfig);

        // --- Build dynamic flow (supports next + conditional) ---
        Flow mainFlow = buildDynamicFlow(jobConfig, stepsMap);

        // --- Initialize JobBuilder ---
        JobBuilder jobBuilder = new JobBuilder(jobConfig.getName(), jobRepository);

        // --- Attach Job Listener if configured ---
        attachJobListener(jobBuilder, jobConfig);

        // --- Attach Job Parameters Validator if configured ---
        attachJobValidator(jobBuilder, jobConfig);

        // --- Attach Incrementer if applicable ---
        if (runIdIncrementer != null) {
            log.info("RunIdIncrementer is used, incrementer: {}", runIdIncrementer);
            jobBuilder.incrementer(new DatabaseRunIdIncrementer(jobConfig, jobExplorer));
        }

        // --- Build final Job ---
        Job job = jobBuilder
                .start(mainFlow)
                .end()
                .build();

        log.info("Job '{}' created successfully with {} steps", jobConfig.getName(), stepsMap.size());
        return job;
    }

    // ---------------------------------------------------
    // Helper methods
    // ---------------------------------------------------

    private Map<String, Step> createStepsFromSteplets(BatchJobConfig jobConfig) {
        Map<String, Step> stepsMap = new HashMap<>();

        try {
            for (StepsConfig stepConfig : jobConfig.getSteps()) {
                AbstractSteplet<?, ?> steplet = resolveStepletBean(stepConfig);
                steplet.setConfig(stepConfig);
                Step step = steplet.buildStep();
                stepsMap.put(stepConfig.getName(), step);
                log.info("Step '{}' created via steplet '{}'", stepConfig.getName(), steplet.getClass().getSimpleName());
            }
        } catch (Exception e) {
            throw new BatchException("Error while building steps: " + e.getMessage(), e);
        }

        // Warn for unused annotated beans
        Map<String, Object> annotatedSteplets = context.getBeansWithAnnotation(BulkBatchSteplet.class);
        for (Object bean : annotatedSteplets.values()) {
            BulkBatchSteplet ann = bean.getClass().getAnnotation(BulkBatchSteplet.class);
            if (!stepsMap.containsKey(ann.name())) {
                log.warn("Annotated steplet bean '{}' ({}) is not used in YAML configuration",
                        ann.name(), bean.getClass().getSimpleName());
            }
        }

        return stepsMap;
    }

    private Flow buildDynamicFlow(BatchJobConfig jobConfig, Map<String, Step> stepsMap) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow-" + jobConfig.getName());

        // Start dal primo step
        Step firstStep = stepsMap.get(jobConfig.getSteps().get(0).getName());
        flowBuilder.start(firstStep);

        for (StepsConfig stepConfig : jobConfig.getSteps()) {
            Step currentStep = stepsMap.get(stepConfig.getName());

            // --- 1️⃣ Conditional transitions ---
            if (stepConfig.getTransitions() != null && !stepConfig.getTransitions().isEmpty()) {
                for (StepConditionConfig transition : stepConfig.getTransitions()) {
                    Step toStep = stepsMap.get(transition.getToStep());
                    Step fromStep = stepsMap.get(transition.getFrom());
                    if (toStep == null || fromStep == null) {
                        throw new IllegalArgumentException("Transition references unknown step: " +
                                transition.getFrom() + " -> " + transition.getToStep());
                    }

                    log.info("Adding conditional transition: {} --[{}]--> {}",
                            fromStep.getName(), transition.getOnCondition(), toStep.getName());

                    flowBuilder.from(fromStep)
                            .on(transition.getOnCondition())
                            .to(toStep);

                    if (transition.isEnded()) {
                        flowBuilder.end();
                    }
                }
            }

            // --- 2️⃣ Linear 'next' transitions come AFTER le conditional ---
            if (stepConfig.getNext() != null && !stepConfig.getNext().isEmpty()) {
                Step nextStep = stepsMap.get(stepConfig.getNext());
                if (nextStep == null) {
                    throw new IllegalArgumentException("Next step not found: " + stepConfig.getNext());
                }

                log.info("Adding linear transition: {} --*--> {}", currentStep.getName(), nextStep.getName());

                // Usa wildcard "*" per fallback dopo le transizioni condizionali
                flowBuilder.from(currentStep)
                        .on("*")
                        .to(nextStep);
            }
        }

        // --- 3️⃣ Opzionale: fallback globale per tutti i branch non gestiti ---
        for (StepsConfig stepConfig : jobConfig.getSteps()) {
            Step currentStep = stepsMap.get(stepConfig.getName());
            flowBuilder.from(currentStep)
                    .on("*")
                    .end(); // termina il flow per tutti gli exit status non catturati
        }

        return flowBuilder.end();
    }


    private void attachJobListener(JobBuilder jobBuilder, BatchJobConfig jobConfig) {
        ListenerConfig jobListenerConfig = jobConfig.getListener();
        if (jobListenerConfig == null || jobListenerConfig.getName() == null) {
            return;
        }

        try {
            JobExecutionListener listener =
                    context.getBean(jobListenerConfig.getName(), JobExecutionListener.class);
            jobBuilder.listener(listener);
            log.info("Attached JobExecutionListener '{}' to job '{}'",
                    jobListenerConfig.getName(), jobConfig.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid JobExecutionListener bean: " + jobListenerConfig.getName(), e);
        }
    }

    private void attachJobValidator(JobBuilder jobBuilder, BatchJobConfig jobConfig) {
        ParametersValidatorConfig validatorConfig = jobConfig.getValidator();
        if (validatorConfig == null || !validatorConfig.isValidate()) {
            return;
        }

        Map<String, Object> validators = context.getBeansWithAnnotation(BulkBatchValidator.class);
        Object validatorBean = validators.get(validatorConfig.getName());

        if (validatorBean == null) {
            throw new IllegalArgumentException("No BulkBatchValidator bean found with name: " + validatorConfig.getName());
        }
        if (!(validatorBean instanceof JobParametersValidator)) {
            throw new IllegalArgumentException("Bean '" + validatorConfig.getName() + "' is not a JobParametersValidator");
        }

        jobBuilder.validator((JobParametersValidator) validatorBean);
        log.info("Attached JobParametersValidator '{}' to job '{}'",
                validatorConfig.getName(), jobConfig.getName());
    }

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
