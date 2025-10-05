package com.marbl.declarative_batct.spring_declarative_batch.configuration.datasource;

import lombok.Data;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Data
public class DatasourceConfig {

    private String name;
    private boolean isMain;
    private String type;
    private IsolationLevelEnum isolationLevelEnum = IsolationLevelEnum.ISOLATION_SERIALIZABLE;
    private DataSourceProperties dataSource;
    private BatchProperties batchProperties;

    enum IsolationLevelEnum {
        ISOLATION_READ_UNCOMMITTED,ISOLATION_READ_COMMITTED,ISOLATION_REPEATABLE_READ,ISOLATION_SERIALIZABLE;
    }
}
