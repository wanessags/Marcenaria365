package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.Material;
import com.nexo.marcenaria365.api.repository.MaterialRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiais")
public class MaterialController {

    private final MaterialRepository repository;

    public MaterialController(MaterialRepository repository) {
        this.repository = repository;
    }

    public record MaterialRequest(
            @NotBlank
            @Size(max = 120)
            String nome,

            @Size(max = 60)
            String categoria,

            @NotBlank
            @Size(max = 20)
            String unidade,

            @NotNull
            @DecimalMin("0.0")
            BigDecimal quantidadeEstoque,

            @NotNull
            @DecimalMin("0.0")
            BigDecimal estoqueMinimo,

            @NotNull
            @DecimalMin("0.0")
            BigDecimal custoUnitario
    ) {}

    public record MaterialResponse(
            Long id,
            String nome,
            String categoria,
            String unidade,
            BigDecimal quantidadeEstoque,
            BigDecimal estoqueMinimo,
            BigDecimal custoUnitario
    ) {}

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private MaterialResponse resposta(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getNome(),
                material.getCategoria(),
                material.getUnidade(),
                material.getQuantidadeEstoque(),
                material.getEstoqueMinimo(),
                material.getCustoUnitario()
        );
    }

    private void preencher(
            Material material,
            MaterialRequest dados
    ) {
        material.setNome(dados.nome().trim());

        material.setCategoria(
                dados.categoria() == null ||
                dados.categoria().isBlank()
                        ? "Outros"
                        : dados.categoria().trim()
        );

        material.setUnidade(
                dados.unidade().trim().toUpperCase(
                        java.util.Locale.ROOT
                )
        );

        material.setQuantidadeEstoque(
                dados.quantidadeEstoque()
        );

        material.setEstoqueMinimo(
                dados.estoqueMinimo()
        );

        material.setCustoUnitario(
                dados.custoUnitario()
        );
    }

    @GetMapping
    public List<MaterialResponse> listar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
                .findByUsuarioIdOrderByNomeAsc(
                        usuarioId(jwt)
                )
                .stream()
                .map(this::resposta)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponse> buscar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
                .findByIdAndUsuarioId(
                        id,
                        usuarioId(jwt)
                )
                .map(material ->
                        ResponseEntity.ok(
                                resposta(material)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    public ResponseEntity<MaterialResponse> cadastrar(
            @Valid @RequestBody MaterialRequest dados,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Material material = new Material();

        preencher(material, dados);

        material.setUsuarioId(
                usuarioId(jwt)
        );

        Material salvo = repository.save(material);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resposta(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody MaterialRequest dados,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
                .findByIdAndUsuarioId(
                        id,
                        usuarioId(jwt)
                )
                .map(material -> {
                    preencher(material, dados);

                    Material salvo =
                            repository.save(material);

                    return ResponseEntity.ok(
                            resposta(salvo)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var material = repository
                .findByIdAndUsuarioId(
                        id,
                        usuarioId(jwt)
                );

        if (material.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            repository.delete(material.get());
            repository.flush();

            return ResponseEntity.noContent().build();

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Este material possui registros " +
                            "vinculados e nao pode ser excluido."
                    ));
        }
    }
}