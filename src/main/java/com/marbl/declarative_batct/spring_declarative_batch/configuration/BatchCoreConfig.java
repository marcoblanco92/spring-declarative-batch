package com.marbl.declarative_batct.spring_declarative_batch.configuration;

import com.marbl.declarative_batct.spring_declarative_batch.configuration.datasource.BatchDatasourceConfig;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.datasource.DataSourceConfig;
import com.marbl.declarative_batct.spring_declarative_batch.factory.datasource.DataSourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BatchDatasourceConfig.class)
public class BatchCoreConfig {


    private final DataSourceFactory dataSourceFactory;
    private final BatchDatasourceConfig batchDatasourceConfig;

    @Bean
    public Map<String, DataSource> dataSources() {
        Map<String, DataSource> map = new HashMap<>();
        batchDatasourceConfig.getDatasources().forEach((name, cfg) -> {
            DataSource ds = dataSourceFactory.create(name, cfg);
            map.put(name, ds);
            log.info("Registered datasource [{}] as type [{}]", name, cfg.getType());
        });
        return map;
    }

    @Bean
    @Primary
    @BatchDataSource
    public DataSource mainDataSource(Map<String, DataSource> dataSources) {
        return batchDatasourceConfig.getDatasources().entrySet().stream()
                .filter(e -> e.getValue().isMain())
                .findFirst()
                .map(e -> dataSources.get(e.getKey()))
                .orElseThrow(() -> new IllegalStateException("No main datasource defined in configuration"));
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public JobRepository jobRepositoryFactoryBean(DataSource dataSource, BatchDatasourceConfig batchDatasourceConfig, PlatformTransactionManager platformTransactionManager) throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactory = new JobRepositoryFactoryBean();
        jobRepositoryFactory.setDataSource(dataSource);
        jobRepositoryFactory.setTransactionManager(platformTransactionManager);
        jobRepositoryFactory.setTablePrefix(batchDatasourceConfig.getBatchProperties().getJdbc().getTablePrefix());
        String isolationLevel = batchDatasourceConfig.getDatasources()
                .values()
                .stream()
                .filter(DataSourceConfig::isMain)
                .map(dataSourceConfig -> dataSourceConfig.getIsolationLevelEnum().name())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No main datasource defined in 'bulk.datasources' configuration."));
        String databaseType = batchDatasourceConfig.getDatasources()
                .values()
                .stream()
                .filter(DataSourceConfig::isMain)
                .map(DataSourceConfig::getType)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No main datasource defined in 'bulk.datasources' configuration."));

        log.info("Setting JobRepository isolation level to {}", isolationLevel);
        jobRepositoryFactory.setIsolationLevelForCreate(isolationLevel);
        jobRepositoryFactory.setDatabaseType(databaseType);
        jobRepositoryFactory.afterPropertiesSet();

        return jobRepositoryFactory.getObject();
    }

    @Bean
    public JobExplorer jobExplorerFactoryBean(DataSource dataSource, BatchDatasourceConfig batchDatasourceConfig) throws Exception {
        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(dataSource);
        jobExplorerFactoryBean.setTablePrefix(batchDatasourceConfig.getBatchProperties().getJdbc().getTablePrefix());
        jobExplorerFactoryBean.afterPropertiesSet();

        return jobExplorerFactoryBean.getObject();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SyncTaskExecutor());
        launcher.afterPropertiesSet();
        return launcher;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean
    public JobOperator jobOperator(JobExplorer jobExplorer, JobLauncher jobLauncher, JobRegistry jobRegistry, JobRepository jobRepository) throws Exception {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobRepository(jobRepository);
        jobOperator.afterPropertiesSet();

        return jobOperator;
    }

}
