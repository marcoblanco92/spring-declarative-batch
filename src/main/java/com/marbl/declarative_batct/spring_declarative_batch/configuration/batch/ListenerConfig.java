package com.marbl.declarative_batct.spring_declarative_batch.configuration.batch;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ListenerConfig {

    @NotBlank(message = "Listener name is required")
    private String name;

    @NotBlank(message = "Listener type is required")
    private String type;



    /**
     * Validation:
     * - if name is null, type must be null
     * - if name is not null, type must be present
     */
    @AssertTrue(message = "If 'name' is present, 'type' must be provided; if 'name' is absent, 'type' must be null")
    public boolean isValidNameType() {
        if (name == null || name.isBlank()) {
            return type == null || type.isBlank();
        } else {
            return type != null && !type.isBlank();
        }
    }
}
