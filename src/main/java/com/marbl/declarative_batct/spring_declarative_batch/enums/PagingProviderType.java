package com.marbl.declarative_batct.spring_declarative_batch.enums;

import com.marbl.declarative_batct.spring_declarative_batch.configuration.reader.JdbcPagingReaderConfig;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.*;

public enum PagingProviderType {

    POSTGRESQL {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            PostgresPagingQueryProvider provider = new PostgresPagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    },
    ORACLE {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            OraclePagingQueryProvider provider = new OraclePagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    },
    MYSQL {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    },
    MARIADB {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            // MariaDB usa lo stesso provider di MySQL
            MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    },
    SQLSERVER {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            SqlServerPagingQueryProvider provider = new SqlServerPagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    },
    DB2 {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            Db2PagingQueryProvider provider = new Db2PagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    },
    H2 {
        @Override
        public PagingQueryProvider create(JdbcPagingReaderConfig config) {
            H2PagingQueryProvider provider = new H2PagingQueryProvider();
            setupProvider(provider, config);
            return provider;
        }
    };

    public abstract PagingQueryProvider create(JdbcPagingReaderConfig config);

    //Helper method used to configure select/from/where and sortKeys
    protected void setupProvider(PagingQueryProvider provider, JdbcPagingReaderConfig config) {
        if (provider instanceof AbstractSqlPagingQueryProvider sqlProvider) {
            sqlProvider.setSelectClause(config.getSelectClause());
            sqlProvider.setFromClause(config.getFromClause());
            sqlProvider.setWhereClause(config.getWhereClause());
            sqlProvider.setSortKeys(config.getSortKeys());
        } else {
            throw new IllegalStateException(
                    "Provider class not supported for automatic configuration: " + provider.getClass().getName()
            );
        }
    }

}
