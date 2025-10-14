package com.marbl.declarative_batct.spring_declarative_batch.factory.job;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchValidator;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.*;
import com.marbl.declarative_batct.spring_declarative_batch.exception.BatchException;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.support.incrementer.DatabaseRunIdIncrementer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
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

    /**
     * Create a Job dynamically from YAML config using AbstractSteplet beans
     */
    @Override
    public Job createJob() {
        if (jobConfig == null || jobConfig.getSteps() == null || jobConfig.getSteps().isEmpty()) {
            throw new IllegalArgumentException("Job config must contain at least one step");
        }

        log.info("Creating job '{}'", jobConfig.getName());

        // --- Create map for unused step ---
        Map<String, AbstractSteplet<?, ?>> usedSteplets = new HashMap<>();


        // --- Create all steps using steplet beans ---
        Map<String, Step> stepsMap = new HashMap<>();
        try {
            for (StepsConfig stepConfig : jobConfig.getSteps()) {
                AbstractSteplet<?, ?> steplet = resolveStepletBean(stepConfig);
                steplet.setConfig(stepConfig);
                Step step = steplet.buildStep();
                stepsMap.put(stepConfig.getName(), step);
                usedSteplets.put(stepConfig.getName(), steplet);
                log.info("Step '{}' created via steplet '{}'", stepConfig.getName(), steplet.getClass().getSimpleName());
            }
        } catch (Exception e) {
            throw new BatchException(e.getMessage(), e);
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
        for (int stepIndex = 0; stepIndex < jobConfig.getSteps().size(); stepIndex++) {
            StepsConfig stepConfig = jobConfig.getSteps().get(stepIndex);
            Step currentStep = stepsMap.get(stepConfig.getName());

            // --- Handle linear next ---
            if (stepConfig.getNext() != null) {
                String nextStepName = stepConfig.getNext();

                // Check that the nextStep is defined in the YAML job configuration
                boolean nextStepExists = jobConfig.getSteps().stream()
                        .anyMatch(s -> s.getName().equals(nextStepName));

                if (!nextStepExists) {
                    throw new IllegalArgumentException(
                            "Step '" + stepConfig.getName() + "' declares next step '" + nextStepName +
                                    "' which is not defined in the YAML job configuration"
                    );
                }

                // Ensure that the declared next step corresponds to the next one in YAML order
                if (stepIndex + 1 < jobConfig.getSteps().size()) {
                    String expectedNext = jobConfig.getSteps().get(stepIndex + 1).getName();
                    if (!nextStepName.equals(expectedNext)) {
                        throw new IllegalArgumentException(
                                "Step '" + stepConfig.getName() + "' declares next step '" + nextStepName +
                                        "', but the next step in YAML sequence is '" + expectedNext +
                                        "'. When 'next' is declared, it must refer to the immediate subsequent step."
                        );
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Step '" + stepConfig.getName() + "' declares a 'next' step but it is the last one in the job sequence"
                    );
                }

                // Retrieve the nextStep from the map of created steps
                Step nextStep = stepsMap.get(nextStepName);

                // Chain the next step to the current job builder
                jobBuilder.next(nextStep);
                log.info("Step '{}' linked linearly to '{}'", stepConfig.getName(), nextStepName);
            }

            // --- Handle conditional transitions ---
            if (stepConfig.getTransitions() != null && !stepConfig.getTransitions().isEmpty()) {
                for (int i = 0; i < stepConfig.getTransitions().size(); i++) {
                    StepConditionConfig transition = stepConfig.getTransitions().get(i);
                    Step toStep = stepsMap.get(transition.getToStep());

                    if (toStep == null) {
                        throw new IllegalArgumentException("Transition references unknown step: " + transition.getToStep());
                    }

                    FlowBuilder<SimpleFlow> flow = new FlowBuilder<>("flow-" + currentStep.getName() + "-" + toStep.getName());

                    // Only first transition of the first step must not have 'from'
                    if (stepIndex == 0 && i == 0 && transition.getFrom() != null) {
                        throw new IllegalArgumentException("The first transition of the first step must not have 'from'");
                    }

                    if (transition.getFrom() != null) {
                        Step fromStep = stepsMap.get(transition.getFrom());
                        if (fromStep == null) {
                            throw new IllegalArgumentException("Transition 'from' references unknown step: " + transition.getFrom());
                        }
                        flow.from(fromStep)
                                .on(transition.getOnCondition())
                                .to(toStep);
                    } else {
                        // first transition implicitly from start
                        flow.on(transition.getOnCondition())
                                .to(toStep);
                    }

                    if (transition.isEnded()) {
                        flow.end();
                    }

                    jobBuilder.next((JobExecutionDecider) flow.build());
                }
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

        // --- Attach Params Validator if Allowed
        ParametersValidatorConfig validatorConfig = jobConfig.getValidator();
        if (validatorConfig != null && validatorConfig.getName() != null
                && !validatorConfig.getName().isEmpty() && validatorConfig.isValidate()) {

            Map<String, Object> validators = context.getBeansWithAnnotation(BulkBatchValidator.class);
            Object validatorBean = validators.get(validatorConfig.getName());

            if (validatorBean == null) {
                throw new IllegalArgumentException("No BulkBatchValidator bean found with name: " + validatorConfig.getName());
            }

            if (!(validatorBean instanceof JobParametersValidator)) {
                throw new IllegalArgumentException("Bean '" + validatorConfig.getName() + "' is not a JobParametersValidator");
            }

            jobBuilder.validator((JobParametersValidator) validatorBean);
            log.info("Attached JobParametersValidator '{}' to job '{}'", validatorConfig.getName(), jobConfig.getName());
        }

        // --- Attach Incrementer valid only for local profile
        if (runIdIncrementer != null) {
            log.info("RunIdIncrementer is used, incrementer: {}", runIdIncrementer);
            jobBuilder.incrementer(new DatabaseRunIdIncrementer(jobConfig, jobExplorer));
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
