package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orcamentos")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "servico_id")
    private Long servicoId;

    @Column(nullable = false, length = 20)
    private String status = "RASCUNHO";

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao = LocalDate.now();

    private LocalDate validade;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "percentual_entrada", nullable = false)
    private Integer percentualEntrada = 50;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public Long getId() { return id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long valor) { clienteId = valor; }

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long valor) { servicoId = valor; }

    public String getStatus() { return status; }
    public void setStatus(String valor) { status = valor; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate valor) { dataEmissao = valor; }

    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate valor) { validade = valor; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String valor) { observacoes = valor; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal valor) { desconto = valor; }

    public Integer getPercentualEntrada() { return percentualEntrada; }
    public void setPercentualEntrada(Integer valor) { percentualEntrada = valor; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
}