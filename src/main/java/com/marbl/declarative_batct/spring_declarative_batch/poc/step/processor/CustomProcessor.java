package com.marbl.declarative_batct.spring_declarative_batch.poc.step.processor;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchProcessor;
import com.marbl.declarative_batct.spring_declarative_batch.poc.dto.ClientTransactionsDTO;
import com.marbl.declarative_batct.spring_declarative_batch.poc.entity.UserAuxEntity;
import com.marbl.declarative_batct.spring_declarative_batch.poc.repository.DeclarativeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@BulkBatchProcessor(name = "customItemProcessor")
public class CustomProcessor implements ItemProcessor<ClientTransactionsDTO, UserAuxEntity> {

    private final DeclarativeRepository declarativeRepository;

    @Override
    public UserAuxEntity process(ClientTransactionsDTO item) {
        UserAuxEntity entity = UserAuxEntity
                .builder()
                .email(declarativeRepository.findUserEmail(item.getIdCliente()).get(0))
                .build();

        log.info("Created user to persist: {}", entity);

        return entity;
    }
}
