package com.marbl.declarative_batct.spring_declarative_batch.poc.config;

import com.marbl.declarative_batct.spring_declarative_batch.poc.dto.ClientTransactionsDTO;
import com.marbl.declarative_batct.spring_declarative_batch.poc.entity.UserAuxEntity;
import com.marbl.declarative_batct.spring_declarative_batch.poc.step.reader.CustomClientTransactionsReader;
import com.marbl.declarative_batct.spring_declarative_batch.poc.step.writer.TracingItemWriterImpl;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class PocConfiguration {


    @Bean
    public NamedParameterJdbcTemplate declarativeNamedParameterJdbcTemplate(DataSource dataSources) {
        return new NamedParameterJdbcTemplate(dataSources);
    }

    @Bean
    public NamedParameterJdbcTemplate auxNamedParameterJdbcTemplate(Map<String, DataSource> dataSources) {
        return new NamedParameterJdbcTemplate(dataSources.get("b_aux"));
    }

    @Bean
    public FlatFileItemReader<ClientTransactionsDTO> customFileReader() {
        return new CustomClientTransactionsReader();
    }

    @Bean
    public ItemWriter<UserAuxEntity> tracingItemWriter(NamedParameterJdbcTemplate auxNamedParameterJdbcTemplate) {
        return new TracingItemWriterImpl(auxNamedParameterJdbcTemplate);
    }


}
