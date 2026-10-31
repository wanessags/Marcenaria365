package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orcamento_itens")
public class OrcamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orcamento_id", nullable = false)
    private Long orcamentoId;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(columnDefinition = "text")
    private String detalhe;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    public Long getId() { return id; }

    public Long getOrcamentoId() { return orcamentoId; }
    public void setOrcamentoId(Long valor) { orcamentoId = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String valor) { descricao = valor; }

    public String getDetalhe() { return detalhe; }
    public void setDetalhe(String valor) { detalhe = valor; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal valor) { quantidade = valor; }

    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valor) { valorUnitario = valor; }
}