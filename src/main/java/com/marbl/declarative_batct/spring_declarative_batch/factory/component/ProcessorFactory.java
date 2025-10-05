package com.marbl.declarative_batct.spring_declarative_batch.factory.component;

import com.marbl.declarative_batct.spring_declarative_batch.exception.TypeNotSupportedException;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.ComponentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
public class ProcessorFactory {

    private final ApplicationContext context;

    private static final Map<String, Class<?>> PROCESSOR_TYPES = Map.of(
            "PassThroughItemProcessor", PassThroughItemProcessor.class,
            "ItemProcessor", ItemProcessor.class
    );

    public ProcessorFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Create a processor of type <I,O> based on config or Spring context.
     */
    @SuppressWarnings("unchecked")
    public <I,O> ItemProcessor<I,O> createProcessor(ComponentConfig config) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Processor type must be provided");
        }

        ItemProcessor<I,O> processor = switch(config.getType()) {
            case "PassThroughItemProcessor" -> (ItemProcessor<I,O>) new PassThroughItemProcessor<I>();
            // case "CustomProcessor" -> (ItemProcessor<I,O>) new CustomProcessor<I,O>();
            default -> throw new TypeNotSupportedException("Unknown processor type: " + config.getType());
        };

        log.debug("Created new processor of type '{}'", config.getType());
        return processor;
    }

    public boolean isAllowedProcessor(Object bean, String type) {
        Class<?> expected = PROCESSOR_TYPES.get(type);
        if (expected == null) throw new TypeNotSupportedException("Unknown processor type: " + type);
        return expected.isInstance(bean);
    }
}
