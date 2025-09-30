package com.marbl.declarative_batct.spring_declarative_batch.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ListenerUtils {

    public static Class<?> resolveStepListenerClass(String type) {
        return switch (type) {
            case "StepExecutionListener" -> StepExecutionListener.class;
            case "ChunkListener" -> ChunkListener.class;
            case "ItemReadListener" -> ItemReadListener.class;
            case "ItemProcessListener" -> ItemProcessListener.class;
            case "ItemWriteListener" -> ItemWriteListener.class;
            case "SkipListener" -> SkipListener.class;
            default -> throw new IllegalArgumentException("Unknown listener type: " + type);
        };
    }
}
