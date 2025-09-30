package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StepTransitionConfig {

    @NotBlank(message = "On condition is required")
    private String onCondition;
    @NotBlank(message = "To step is required")
    private String toStep;
}
