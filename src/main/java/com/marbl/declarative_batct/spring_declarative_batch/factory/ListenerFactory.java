package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ListenerUtils.resolveStepListenerClass;

@Slf4j
@Component
public class ListenerFactory {

    private final ApplicationContext context;

    public ListenerFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Create a JobExecutionListener from config or Spring context.
     */
    public JobExecutionListener createJobListener(ListenerConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new ListenerTypeNotSupportedException("Listener type must be provided");
        }

        if (!StringUtils.hasText(config.getName())) {
            return null; // no explicit bean
        }

        var bean = getBean(config.getName());
        if (!(bean instanceof JobExecutionListener listener)) {
            throw new InvalidListenerBeanException(
                    "Bean '" + config.getName() + "' does not implement JobExecutionListener"
            );
        }

        log.debug("Using JobExecutionListener bean '{}'", config.getName());
        return listener;
    }

    /**
     * Create a Step listener from config or Spring context.
     */
    public Object createStepListener(ListenerConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new ListenerTypeNotSupportedException("Listener type must be provided");
        }

        if (!StringUtils.hasText(config.getName())) {
            return null; // no explicit bean
        }

        var bean = getBean(config.getName());
        var expectedType = resolveStepListenerClass(config.getType());

        if (!expectedType.isInstance(bean)) {
            throw new InvalidListenerBeanException(
                    "Bean '" + config.getName() + "' does not implement " + config.getType()
            );
        }

        log.debug("Using Step listener bean '{}'", config.getName());
        return bean;
    }

    // --- Helper method to retrieve Spring bean with validation ---
    private Object getBean(String beanName) {
        if (!context.containsBean(beanName)) {
            throw new InvalidListenerBeanException("No bean found with name '" + beanName + "'");
        }
        return context.getBean(beanName);
    }

    // --- Custom exceptions ---
    public static class ListenerTypeNotSupportedException extends RuntimeException {
        public ListenerTypeNotSupportedException(String msg) {
            super(msg);
        }
    }

    public static class InvalidListenerBeanException extends RuntimeException {
        public InvalidListenerBeanException(String msg) {
            super(msg);
        }
    }
}
