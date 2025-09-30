package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.declarative_batct.spring_declarative_batch.exception.InvalidProcessorBeanException;
import com.marbl.declarative_batct.spring_declarative_batch.exception.ProcessorTypeNotSupportedException;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
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

    // Functional interface for dynamic builder
    @FunctionalInterface
    private interface ProcessorBuilderFn {
        ItemProcessor<?, ?> build(ComponentConfig config, ApplicationContext ctx);
    }

    // Processor types -> expected class
    private static final Map<String, Class<?>> PROCESSOR_TYPES = Map.of(
            "PassThroughItemProcessor", PassThroughItemProcessor.class,
            "ItemProcessor", ItemProcessor.class
    );

    // Processor types -> builder function
    private static final Map<String, ProcessorBuilderFn> BUILDER_MAP = Map.of(
            "PassThroughItemProcessor", (config, ctx) -> item -> item
            // add new processors here
    );

    public ProcessorFactory(ApplicationContext context) {
        this.context = context;
    }

    public ItemProcessor<?, ?> createProcessor(ComponentConfig config) {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Processor type must be provided");
        }

        // 1. Try to load processor from Spring context
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            var bean = context.getBean(config.getName());
            if (!(bean instanceof ItemProcessor<?, ?> processorBean)) {
                throw new InvalidProcessorBeanException("Bean '" + config.getName() + "' is not an ItemProcessor");
            }
            if (!isAllowedProcessor(processorBean, config.getType())) {
                throw new InvalidProcessorBeanException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }
            log.debug("Using existing processor bean '{}'", config.getName());
            return processorBean;
        }

        // 2. Build a new processor dynamically
        var builder = BUILDER_MAP.get(config.getType());
        if (builder == null) {
            throw new ProcessorTypeNotSupportedException("Unknown processor type: " + config.getType());
        }

        log.debug("Creating new processor of type '{}'", config.getType());
        return builder.build(config, context);
    }

    private boolean isAllowedProcessor(Object bean, String type) {
        var expected = PROCESSOR_TYPES.get(type);
        if (expected == null) throw new ProcessorTypeNotSupportedException("Unknown processor type: " + type);
        return expected.isInstance(bean);
    }
}
