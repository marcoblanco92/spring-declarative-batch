package com.marbl.declarative_batct.spring_declarative_batch.steplet;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.StepFactory;
import com.marbl.declarative_batct.spring_declarative_batch.model.dummy.Customer;
import com.marbl.declarative_batct.spring_declarative_batch.model.dummy.User;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
@BulkBatchSteplet(name = "userSteplet")
public class UserSteplet extends AbstractSteplet<User, Customer> implements StepExecutionListener {

    public UserSteplet(StepFactory stepFactory) {
        super(stepFactory);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("STEP BEFORE STEP");
    }
}
