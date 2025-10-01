package com.marbl.declarative_batct.spring_declarative_batch.factory.component;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
public abstract class AbstractComponentFactory<T> {

    protected final ApplicationContext context;

    protected AbstractComponentFactory(ApplicationContext context) {
        this.context = context;
    }

    protected T createFromBean(String name, String type, Map<String, Class<?>> typeMap) {
        if (StringUtils.hasText(name) && context.containsBean(name)) {
            var bean = context.getBean(name);
            if (!typeMap.containsKey(type)) {
                throw new InvalidComponentBeanException("Unknown type: " + type);
            }
            var expected = typeMap.get(type);
            if (!expected.isInstance(bean)) {
                throw new InvalidComponentBeanException(
                        "Bean '" + name + "' does not match expected type '" + type + "'"
                );
            }
            log.debug("Using existing bean '{}'", name);
            return (T) bean;
        }
        return null;
    }

    protected T createFromBuilder(String type, Map<String, BiFunction<ComponentConfig, ApplicationContext, T>> builderMap,
                                  ComponentConfig config) throws Exception {
        var builder = builderMap.get(type);
        if (builder == null) {
            throw new ComponentTypeNotSupportedException("Unknown component type: " + type);
        }
        return builder.apply(config, context);
    }

    // --- Exceptions ---
    public static class ComponentTypeNotSupportedException extends RuntimeException {
        public ComponentTypeNotSupportedException(String msg) {
            super(msg);
        }
    }

    public static class InvalidComponentBeanException extends RuntimeException {
        public InvalidComponentBeanException(String msg) {
            super(msg);
        }
    }
}
