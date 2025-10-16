package com.marbl.declarative_batct.spring_declarative_batch.poc.step.reader;

import com.marbl.declarative_batct.spring_declarative_batch.annotation.BulkBatchReader;
import com.marbl.declarative_batct.spring_declarative_batch.poc.dto.ClientTransactionsDTO;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ResourceUtils.resolveResource;

@BulkBatchReader(name = "customFileReader")
public class CustomClientTransactionsReader extends FlatFileItemReader<ClientTransactionsDTO> {
    public CustomClientTransactionsReader() {
        setName("customFileReader");
        setResource(resolveResource("file:src/main/resources/poc/test_poc.csv"));
        setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("idCliente", "nome", "cognome", "importo", "stato", "dataCreazione");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(ClientTransactionsDTO.class);
            }});
        }});
        setLinesToSkip(1);
    }
}
