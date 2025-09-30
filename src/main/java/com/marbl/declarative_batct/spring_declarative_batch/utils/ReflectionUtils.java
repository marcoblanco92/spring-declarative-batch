package com.marbl.declarative_batct.spring_declarative_batch.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    // Cache for already loaded classes to improve performance
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();


    /**
     * Instantiates a class given its name and checks that it implements the expected interface or extends the expected base class.
     *
     * @param className    the fully qualified name of the class to instantiate
     * @param expectedType the interface or base class the class must implement
     * @param <T>          type of the expected interface
     * @return instance of the class
     * @throws IllegalArgumentException if the class cannot be found, does not implement the expected type, or cannot be instantiated
     */
    public static <T> T instantiateClass(String className, Class<T> expectedType) {
        if (!StringUtils.hasText(className)) {
            throw new IllegalArgumentException("Class name must not be empty");
        }

        try {
            // Load class from cache or classloader
            Class<?> clazz = CLASS_CACHE.computeIfAbsent(className, name -> {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException e) {
                    log.error("Class not found: {}", name, e);
                    throw new IllegalArgumentException("Class not found: " + name, e);
                }
            });

            // Check if the class implements the expected interface
            if (!expectedType.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                        String.format("Class %s does not implement %s", className, expectedType.getName())
                );
            }

            // Instantiate the object using the default constructor
            @SuppressWarnings("unchecked")
            T instance = (T) clazz.getDeclaredConstructor().newInstance();
            log.debug("Successfully instantiated class: {}", className);
            return instance;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Error instantiating class %s", className), e
            );
        }
    }


    /**
     * Invokes a method with the given name and argument on the target object,
     * only if such method exists. Silently does nothing if the method is not found.
     *
     * @param target     the object on which to invoke the method
     * @param methodName the name of the method
     * @param args       arguments to pass to the method
     */
    public static void invokeMethodIfExists(Object target, String methodName, Object... args) {
        if (target == null || !StringUtils.hasText(methodName)) {
            return;
        }

        Class<?> clazz = target.getClass();
        Method method = findMethod(clazz, methodName, args);

        if (method != null) {
            try {
                method.setAccessible(true);
                method.invoke(target, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(
                        "Failed to invoke method '" + methodName + "' on " + clazz.getName(), e
                );
            }
        }
    }

    private static Method findMethod(Class<?> clazz, String methodName, Object... args) {
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName) && parameterTypesMatch(m.getParameterTypes(), args)) {
                return m;
            }
        }
        return null;
    }

    private static boolean parameterTypesMatch(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    public static <T> Class<? extends T> loadClass(String className, Class<T> expectedType) {
        if (!StringUtils.hasText(className)) {
            throw new IllegalArgumentException("Class name must not be empty");
        }
        try {
            Class<?> clazz = Class.forName(className);
            if (!expectedType.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                        String.format("Class %s does not implement/extend %s", className, expectedType.getName())
                );
            }
            @SuppressWarnings("unchecked")
            Class<? extends T> safeClass = (Class<? extends T>) clazz;
            return safeClass;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error loading class " + className, e);
        }
    }


}

