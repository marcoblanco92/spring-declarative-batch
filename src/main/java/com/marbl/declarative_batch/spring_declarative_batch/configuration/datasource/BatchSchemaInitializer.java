package com.marbl.declarative_batch.spring_declarative_batch.configuration.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class BatchSchemaInitializer {

    private final DataSource dataSource;
    private final BatchDatasourceConfig batchDatasourceConfig;

    @Bean
    public DataSourceInitializer batchDataSourceInitializer() {
        BatchProperties.Jdbc jdbc = batchDatasourceConfig.getBatchProperties().getJdbc();

        // Inizializza solo se richiesto
        if (jdbc.getInitializeSchema() != DatabaseInitializationMode.ALWAYS) {
            return null;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        // Determina lo script corretto in base alla piattaforma
        String schemaLocation = getSchemaPathForPlatform(jdbc.getPlatform());
        populator.addScript(new ClassPathResource(schemaLocation));

        // Configura l'inizializzatore
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    /**
     * Restituisce il path dello script SQL da usare per la piattaforma.
     * Qui puntiamo direttamente allo script incluso in spring-batch-core.
     */
    private String getSchemaPathForPlatform(String platform) {
        if ("postgres".equalsIgnoreCase(platform) || "postgresql".equalsIgnoreCase(platform)) {
            return "org/springframework/batch/core/schema-postgresql.sql";
        } else if ("mysql".equalsIgnoreCase(platform)) {
            return "org/springframework/batch/core/schema-mysql.sql";
        } else if ("h2".equalsIgnoreCase(platform)) {
            return "org/springframework/batch/core/schema-h2.sql";
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }
}
