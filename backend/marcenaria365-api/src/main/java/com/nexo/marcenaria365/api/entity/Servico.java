package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, length = 25)
    private String status = "AGUARDANDO";

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    private LocalDate prazo;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "custo_previsto", nullable = false)
    private BigDecimal custoPrevisto = BigDecimal.ZERO;

    @Column(name = "valor_cobrado", nullable = false)
    private BigDecimal valorCobrado = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public Long getId() { return id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long valor) { clienteId = valor; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String valor) { titulo = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String valor) { descricao = valor; }

    public String getStatus() { return status; }
    public void setStatus(String valor) { status = valor; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate valor) { dataInicio = valor; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate valor) { prazo = valor; }

    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate valor) { dataConclusao = valor; }

    public BigDecimal getCustoPrevisto() { return custoPrevisto; }
    public void setCustoPrevisto(BigDecimal valor) { custoPrevisto = valor; }

    public BigDecimal getValorCobrado() { return valorCobrado; }
    public void setValorCobrado(BigDecimal valor) { valorCobrado = valor; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String valor) { observacoes = valor; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
}