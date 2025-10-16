package com.marbl.declarative_batct.spring_declarative_batch.poc.step.processor;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchProcessor;
import com.marbl.declarative_batct.spring_declarative_batch.poc.entity.UserEntity;
import com.marbl.declarative_batct.spring_declarative_batch.poc.model.UserCsv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@BulkBatchProcessor(name = "pocProcessor")
public class PocProcessor implements ItemProcessor<UserCsv, UserEntity> {


    @Override
    public UserEntity process(UserCsv item) {
        UserEntity entity = UserEntity
                .builder()
                .name(item.getName())
                .surname(item.getSurname())
                .email(item.getEmail())
                .status(item.getStatus())
                .creationDate(item.getCreationDate())
                .transaction(String.valueOf(Double.parseDouble(item.getTransaction()) * (0.8 + Math.random() * 0.4)))
                .build();

        log.info("Created user to persist: {}", entity);

        return entity;
    }
}
