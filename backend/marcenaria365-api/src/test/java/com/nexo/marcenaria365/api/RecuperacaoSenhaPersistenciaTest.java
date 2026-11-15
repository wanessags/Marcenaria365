package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.entity.*;
import com.nexo.marcenaria365.api.repository.*;
import com.nexo.marcenaria365.api.service.*;
import com.nexo.marcenaria365.api.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Exclusivamente banco H2 em memória: não conecta ao PostgreSQL nem ao SMTP.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:recuperacao;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "app.recovery.worker-enabled=false",
    "spring.mail.host=smtp.example.test", "app.recovery.from=app@example.test"
})
class RecuperacaoSenhaPersistenciaTest {
    @DynamicPropertySource
    static void chaveDeTeste(DynamicPropertyRegistry properties) {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        String chave = java.util.Base64.getEncoder().encodeToString(bytes);
        properties.add("app.jwt.secret", () -> chave);
        byte[] outra = new byte[32];
        new java.security.SecureRandom().nextBytes(outra);
        properties.add("app.recovery.encryption-key", () -> java.util.Base64.getEncoder().encodeToString(outra));
    }

    @Autowired UsuarioRepository usuarios;
    @Autowired RecuperacaoSenhaRepository tokens;
    @Autowired RecuperacaoSenhaService service;
    @Autowired PasswordEncoder encoder;
    @Autowired AuthController auth;
    @Autowired PlatformTransactionManager transactions;
    @Autowired JdbcTemplate jdbc;
    @Autowired PedidoRecuperacaoSenhaRepository pedidos;
    @MockitoBean JavaMailSender mail;

    @BeforeEach void controleDaFila() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS recuperacao_fila_controle (id INTEGER PRIMARY KEY)");
        jdbc.update("MERGE INTO recuperacao_fila_controle (id) KEY (id) VALUES (1)");
    }

    @Test void falhaSmtpMantemCodigoDuravelSemAlterarSenha() {
        var usuario = new Usuario();
        usuario.setNome("Teste de rollback");
        usuario.setEmail(UUID.randomUUID() + "@example.test");
        usuario.setSenhaHash(encoder.encode("Antiga123!"));
        var salvo = usuarios.saveAndFlush(usuario);
        doThrow(new MailSendException("Falha simulada")).when(mail).send(any(SimpleMailMessage.class));
        service.solicitar(salvo.getEmail());
        var id = service.prepararProximo();
        assertNotNull(id);
        String hashPersistido = tokens.findById(salvo.getId()).orElseThrow().getTokenHash();
        service.enviarPreparado(id);
        assertEquals(hashPersistido, tokens.findById(salvo.getId()).orElseThrow().getTokenHash());
        var pedido = pedidos.findById(id).orElseThrow();
        assertTrue(pedido.isPendente());
        assertEquals(1, pedido.getTentativas());
        assertTrue(encoder.matches("Antiga123!", usuarios.findById(salvo.getId()).orElseThrow().getSenhaHash()));
    }

    @Test void consumoConcorrentePermiteSomenteUmaTrocaEPersisteSenha() throws Exception {
        var usuario = new Usuario();
        usuario.setNome("Teste isolado");
        usuario.setEmail(UUID.randomUUID() + "@example.test");
        usuario.setSenhaHash(encoder.encode("Antiga123!"));
        usuario = usuarios.saveAndFlush(usuario);
        String codigo = UUID.randomUUID().toString();
        tokens.saveAndFlush(new RecuperacaoSenha(usuario.getId(), RecuperacaoSenhaService.hash(codigo), Instant.now()));
        var inicio = new CountDownLatch(1);
        Callable<Boolean> redefinir = () -> {
            inicio.await();
            try { service.redefinir(codigo, "NovaSegura123!"); return true; }
            catch (ResponseStatusException erro) { return false; }
        };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var primeira = executor.submit(redefinir);
            var segunda = executor.submit(redefinir);
            inicio.countDown();
            assertNotEquals(primeira.get(10, TimeUnit.SECONDS), segunda.get(10, TimeUnit.SECONDS));
        }
        var persistido = usuarios.findById(usuario.getId()).orElseThrow();
        assertTrue(encoder.matches("NovaSegura123!", persistido.getSenhaHash()));
        assertFalse(encoder.matches("Antiga123!", persistido.getSenhaHash()));
        assertTrue(tokens.findById(usuario.getId()).orElseThrow().isUsado());
        assertEquals(200, auth.login(new AuthController.LoginRequest(persistido.getEmail(), "NovaSegura123!"))
                .getStatusCode().value());
        assertEquals(401, auth.login(new AuthController.LoginRequest(persistido.getEmail(), "Antiga123!"))
                .getStatusCode().value());
    }
}
