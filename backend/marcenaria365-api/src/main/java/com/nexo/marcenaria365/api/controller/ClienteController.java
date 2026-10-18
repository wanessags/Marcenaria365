package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.Cliente;
import com.nexo.marcenaria365.api.repository.ClienteRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository repository;

    public ClienteController(ClienteRepository repository) {
        this.repository = repository;
    }

    public record ClienteRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 20) String telefone,
            @Email @Size(max = 160) String email,
            String endereco,
            String observacoes
    ) {}

    public record ClienteResponse(
            Long id,
            String nome,
            String telefone,
            String email,
            String endereco,
            String observacoes
    ) {}

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private ClienteResponse resposta(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getObservacoes()
        );
    }

    private void preencher(Cliente cliente, ClienteRequest dados) {
        cliente.setNome(dados.nome().trim());
        cliente.setTelefone(dados.telefone());
        cliente.setEmail(dados.email());
        cliente.setEndereco(dados.endereco());
        cliente.setObservacoes(dados.observacoes());
    }

    @GetMapping
    public List<ClienteResponse> listar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
                .findByUsuarioIdOrderByNomeAsc(usuarioId(jwt))
                .stream()
                .map(this::resposta)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
                .findByIdAndUsuarioId(id, usuarioId(jwt))
                .map(cliente -> ResponseEntity.ok(resposta(cliente)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(
            @Valid @RequestBody ClienteRequest dados,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Cliente cliente = new Cliente();

        preencher(cliente, dados);
        cliente.setUsuarioId(usuarioId(jwt));

        Cliente salvo = repository.save(cliente);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resposta(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest dados,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return repository
                .findByIdAndUsuarioId(id, usuarioId(jwt))
                .map(cliente -> {
                    preencher(cliente, dados);
                    Cliente salvo = repository.save(cliente);
                    return ResponseEntity.ok(resposta(salvo));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var cliente = repository.findByIdAndUsuarioId(
                id, usuarioId(jwt)
        );

        if (cliente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            repository.delete(cliente.get());
            repository.flush();

            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Este cliente possui registros vinculados e não pode ser excluído."
                    ));
        }
    }
}