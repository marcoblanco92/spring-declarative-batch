package com.marbl.declarative_batch.spring_declarative_batch.factory.datasource;

import com.marbl.declarative_batch.spring_declarative_batch.configuration.datasource.DataSourceConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@Component
public class DataSourceFactory {

    public DataSource create(String name, DataSourceConfig cfg) {
        log.info("Creating datasource of type [{}]", cfg.getType());
        return switch (cfg.getType().toLowerCase()) {
            case "postgres" -> buildHikari(name,cfg);
            case "oracle" -> buildOracle(name,cfg);
            case "h2" -> buildH2(cfg);
            default -> throw new IllegalArgumentException("Unsupported datasource type: " + cfg.getType());
        };
    }

    private DataSource buildHikari(String name, DataSourceConfig cfg) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(cfg.getUrl());
        ds.setUsername(cfg.getUsername());
        ds.setPassword(cfg.getPassword());
        ds.setDriverClassName(cfg.getDriverClassName());
        ds.setPoolName(name);
        return ds;
    }

    private DataSource buildOracle(String name, DataSourceConfig cfg) {
        try {
            PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
            pds.setURL(cfg.getUrl());
            pds.setUser(cfg.getUsername());
            pds.setPassword(cfg.getPassword());
            pds.setConnectionFactoryClassName(cfg.getDriverClassName());
            pds.setConnectionPoolName(name);
            return pds;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create Oracle PoolDataSource", e);
        }
    }

    private DataSource buildH2(DataSourceConfig cfg) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(cfg.getUrl());
        ds.setUser(cfg.getUsername());
        ds.setPassword(cfg.getPassword());
        return ds;
    }
}
