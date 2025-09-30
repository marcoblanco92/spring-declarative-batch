package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiFunction;

@Component
public class ProcessorFactory extends AbstractComponentFactory<ItemProcessor<?, ?>> {

    private static final Map<String, Class<?>> PROCESSOR_TYPES = Map.of(
            "PassThroughItemProcessor", PassThroughItemProcessor.class,
            "ItemProcessor", ItemProcessor.class
    );

    private static final Map<String, BiFunction<ComponentConfig, ApplicationContext, ItemProcessor<?, ?>>> BUILDER_MAP = Map.of(
            "PassThroughItemProcessor", (config, ctx) -> item -> item
    );

    public ProcessorFactory(ApplicationContext context) {
        super(context);
    }

    public ItemProcessor<?, ?> createProcessor(ComponentConfig config) throws Exception {
        var processor = createFromBean(config.getName(), config.getType(), PROCESSOR_TYPES);
        if (processor != null) return processor;

        return createFromBuilder(config.getType(), BUILDER_MAP, config);

    }
}
