package com.marbl.declarative_batct.spring_declarative_batch.poc.mapper;

import com.marbl.declarative_batct.spring_declarative_batch.poc.dto.ClientTransactionsDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ClientTransactionsRowMapper implements RowMapper<ClientTransactionsDTO> {

    @Override
    public ClientTransactionsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClientTransactionsDTO dto = new ClientTransactionsDTO();
        dto.setIdCliente(rs.getLong("c.id_cliente"));
        dto.setNome(rs.getString("nome"));
        dto.setCognome(rs.getString("cognome"));
        dto.setImporto(rs.getString("importo"));
        dto.setStato(rs.getString("stato"));
        dto.setDataCreazione(rs.getString("t.data_creazione"));
        return dto;
    }
}
