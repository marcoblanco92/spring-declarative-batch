package com.marbl.declarative_batch.spring_declarative_batch.factory.component;

import com.marbl.declarative_batch.spring_declarative_batch.annotation.*;
import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.ListenerConfig;
import com.marbl.declarative_batch.spring_declarative_batch.exception.InvalidBeanException;
import com.marbl.declarative_batch.spring_declarative_batch.exception.TypeNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

import static com.marbl.declarative_batch.spring_declarative_batch.utils.ListenerUtils.resolveStepListenerClass;

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
            return null; // no explicit bean defined in YAML
        }

        Class<?> expectedType = resolveStepListenerClass(config.getType());
        Object targetBean = null;

        // --- 1 Search for beans annotated with @BulkBatchListener ---
        targetBean = findBeanByAnnotation(config.getName(), BulkBatchListener.class, expectedType);

        // --- 2 Fallback to @BulkBatchSteplet ---
        if (targetBean == null) {
            targetBean = findBeanByAnnotation(config.getName(), BulkBatchSteplet.class, expectedType);
        }

        // --- 3 Fallback to @BulkBatchReader ---
        if (targetBean == null) {
            targetBean = findBeanByAnnotation(config.getName(), BulkBatchReader.class, expectedType);
        }

        // --- 4 Fallback to @BulkBatchProcessor ---
        if (targetBean == null) {
            targetBean = findBeanByAnnotation(config.getName(), BulkBatchProcessor.class, expectedType);
        }

        // --- 5 Fallback to @BulkBatchWriter ---
        if (targetBean == null) {
            targetBean = findBeanByAnnotation(config.getName(), BulkBatchWriter.class, expectedType);
        }

        // --- 6 Throw error if nothing found ---
        if (targetBean == null) {
            throw new InvalidBeanException("No bean found implementing listener '" + config.getName() + "'");
        }

        // --- 7 Validate type safety ---
        if (!expectedType.isInstance(targetBean)) {
            throw new InvalidBeanException(
                    "Bean '" + config.getName() + "' does not implement expected type: " + config.getType()
            );
        }

        log.debug("Using Step listener bean '{}' as {}", config.getName(), config.getType());
        return (StepListener) targetBean;
    }

    private Object findBeanByAnnotation(String name, Class<? extends Annotation> annotationType, Class<?> expectedType) {
        Map<String, Object> beans = context.getBeansWithAnnotation(annotationType);

        return beans.values().stream()
                .filter(bean -> {
                    Annotation ann = bean.getClass().getAnnotation(annotationType);
                    try {
                        // all BulkBatch* annotations have a `name()` method
                        String annName = (String) annotationType.getMethod("name").invoke(ann);
                        return annName.equals(name) && expectedType.isInstance(bean);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
    }


}
