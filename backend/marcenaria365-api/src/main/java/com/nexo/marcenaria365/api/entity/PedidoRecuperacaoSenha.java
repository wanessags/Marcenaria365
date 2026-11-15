package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Fila durável do fluxo de recuperação, nunca contém o código em texto puro. */
@Entity
@Table(name = "pedidos_recuperacao_senha")
public class PedidoRecuperacaoSenha {
    @Id @Column(length = 64)
    private String id;
    @Column(nullable = false, length = 160)
    private String email;
    @Column(name = "solicitado_em", nullable = false)
    private Instant solicitadoEm;
    @Column(name = "proxima_tentativa", nullable = false)
    private Instant proximaTentativa;
    @Column(nullable = false)
    private boolean pendente;
    @Column(nullable = false)
    private boolean preparado;
    @Column(nullable = false)
    private int tentativas;
    @Column(name = "codigo_cifrado", length = 255)
    private String codigoCifrado;

    protected PedidoRecuperacaoSenha() {}

    public PedidoRecuperacaoSenha(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public void solicitar(Instant agora) {
        solicitadoEm = agora;
        proximaTentativa = agora;
        pendente = true;
        preparado = false;
        tentativas = 0;
    }

    public void preparar(String cifrado) { codigoCifrado = cifrado; preparado = true; }
    public void concluir() { pendente = false; tentativas = 0; }
    public void descartarCodigo() { codigoCifrado = null; preparado = false; }
    public void reagendar(Instant agora) {
        tentativas = Math.min(tentativas + 1, 1000);
        proximaTentativa = agora.plusSeconds(Math.min(300L, 30L << Math.min(tentativas - 1, 4)));
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public Instant getSolicitadoEm() { return solicitadoEm; }
    public boolean isPendente() { return pendente; }
    public boolean isPreparado() { return preparado; }
    public String getCodigoCifrado() { return codigoCifrado; }
    public Instant getProximaTentativa() { return proximaTentativa; }
    public int getTentativas() { return tentativas; }
}
