package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import lombok.Data;

@Data
public class StepConditionConfig {

    private String from;
    private String onCondition;
    private String toStep;
    private boolean isEnded;
}
