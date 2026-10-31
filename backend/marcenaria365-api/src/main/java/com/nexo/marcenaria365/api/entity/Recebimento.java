package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recebimentos")
public class Recebimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "forma_pagamento")
    private String formaPagamento;

    @Column(nullable = false)
    private String status = "PENDENTE";

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "data_recebimento")
    private LocalDate dataRecebimento;

    @Column(columnDefinition = "text")
    private String observacoes;

    public Long getId() { return id; }

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long valor) { servicoId = valor; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String valor) {
        formaPagamento = valor;
    }

    public String getStatus() { return status; }
    public void setStatus(String valor) { status = valor; }

    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate valor) {
        dataPrevista = valor;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }
    public void setDataRecebimento(LocalDate valor) {
        dataRecebimento = valor;
    }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String valor) {
        observacoes = valor;
    }
}