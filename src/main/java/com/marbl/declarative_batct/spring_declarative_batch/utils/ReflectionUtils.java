package com.marbl.declarative_batct.spring_declarative_batch.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(String className, Class<T> expectedType) throws Exception {
        // Ensure the class implements/extends the expected type
        Class<?> clazz = Class.forName(className).asSubclass(expectedType);
        return (T) clazz.getDeclaredConstructor().newInstance();
    }
}
