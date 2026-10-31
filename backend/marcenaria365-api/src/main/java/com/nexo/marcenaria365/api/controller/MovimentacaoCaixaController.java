package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.MovimentacaoCaixa;
import com.nexo.marcenaria365.api.repository.MovimentacaoCaixaRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/caixa")
public class MovimentacaoCaixaController {

    private static final Set<String> TIPOS =
        Set.of("ENTRADA", "SAIDA");

    private final MovimentacaoCaixaRepository repository;

    public MovimentacaoCaixaController(
        MovimentacaoCaixaRepository repository
    ) {
        this.repository = repository;
    }

    public record MovimentacaoRequest(
        @NotBlank String tipo,

        @NotBlank
        @Size(max = 80)
        String categoria,

        @NotBlank
        @Size(max = 200)
        String descricao,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal valor,

        LocalDate dataMovimentacao
    ) {}

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    @GetMapping
    public List<MovimentacaoCaixa> listar(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
            .findByUsuarioIdOrderByDataMovimentacaoDescIdDesc(
                usuarioId(jwt)
            );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoCaixa> buscar(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
            .findByIdAndUsuarioId(id, usuarioId(jwt))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
        @Valid @RequestBody MovimentacaoRequest dados,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String tipo = dados.tipo().trim().toUpperCase();

        if (!TIPOS.contains(tipo)) {
            return ResponseEntity.badRequest().body(
                Map.of("erro", "Tipo deve ser ENTRADA ou SAIDA.")
            );
        }

        MovimentacaoCaixa nova = new MovimentacaoCaixa();

        nova.setTipo(tipo);
        nova.setCategoria(dados.categoria().trim());
        nova.setDescricao(dados.descricao().trim());
        nova.setValor(dados.valor());

        nova.setDataMovimentacao(
            dados.dataMovimentacao() == null
                ? LocalDate.now()
                : dados.dataMovimentacao()
        );

        nova.setUsuarioId(usuarioId(jwt));

        // Lançamentos avulsos não são vinculados a recebimentos.
        // Isso evita associar indevidamente um pagamento.
        nova.setRecebimentoId(null);

        MovimentacaoCaixa salva = repository.saveAndFlush(nova);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(salva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var encontrada = repository
            .findByIdAndUsuarioId(id, usuarioId(jwt));

        if (encontrada.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MovimentacaoCaixa movimentacao = encontrada.get();

        if (movimentacao.getRecebimentoId() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "erro",
                    "Movimentacao vinculada a recebimento nao pode ser excluida por esta rota."
                ));
        }

        repository.delete(movimentacao);
        repository.flush();

        return ResponseEntity.noContent().build();
    }
}