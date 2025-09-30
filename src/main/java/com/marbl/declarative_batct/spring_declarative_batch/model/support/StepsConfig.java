package com.marbl.declarative_batct.spring_declarative_batch.model.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
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


    private String next;
    @Valid
    private List<StepTransitionConfig> transitions;

    // --- Validation for next/transitions ---
    @AssertTrue(message = "You cannot set both 'next' and 'transitions' at the same time")
    public boolean isValidTransitions() {
        return !(next != null && transitions != null && !transitions.isEmpty());
    }

    // --- Processor listener validation ---
    @AssertTrue(message = "Processor listener name must match processor bean name when processor implements ItemProcessListener")
    private boolean isProcessorListenerValid() {
        if (processor == null || processor.getName() == null || processor.getName().isBlank()) {
            return true; // nothing to check
        }
        if (listeners == null || listeners.isEmpty()) {
            return true; // no listener declared
        }

        for (ListenerConfig listener : listeners) {
            if (listener.getName() != null && listener.getType() != null
                    && listener.getType().equalsIgnoreCase("ItemProcessListener")) {
                if (!listener.getName().equals(processor.getName())) {
                    return false; // mismatch
                }
            }
        }
        return true;
    }

    // --- Reader listener validation ---
    @AssertTrue(message = "Reader listener name must match reader bean name when reader implements ItemReadListener")
    private boolean isReaderListenerValid() {
        if (reader == null || reader.getName() == null || reader.getName().isBlank()) {
            return true;
        }
        if (listeners == null || listeners.isEmpty()) {
            return true;
        }

        for (ListenerConfig listener : listeners) {
            if (listener.getName() != null && listener.getType() != null
                    && listener.getType().equalsIgnoreCase("ItemReadListener") && !listener.getName().equals(reader.getName())) {
                    return false;
                }

        }
        return true;
    }

    // --- Writer listener validation ---
    @AssertTrue(message = "Writer listener name must match writer bean name when writer implements ItemWriteListener")
    private boolean isWriterListenerValid() {
        if (writer == null || writer.getName() == null || writer.getName().isBlank()) {
            return true;
        }
        if (listeners == null || listeners.isEmpty()) {
            return true;
        }

        for (ListenerConfig listener : listeners) {
            if (listener.getName() != null && listener.getType() != null
                    && listener.getType().equalsIgnoreCase("ItemWriteListener")) {
                if (!listener.getName().equals(writer.getName())) {
                    return false;
                }
            }
        }
        return true;
    }
}
