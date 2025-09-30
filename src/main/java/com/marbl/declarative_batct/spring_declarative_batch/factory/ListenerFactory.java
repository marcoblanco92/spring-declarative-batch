package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ListenerConfig;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ListenerUtils.resolveStepListenerClass;


@Component
public class ListenerFactory {

    private final ApplicationContext context;

    public ListenerFactory(ApplicationContext context) {
        this.context = context;
    }

    public JobExecutionListener createJobListener(ListenerConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Listener type must be provided");
        }

        if (!StringUtils.hasText(config.getName())) {
            return null; // no explicit bean
        }

        if (!context.containsBean(config.getName())) {
            throw new IllegalArgumentException(
                    "No bean found with name '" + config.getName() + "' for type " + config.getType()
            );
        }

        Object bean = context.getBean(config.getName());

        if (!(bean instanceof JobExecutionListener)) {
            throw new IllegalArgumentException(
                    "Bean '" + config.getName() + "' does not implement JobExecutionListener"
            );
        }

        return (JobExecutionListener) bean;
    }

    public Object createStepListener(ListenerConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Listener type must be provided");
        }

        if (!StringUtils.hasText(config.getName())) {
            return null; // no explicit bean
        }

        if (!context.containsBean(config.getName())) {
            throw new IllegalArgumentException(
                    "No bean found with name '" + config.getName() + "' for type " + config.getType()
            );
        }

        Object bean = context.getBean(config.getName());
        Class<?> expectedType = resolveStepListenerClass(config.getType());

        if (!expectedType.isInstance(bean)) {
            throw new IllegalArgumentException(
                    "Bean '" + config.getName() + "' does not implement " + config.getType()
            );
        }

        return bean;
    }


}
