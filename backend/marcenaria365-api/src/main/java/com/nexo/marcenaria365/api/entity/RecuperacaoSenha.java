package com.nexo.marcenaria365.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recuperacoes_senha")
public class RecuperacaoSenha {
    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;
    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;
    @Column(name = "solicitado_em", nullable = false)
    private Instant solicitadoEm;
    @Column(nullable = false)
    private boolean usado;

    protected RecuperacaoSenha() {}

    public RecuperacaoSenha(Long usuarioId, String hash, Instant agora) {
        this.usuarioId = usuarioId;
        this.tokenHash = hash;
        this.solicitadoEm = agora;
        this.expiraEm = agora.plusSeconds(900);
    }

    public Long getUsuarioId() { return usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiraEm() { return expiraEm; }
    public Instant getSolicitadoEm() { return solicitadoEm; }
    public boolean isUsado() { return usado; }
    public void consumir() { usado = true; }
}
