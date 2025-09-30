package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.bulk.com_marbl_bulk_v2.model.support.ComponentConfig;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProcessorFactory {

    private final ApplicationContext context;

    public ProcessorFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Create ItemProcessor based on ComponentConfig.
     * Supports Spring bean or fallback minimal processors.
     */
    @SuppressWarnings("unchecked")
    public ItemProcessor<?, ?> createProcessor(ComponentConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Processor type must be provided");
        }

        // --- 1️⃣ Use Spring bean if exists ---
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            Object bean = context.getBean(config.getName());

            if (!(bean instanceof ItemProcessor<?, ?> processor)) {
                throw new IllegalArgumentException("Bean '" + config.getName() + "' is not an ItemProcessor");
            }

            if (!isAllowedProcessor(processor, config.getType())) {
                throw new IllegalArgumentException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }

            return processor; // safe if caller ensures types
        }

        // --- 2️⃣ Fallback minimal processor ---
        return createByType(config.getType());
    }

    /**
     * Fallback processor creation by type.
     * Only PassThrough is supported as safe fallback.
     */
    private ItemProcessor<?, ?> createByType(String type) {
        switch (type) {
            case "PassThroughItemProcessor" -> {return item -> item;}
            default -> throw new IllegalArgumentException("Unknown processor type: " + type);
        }
    }


        /**
         * Bean type check
         */
        private boolean isAllowedProcessor (Object bean, String type){
            return switch (type) {
                case "PassThroughItemProcessor" -> bean instanceof PassThroughItemProcessor;
                case "ItemProcessor" -> bean instanceof ItemProcessor;
                default -> throw new IllegalArgumentException("Unknown processor type: " + type);
            };
        }
    }
