package com.marbl.declarative_batct.spring_declarative_batch.poc.step.writer;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchWriter;
import com.marbl.declarative_batct.spring_declarative_batch.poc.entity.UserAuxEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RequiredArgsConstructor
@BulkBatchWriter(name = "customBatchWriter")
public class TracingItemWriterImpl implements ItemWriter<UserAuxEntity> {

    private final NamedParameterJdbcTemplate auxNamedParameterJdbcTemplate;

    @Override
    public void write(Chunk<? extends UserAuxEntity> items) throws Exception {
        String sql = """
                    INSERT INTO tb_poc_tracing (mail, count)
                    VALUES (:mail, 1)
                    ON CONFLICT (mail)
                    DO UPDATE SET count = tb_poc_tracing.count + 1
                """;

        for (UserAuxEntity user : items) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("mail", user.getEmail());

            auxNamedParameterJdbcTemplate.update(sql, params);
        }
    }
}
