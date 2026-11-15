package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.service.RecuperacaoSenhaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class RecuperacaoSenhaController {
    private final RecuperacaoSenhaService service;
    private final Map<String, List<Instant>> tentativas = new HashMap<>();

    public RecuperacaoSenhaController(RecuperacaoSenhaService service) { this.service = service; }

    public record Solicitacao(@NotBlank @Email @Size(max = 160) String email) {}
    public record Redefinicao(@NotBlank @Pattern(regexp = "[a-fA-F0-9]{64}") String codigo,
                             @NotBlank @Size(min = 8, max = 72) String senha) {}

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> dadosInvalidos() {
        // Evita que o handler padrão registre valores rejeitados de código/senha.
        return ResponseEntity.badRequest().body(Map.of("mensagem", "Confira os dados da recuperação."));
    }

    @PostMapping("/recuperacao")
    public ResponseEntity<?> solicitar(@Valid @RequestBody Solicitacao dados, HttpServletRequest request) {
        limitar("pedido:" + request.getRemoteAddr(), 5);
        service.verificarConfiguracao();
        service.solicitar(dados.email());
        return ResponseEntity.accepted().body(Map.of("mensagem",
                "Solicitação recebida. Se o e-mail estiver cadastrado, o envio do código será tentado."
                + " O envio depende do serviço de e-mail e pode levar alguns minutos."
                + " Se o código não chegar, verifique a pasta de spam e tente novamente mais tarde."));
    }

    @PostMapping("/redefinicao")
    public ResponseEntity<?> redefinir(@Valid @RequestBody Redefinicao dados, HttpServletRequest request) {
        limitar("confirmacao:" + request.getRemoteAddr(), 10);
        service.redefinir(dados.codigo(), dados.senha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha atualizada. Entre com sua nova senha."));
    }

    private synchronized void limitar(String chave, int limite) {
        var agora = Instant.now();
        tentativas.values().forEach(lista -> lista.removeIf(t -> t.isBefore(agora.minusSeconds(60))));
        tentativas.entrySet().removeIf(e -> e.getValue().isEmpty());
        if (tentativas.size() >= 10000 && !tentativas.containsKey(chave)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
        var lista = tentativas.computeIfAbsent(chave, k -> new ArrayList<>());
        if (lista.size() >= limite) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        lista.add(agora);
    }
}
