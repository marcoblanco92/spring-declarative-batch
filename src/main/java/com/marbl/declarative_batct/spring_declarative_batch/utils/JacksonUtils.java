package com.marbl.declarative_batct.spring_declarative_batch.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * Utility class providing preconfigured ObjectMappers for YAML and JSON formats.
 * Designed to behave like Spring Boot's YAML mapper but without requiring Spring context.
 */
public final class JacksonUtils {

    private static final ObjectMapper YAML_MAPPER = buildYamlMapper();

    private JacksonUtils() {
        // prevent instantiation
    }

    /**
     * Returns a shared, thread-safe YAML ObjectMapper configured like Spring Boot's.
     */
    public static ObjectMapper yamlMapper() {
        return YAML_MAPPER;
    }

    /**
     * Returns a new JSON ObjectMapper (optional, for completeness).
     */
    public static ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    private static ObjectMapper buildYamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.registerModule(new JavaTimeModule()); // Support for LocalDate, etc.
        return mapper;
    }
}
