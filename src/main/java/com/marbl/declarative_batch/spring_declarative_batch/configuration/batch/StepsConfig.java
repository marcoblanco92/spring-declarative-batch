package com.marbl.declarative_batch.spring_declarative_batch.configuration.batch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.marbl.declarative_batch.spring_declarative_batch.utils.ListenerUtils.validateListenerMatch;

@Data
@Slf4j
public class StepsConfig {

    @NotEmpty(message = "The Step name must be provided and cannot be empty")
    private String name;
    private int chunk = 10;
    @Valid
    @NotNull(message = "'reader' component must be provided")
    private ComponentConfig reader;
    @Valid
    @NotNull(message = "'processor' component must be provided")
    private ComponentConfig processor;
    @Valid
    @NotNull(message = "'writer' component must be provided")
    private ComponentConfig writer;
    @Valid
    private List<ListenerConfig> listeners;

    private RetryConfig retry;
    private SkipConfig skip;
    private TransactionConfig transaction;


    private String next;
    @Valid
    private List<StepConditionConfig> transitions;

    // --- Validation for next/transitions ---
    @AssertTrue(message = "Both 'next' and 'transitions' cannot be set simultaneously")
    public boolean isValidTransitions() {
        if (next != null && transitions != null && !transitions.isEmpty()) {
            log.warn("Step [{}] - Both 'next' [{}] and 'transitions' [{}] are set. Only one is allowed.",
                    name, next, transitions);
            return false;
        }
        return true;
    }


    // --- Processor listener validation ---
    @AssertTrue(message = "Processor listener name must match processor bean name when processor implementing ItemProcessListener")
    public boolean isProcessorListenerValid() {
        return validateListenerMatch(name, processor, listeners, "ItemProcessListener", "Processor");
    }

    @AssertTrue(message = "Reader listener name must match reader bean name when reader implementing ItemReadListener")
    public boolean isReaderListenerValid() {
        return validateListenerMatch(name, reader, listeners, "ItemReadListener", "Reader");
    }

    @AssertTrue(message = "Writer listener name must match writer bean name when writer implementing ItemWriteListener")
    public boolean isWriterListenerValid() {
        return validateListenerMatch(name, writer, listeners, "ItemWriteListener", "Writer");
    }

}
