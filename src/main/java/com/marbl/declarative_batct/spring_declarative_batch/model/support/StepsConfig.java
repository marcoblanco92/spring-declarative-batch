package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ListenerUtils.validateListenerMatch;

@Data
@Slf4j
public class StepsConfig {

    @NotBlank(message = "Step name is required")
    private String name;
    private int chunk = 10;
    @Valid
    @NotNull(message = "Reader is required")
    private ComponentConfig reader;
    @Valid
    @NotNull(message = "Processor is required")
    private ComponentConfig processor;
    @Valid
    @NotNull(message = "Writer is required")
    private ComponentConfig writer;
    @Valid
    private List<ListenerConfig> listeners;

    private RetryConfig retry;
    private SkipConfig skip;


    private String next;
    @Valid
    private List<StepTransitionConfig> transitions;

    // --- Validation for next/transitions ---
    @AssertTrue(message = "You cannot set both 'next' and 'transitions' at the same time")
    public boolean isValidTransitions() {
        if (next != null && transitions != null && !transitions.isEmpty()) {
            log.warn("Step [{}] - Both 'next' [{}] and 'transitions' [{}] are set. Only one is allowed.",
                    name, next, transitions);
            return false;
        }
        return true;
    }


    // --- Processor listener validation ---
    @AssertTrue(message = "Processor listener name must match processor bean name when processor implements ItemProcessListener")
    private boolean isProcessorListenerValid() {
        return validateListenerMatch(name, processor, listeners, "ItemProcessListener", "Processor");
    }

    @AssertTrue(message = "Reader listener name must match reader bean name when reader implements ItemReadListener")
    private boolean isReaderListenerValid() {
        return validateListenerMatch(name, reader, listeners, "ItemReadListener", "Reader");
    }

    @AssertTrue(message = "Writer listener name must match writer bean name when writer implements ItemWriteListener")
    private boolean isWriterListenerValid() {
        return validateListenerMatch(name, writer, listeners, "ItemWriteListener", "Writer");
    }

}
