package com.marbl.declarative_batct.spring_declarative_batch.configuration.batch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComponentConfig {

     @NotBlank(message = "Name is required")
     private String name;
     @NotBlank(message = "Type is required")
     private String type;
     @Valid
     private AdditionalConfig config;

}
