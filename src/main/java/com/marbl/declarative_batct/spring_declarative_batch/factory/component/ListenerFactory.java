package com.marbl.declarative_batct.spring_declarative_batch.factory.component;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchListener;
import com.marbl.declarative_batct.spring_declarative_batch.exception.InvalidBeanException;
import com.marbl.declarative_batct.spring_declarative_batch.exception.TypeNotSupportedException;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

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
            throw new TypeNotSupportedException("Listener type must be provided");
        }

        if (!StringUtils.hasText(config.getName())) {
            return null; // no explicit bean
        }

        // --- Find all beans annotated with @BulkBatchJobListener ---
        Map<String, Object> beans = context.getBeansWithAnnotation(BulkBatchListener.class);

        Object targetBean = beans.values().stream()
                .filter(bean -> {
                    BulkBatchListener ann = bean.getClass().getAnnotation(BulkBatchListener.class);
                    return ann != null && ann.name().equals(config.getName());
                })
                .findFirst()
                .orElseThrow(() -> new InvalidBeanException(
                        "No Job listener bean found with annotation name: " + config.getName()
                ));

        if (!(targetBean instanceof JobExecutionListener listener)) {
            throw new InvalidBeanException(
                    "Bean '" + config.getName() + "' annotated with @BulkBatchJobListener does not implement JobExecutionListener"
            );
        }

        log.debug("Using JobExecutionListener bean '{}' as {}", config.getName(), config.getType());
        return listener;
    }


    /**
     * Create a Step listener from config or Spring context.
     * Supports StepExecutionListener, ItemReadListener<I>, ItemProcessListener<I,O>, ItemWriteListener<O>.
     */
    public StepListener createStepListener(ListenerConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new TypeNotSupportedException("Listener type must be provided");
        }

        if (!StringUtils.hasText(config.getName())) {
            return null; // no explicit bean
        }

        // --- Find all beans annotated with @BulkBatchListener ---
        Map<String, Object> beans = context.getBeansWithAnnotation(BulkBatchListener.class);

        Object targetBean = beans.values().stream()
                .filter(bean -> {
                    BulkBatchListener ann = bean.getClass().getAnnotation(BulkBatchListener.class);
                    return ann != null && ann.name().equals(config.getName());
                })
                .findFirst()
                .orElseThrow(() -> new InvalidBeanException(
                        "No Step listener bean found with annotation name: " + config.getName()
                ));


        // --- Validate type ---
        var expectedType = resolveStepListenerClass(config.getType());
        if (!expectedType.isInstance(targetBean)) {
            throw new InvalidBeanException(
                    "Bean '" + config.getName() + "' annotated with @BulkBatchListener does not implement " + config.getType()
            );
        }

        log.debug("Using Step listener bean '{}' as {}", config.getName(), config.getType());
        return (StepListener) targetBean;
    }


    // --- Helper method to retrieve Spring bean with validation ---
    private Object getBean(String beanName) {
        if (!context.containsBean(beanName)) {
            throw new InvalidBeanException("No bean found with name '" + beanName + "'");
        }
        return context.getBean(beanName);
    }

}
