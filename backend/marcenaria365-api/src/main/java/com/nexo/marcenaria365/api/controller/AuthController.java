package com.nexo.marcenaria365.api.controller;

import com.nexo.marcenaria365.api.entity.Usuario;
import com.nexo.marcenaria365.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthController(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public record CadastroRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8) String senha
    ) {}

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String senha
    ) {}

    public record UsuarioResponse(
            Long id,
            String nome,
            String email
    ) {}

    public record LoginResponse(
            String token,
            String tipo,
            long expiraEmSegundos,
            UsuarioResponse usuario
    ) {}

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody CadastroRequest dados
    ) {
        String email = dados.email().trim()
                .toLowerCase(Locale.ROOT);

        if (usuarios.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "E-mail já cadastrado."));
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(
                passwordEncoder.encode(dados.senha())
        );

        Usuario salvo = usuarios.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioResponse(
                        salvo.getId(),
                        salvo.getNome(),
                        salvo.getEmail()
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest dados
    ) {
        return usuarios.findByEmailIgnoreCase(
                        dados.email().trim()
                )
                .filter(usuario -> passwordEncoder.matches(
                        dados.senha(),
                        usuario.getSenhaHash()
                ))
                .<ResponseEntity<?>>map(usuario -> {
                    Instant agora = Instant.now();
                    long duracao = 3600;

                    JwtClaimsSet claims = JwtClaimsSet.builder()
                            .issuer("marcenaria365-api")
                            .subject(usuario.getId().toString())
                            .issuedAt(agora)
                            .expiresAt(
                                    agora.plus(
                                            1, ChronoUnit.HOURS
                                    )
                            )
                            .claim("email", usuario.getEmail())
                            .build();

                    JwsHeader header = JwsHeader
                            .with(MacAlgorithm.HS256)
                            .build();

                    String token = jwtEncoder.encode(
                            JwtEncoderParameters.from(
                                    header, claims
                            )
                    ).getTokenValue();

                    return ResponseEntity.ok(
                            new LoginResponse(
                                    token,
                                    "Bearer",
                                    duracao,
                                    new UsuarioResponse(
                                            usuario.getId(),
                                            usuario.getNome(),
                                            usuario.getEmail()
                                    )
                            )
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.status(
                                HttpStatus.UNAUTHORIZED
                        ).body(Map.of(
                                "erro",
                                "E-mail ou senha inválidos."
                        ))
                );
    }
}