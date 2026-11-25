package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.Orcamento;
import com.nexo.marcenaria365.api.entity.OrcamentoItem;
import com.nexo.marcenaria365.api.repository.ClienteRepository;
import com.nexo.marcenaria365.api.repository.ServicoRepository;
import com.nexo.marcenaria365.api.repository.OrcamentoRepository;
import com.nexo.marcenaria365.api.repository.OrcamentoItemRepository;

import jakarta.transaction.Transactional;
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
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final OrcamentoRepository orcamentos;
    private final OrcamentoItemRepository itens;
    private final ClienteRepository clientes;
    private final ServicoRepository servicos;

    private static final Set<String> STATUS_PERMITIDOS = Set.of(
        "RASCUNHO",
        "ENVIADO",
        "EM_APROVACAO",
        "APROVADO",
        "RECUSADO"
    );

    public OrcamentoController(
        OrcamentoRepository orcamentos,
        OrcamentoItemRepository itens,
        ClienteRepository clientes,
        ServicoRepository servicos
    ) {
        this.orcamentos = orcamentos;
        this.itens = itens;
        this.clientes = clientes;
        this.servicos = servicos;
    }

    public record ItemRequest(
        @NotBlank
        @Size(max = 200)
        String descricao,

        String detalhe,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal quantidade,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal valorUnitario
    ) {}

    public record OrcamentoRequest(
        @NotNull Long clienteId,

        @NotNull Long servicoId,

        String status,

        LocalDate validade,

        String observacoes,

        @DecimalMin("0.00")
        BigDecimal desconto,

        @Min(0)
        @Max(100)
        Integer percentualEntrada,

        @NotEmpty
        List<@Valid ItemRequest> itens
    ) {}

    public record OrcamentoResponse(
        Long id,
        Long clienteId,
        Long servicoId,
        String status,
        LocalDate dataEmissao,
        LocalDate validade,
        String observacoes,
        BigDecimal desconto,
        Integer percentualEntrada,
        BigDecimal total,
        List<OrcamentoItem> itens
    ) {}

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private boolean vinculoPermitido(
        Long clienteId,
        Long servicoId,
        Long usuarioId
    ) {
        if (clienteId == null || servicoId == null) {
            return false;
        }

        boolean clienteValido = clientes
            .findByIdAndUsuarioId(clienteId, usuarioId)
            .isPresent();

        boolean servicoValido = servicos
            .buscarDoUsuario(servicoId, usuarioId)
            .map(servico ->
                clienteId.equals(servico.getClienteId())
            )
            .orElse(false);

        return clienteValido && servicoValido;
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EM_APROVACAO";
        }

        return status.trim().toUpperCase();
    }

    private boolean statusValido(String status) {
        return STATUS_PERMITIDOS.contains(
            normalizarStatus(status)
        );
    }

    private void preencher(
        Orcamento orcamento,
        OrcamentoRequest dados
    ) {
        orcamento.setClienteId(dados.clienteId());
        orcamento.setServicoId(dados.servicoId());
        orcamento.setStatus(normalizarStatus(dados.status()));
        orcamento.setValidade(dados.validade());
        orcamento.setObservacoes(dados.observacoes());

        orcamento.setDesconto(
            dados.desconto() == null
                ? BigDecimal.ZERO
                : dados.desconto()
        );

        orcamento.setPercentualEntrada(
            dados.percentualEntrada() == null
                ? 50
                : dados.percentualEntrada()
        );
    }

    private OrcamentoResponse resposta(Orcamento orcamento) {
        List<OrcamentoItem> lista = itens
            .findByOrcamentoIdOrderByIdAsc(orcamento.getId());

        BigDecimal total = lista.stream()
            .map(item ->
                item.getQuantidade()
                    .multiply(item.getValorUnitario())
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .subtract(orcamento.getDesconto());

        if (total.signum() < 0) {
            total = BigDecimal.ZERO;
        }

        return new OrcamentoResponse(
            orcamento.getId(),
            orcamento.getClienteId(),
            orcamento.getServicoId(),
            orcamento.getStatus(),
            orcamento.getDataEmissao(),
            orcamento.getValidade(),
            orcamento.getObservacoes(),
            orcamento.getDesconto(),
            orcamento.getPercentualEntrada(),
            total,
            lista
        );
    }

    // Sincroniza somente o valor financeiro do servico.
    // Preserva status, recebimentos e demais informacoes.
    private void sincronizarValorServico(
        Long servicoId,
        Long usuarioId
    ) {
        if (servicoId == null) {
            return;
        }

        var servicoEncontrado = servicos
            .buscarDoUsuario(servicoId, usuarioId);

        if (servicoEncontrado.isEmpty()) {
            return;
        }

        var servico = servicoEncontrado.get();

        var orcamentoVigente = orcamentos
            .listarDoUsuario(usuarioId)
            .stream()
            .filter(orcamento ->
                servicoId.equals(orcamento.getServicoId())
            )
            .filter(orcamento ->
                servico.getClienteId().equals(
                    orcamento.getClienteId()
                )
            )
            .filter(orcamento ->
                !"RECUSADO".equals(
                    normalizarStatus(orcamento.getStatus())
                )
            )
            .findFirst();

        // Sem orcamento valido, preserva o valor atual.
        if (orcamentoVigente.isEmpty()) {
            return;
        }

        BigDecimal total = resposta(
            orcamentoVigente.get()
        ).total();

        if (
            servico.getValorCobrado() == null ||
            servico.getValorCobrado().compareTo(total) != 0
        ) {
            servico.setValorCobrado(total);
            servicos.saveAndFlush(servico);
        }
    }

    private void salvarItens(
        Long orcamentoId,
        List<ItemRequest> dados
    ) {
        for (ItemRequest item : dados) {
            OrcamentoItem novo = new OrcamentoItem();

            novo.setOrcamentoId(orcamentoId);
            novo.setDescricao(item.descricao().trim());
            novo.setDetalhe(item.detalhe());
            novo.setQuantidade(item.quantidade());
            novo.setValorUnitario(item.valorUnitario());

            itens.save(novo);
        }

        itens.flush();
    }

    @GetMapping
    @Transactional
    public List<OrcamentoResponse> listar(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return orcamentos.listarDoUsuario(usuarioId(jwt))
            .stream()
            .map(this::resposta)
            .toList();
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<OrcamentoResponse> buscar(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return orcamentos
            .buscarDoUsuario(id, usuarioId(jwt))
            .map(this::resposta)
            .map(ResponseEntity::ok)
            .orElseGet(() ->
                ResponseEntity.notFound().build()
            );
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> cadastrar(
        @Valid @RequestBody OrcamentoRequest dados,
        @AuthenticationPrincipal Jwt jwt
    ) {
        if (!vinculoPermitido(
            dados.clienteId(),
            dados.servicoId(),
            usuarioId(jwt)
        )) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "erro",
                    "Cliente e servico nao pertencem a mesma conta."
                )
            );
        }

        if (!statusValido(dados.status())) {
            return ResponseEntity.badRequest().body(
                Map.of("erro", "Status de orcamento invalido.")
            );
        }

        Orcamento novo = new Orcamento();
        preencher(novo, dados);

        Orcamento salvo = orcamentos.saveAndFlush(novo);
        salvarItens(salvo.getId(), dados.itens());

        // Atualiza o valor do servico vinculado.
        sincronizarValorServico(
            salvo.getServicoId(),
            usuarioId(jwt)
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resposta(salvo));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> editar(
        @PathVariable Long id,
        @Valid @RequestBody OrcamentoRequest dados,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var encontrado = orcamentos
            .buscarDoUsuario(id, usuarioId(jwt));

        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!vinculoPermitido(
            dados.clienteId(),
            dados.servicoId(),
            usuarioId(jwt)
        )) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "erro",
                    "Cliente e servico nao pertencem a mesma conta."
                )
            );
        }

        if (!statusValido(dados.status())) {
            return ResponseEntity.badRequest().body(
                Map.of("erro", "Status de orcamento invalido.")
            );
        }

        Orcamento orcamento = encontrado.get();

        Long servicoAnteriorId = orcamento.getServicoId();

        preencher(orcamento, dados);

        orcamentos.saveAndFlush(orcamento);

        itens.deleteByOrcamentoId(id);
        itens.flush();

        salvarItens(id, dados.itens());

        // Atualiza o valor apos editar o orcamento.
        sincronizarValorServico(
            orcamento.getServicoId(),
            usuarioId(jwt)
        );

        // Caso o vinculo tenha sido alterado,
        // verifica tambem o servico anterior.
        if (
            servicoAnteriorId != null &&
            !servicoAnteriorId.equals(
                orcamento.getServicoId()
            )
        ) {
            sincronizarValorServico(
                servicoAnteriorId,
                usuarioId(jwt)
            );
        }

        return ResponseEntity.ok(resposta(orcamento));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var encontrado = orcamentos
            .buscarDoUsuario(id, usuarioId(jwt));

        if (encontrado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Long servicoId = encontrado.get().getServicoId();

        itens.deleteByOrcamentoId(id);
        itens.flush();

        orcamentos.delete(encontrado.get());
        orcamentos.flush();

        // Verifica se existe outro orcamento valido.
        // Nao zera o valor caso nao exista.
        sincronizarValorServico(
            servicoId,
            usuarioId(jwt)
        );

        return ResponseEntity.noContent().build();
    }
}