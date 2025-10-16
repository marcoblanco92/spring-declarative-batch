package com.marbl.declarative_batct.spring_declarative_batch.poc.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeclarativeRepository {

    private final NamedParameterJdbcTemplate declarativeNamedParameterJdbcTemplate;

    public List<String> findUserEmail(long idCliente) {
        String sql = """
            SELECT email
            FROM tb_poc_cliente
            WHERE id_cliente = :idCliente
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCliente", idCliente);

        return Collections.singletonList(declarativeNamedParameterJdbcTemplate.queryForObject(sql, params, String.class));
    }
}
