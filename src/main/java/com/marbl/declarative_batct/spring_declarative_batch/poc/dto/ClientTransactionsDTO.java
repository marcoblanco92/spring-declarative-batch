package com.marbl.declarative_batct.spring_declarative_batch.poc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientTransactionsDTO {

    private Long idCliente;
    private String nome;
    private String cognome;
    private BigDecimal importo;
    private String stato;
    private LocalDate dataCreazione;
}
