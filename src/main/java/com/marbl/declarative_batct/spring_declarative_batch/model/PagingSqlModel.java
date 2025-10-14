package com.marbl.declarative_batct.spring_declarative_batch.model;

import lombok.Data;

import java.util.List;

@Data
public class PagingSqlModel {

    private String selectClause;
    private String fromClause;
    private String whereClause;
    private String groupByClause;

    private List<SortKey> sortClause;
}
