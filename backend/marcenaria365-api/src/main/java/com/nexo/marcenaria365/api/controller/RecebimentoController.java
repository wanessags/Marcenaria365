package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.Recebimento;
import com.nexo.marcenaria365.api.repository.RecebimentoRepository;
import com.nexo.marcenaria365.api.repository.ServicoRepository;

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
@RequestMapping("/api/recebimentos")
public class RecebimentoController {

    private static final Set<String> STATUS_PERMITIDOS =
        Set.of("PENDENTE", "RECEBIDO");

    private final RecebimentoRepository recebimentos;
    private final ServicoRepository servicos;

    public RecebimentoController(
        RecebimentoRepository recebimentos,
        ServicoRepository servicos
    ) {
        this.recebimentos = recebimentos;
        this.servicos = servicos;
    }

    public record RecebimentoRequest(
        @NotNull Long servicoId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal valor,

        String formaPagamento,
        String status,
        LocalDate dataPrevista,
        LocalDate dataRecebimento,
        String observacoes
    ) {}

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private boolean servicoPermitido(
        Long servicoId,
        Long usuarioId
    ) {
        return servicoId != null &&
            servicos.buscarDoUsuario(servicoId, usuarioId)
                .isPresent();
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDENTE";
        }

        return status.trim().toUpperCase();
    }

    @GetMapping
    public List<Recebimento> listar(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return recebimentos.listarDoUsuario(usuarioId(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recebimento> buscar(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return recebimentos.buscarDoUsuario(
                id, usuarioId(jwt)
            )
            .map(ResponseEntity::ok)
            .orElseGet(() ->
                ResponseEntity.notFound().build()
            );
    }

    @GetMapping("/servico/{servicoId}")
    public ResponseEntity<List<Recebimento>> listarServico(
        @PathVariable Long servicoId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        if (!servicoPermitido(servicoId, usuarioId(jwt))) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
            recebimentos.listarPorServico(
                servicoId, usuarioId(jwt)
            )
        );
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
        @Valid @RequestBody RecebimentoRequest dados,
        @AuthenticationPrincipal Jwt jwt
    ) {
        if (!servicoPermitido(
            dados.servicoId(), usuarioId(jwt)
        )) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "erro",
                    "Servico nao encontrado nesta conta."
                )
            );
        }

        String status = normalizarStatus(dados.status());

        if (!STATUS_PERMITIDOS.contains(status)) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "erro",
                    "Status de recebimento invalido."
                )
            );
        }

        if (
            status.equals("PENDENTE") &&
            dados.dataRecebimento() != null
        ) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "erro",
                    "Pagamento pendente nao pode ter data de recebimento."
                )
            );
        }

        Recebimento novo = new Recebimento();

        novo.setServicoId(dados.servicoId());
        novo.setValor(dados.valor());
        novo.setFormaPagamento(dados.formaPagamento());
        novo.setStatus(status);
        novo.setDataPrevista(dados.dataPrevista());
        novo.setObservacoes(dados.observacoes());

        if (status.equals("RECEBIDO")) {
            novo.setDataRecebimento(
                dados.dataRecebimento() != null
                    ? dados.dataRecebimento()
                    : LocalDate.now()
            );
        }

        Recebimento salvo =
            recebimentos.saveAndFlush(novo);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salvo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var encontrado = recebimentos.buscarDoUsuario(
            id, usuarioId(jwt)
        );

        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            recebimentos.delete(encontrado.get());
            recebimentos.flush();

            return ResponseEntity.noContent().build();
        } catch (org.springframework.dao.DataIntegrityViolationException erro) {
            return ResponseEntity.status(
                org.springframework.http.HttpStatus.CONFLICT
            ).body(Map.of(
                "erro",
                "O recebimento possui movimentacoes vinculadas e nao pode ser excluido."
            ));
        }
    }
}