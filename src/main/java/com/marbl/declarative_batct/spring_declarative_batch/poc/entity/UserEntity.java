package com.marbl.declarative_batct.spring_declarative_batch.poc.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEntity {

    private String name;
    private String surname;
    private String email;
    private String transaction;
    private String status;
    private String creationDate;

}
