package com.marbl.declarative_batct.spring_declarative_batch.steplet;

import com.marbl.declarative_batct.spring_declarative_batch.factory.step.AbstractSteplet;
import com.marbl.declarative_batct.spring_declarative_batch.factory.step.StepFactory;
import com.marbl.declarative_batct.spring_declarative_batch.model.dummy.Customer;
import com.marbl.declarative_batct.spring_declarative_batch.model.dummy.User;
import org.springframework.stereotype.Component;

@Component("userSteplet")
public class UserSteplet extends AbstractSteplet<User, Customer> {

    public UserSteplet(StepFactory stepFactory) {
        super(stepFactory);
    }

}
