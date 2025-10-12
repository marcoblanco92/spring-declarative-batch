package com.marbl.declarative_batct.spring_declarative_batch.poc.statement;

import com.marbl.declarative_batct.spring_declarative_batch.poc.entity.UserEntity;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class UserTransactionPreparedStatementSetter implements ItemPreparedStatementSetter<UserEntity> {

    @Override
    public void setValues(UserEntity item, PreparedStatement ps) throws SQLException {
        ps.setString(1, item.getName());
        ps.setString(2, item.getSurname());
        ps.setString(3, item.getEmail());
        ps.setString(4, item.getEmail());
        ps.setDouble(5, Double.parseDouble(item.getTransaction()));
        ps.setString(6, item.getStatus());
        ps.setDate(7, Date.valueOf(item.getCreationDate()));
    }
}
