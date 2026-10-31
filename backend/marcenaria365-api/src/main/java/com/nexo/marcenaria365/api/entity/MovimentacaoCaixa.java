package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimentacoes_caixa")
public class MovimentacaoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "recebimento_id", unique = true)
    private Long recebimentoId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    public Long getId() { return id; }

    public String getTipo() { return tipo; }
    public void setTipo(String valor) { tipo = valor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String valor) { categoria = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String valor) { descricao = valor; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataMovimentacao() {
        return dataMovimentacao;
    }
    public void setDataMovimentacao(LocalDate valor) {
        dataMovimentacao = valor;
    }

    public Long getRecebimentoId() { return recebimentoId; }
    public void setRecebimentoId(Long valor) {
        recebimentoId = valor;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long valor) { usuarioId = valor; }
}