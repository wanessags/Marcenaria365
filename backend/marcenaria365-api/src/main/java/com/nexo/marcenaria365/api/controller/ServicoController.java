package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.Servico;
import com.nexo.marcenaria365.api.repository.ClienteRepository;
import com.nexo.marcenaria365.api.repository.ServicoRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoRepository servicos;
    private final ClienteRepository clientes;

    public ServicoController(
        ServicoRepository servicos,
        ClienteRepository clientes
    ) {
        this.servicos = servicos;
        this.clientes = clientes;
    }

    public record ServicoRequest(
        @NotNull Long clienteId,
        @NotBlank @Size(max = 160) String titulo,
        String descricao,
        @Size(max = 25) String status,
        LocalDate dataInicio,
        LocalDate prazo,
        LocalDate dataConclusao,
        @DecimalMin("0.00") BigDecimal custoPrevisto,
        @DecimalMin("0.00") BigDecimal valorCobrado,
        String observacoes
    ) {}

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private boolean clientePermitido(Long clienteId, Long usuarioId) {
        return clienteId != null &&
            clientes.findByIdAndUsuarioId(clienteId, usuarioId).isPresent();
    }

    private void preencher(Servico servico, ServicoRequest dados) {
        servico.setClienteId(dados.clienteId());
        servico.setTitulo(dados.titulo().trim());
        servico.setDescricao(dados.descricao());

        servico.setStatus(
            dados.status() == null || dados.status().isBlank()
                ? "AGUARDANDO"
                : dados.status().trim()
        );

        servico.setDataInicio(dados.dataInicio());
        servico.setPrazo(dados.prazo());
        servico.setDataConclusao(dados.dataConclusao());

        servico.setCustoPrevisto(
            dados.custoPrevisto() == null
                ? BigDecimal.ZERO
                : dados.custoPrevisto()
        );

        servico.setValorCobrado(
            dados.valorCobrado() == null
                ? BigDecimal.ZERO
                : dados.valorCobrado()
        );

        servico.setObservacoes(dados.observacoes());
    }

    @GetMapping
    public List<Servico> listar(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return servicos.listarDoUsuario(usuarioId(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servico> buscar(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return servicos.buscarDoUsuario(id, usuarioId(jwt))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
        @Valid @RequestBody ServicoRequest dados,
        @AuthenticationPrincipal Jwt jwt
    ) {
        if (!clientePermitido(dados.clienteId(), usuarioId(jwt))) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Cliente nao encontrado nesta conta."));
        }

        Servico servico = new Servico();
        preencher(servico, dados);

        Servico salvo = servicos.saveAndFlush(servico);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
        @PathVariable Long id,
        @Valid @RequestBody ServicoRequest dados,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var encontrado = servicos.buscarDoUsuario(id, usuarioId(jwt));

        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!clientePermitido(dados.clienteId(), usuarioId(jwt))) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Cliente nao encontrado nesta conta."));
        }

        Servico servico = encontrado.get();
        preencher(servico, dados);

        return ResponseEntity.ok(servicos.saveAndFlush(servico));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var encontrado = servicos.buscarDoUsuario(id, usuarioId(jwt));

        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            servicos.delete(encontrado.get());
            servicos.flush();

            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "erro",
                    "O servico possui registros vinculados e nao pode ser excluido."
                ));
        }
    }
}