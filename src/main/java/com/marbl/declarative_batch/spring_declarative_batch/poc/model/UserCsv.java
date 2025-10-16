package com.marbl.declarative_batch.spring_declarative_batch.poc.model;

import lombok.Data;

@Data
public class UserCsv {

    private int id;
    private String name;
    private String surname;
    private String email;
    private String transaction;
    private String status;
    private String creationDate;

}
