package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.entity.Usuario;
import com.nexo.marcenaria365.api.repository.*;
import com.nexo.marcenaria365.api.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Fecha e reabre contextos completos da API; somente PostgreSQL novo e isolado. */
@EnabledIfEnvironmentVariable(named = "M365_TEST_PG_URL", matches = "jdbc:postgresql://127\\.0\\.0\\.1:\\d+/m365_isolated")
class RecuperacaoReinicioPostgresTest {
    @TestConfiguration(proxyBeanMethods = false)
    static class EmailSimulado {
        @Bean JavaMailSender emailDeTeste() { return mock(JavaMailSender.class); }
    }

    static class QuedaSimulada extends Error {}

    private void emParalelo(Runnable acao) throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var inicio = new CountDownLatch(1);
            Callable<Void> tarefa = () -> { inicio.await(); acao.run(); return null; };
            var a = executor.submit(tarefa);
            var b = executor.submit(tarefa);
            inicio.countDown();
            a.get(10, TimeUnit.SECONDS);
            b.get(10, TimeUnit.SECONDS);
        }
    }

    private ConfigurableApplicationContext iniciar(String url, String jwt, String cifra, String ddl) {
        return new SpringApplicationBuilder(Marcenaria365ApiApplication.class, EmailSimulado.class).run(
                "--server.address=127.0.0.1", "--server.port=0", "--spring.main.banner-mode=off",
                "--spring.datasource.url=" + url, "--spring.datasource.username=m365_test", "--spring.datasource.password=",
                "--spring.datasource.driver-class-name=org.postgresql.Driver", "--spring.jpa.hibernate.ddl-auto=" + ddl,
                "--app.jwt.secret=" + jwt, "--app.recovery.encryption-key=" + cifra,
                "--app.recovery.worker-enabled=false", "--spring.mail.host=smtp.example.test", "--app.recovery.from=app@example.test");
    }

    private HttpResponse<String> post(ConfigurableApplicationContext app, String endpoint, String json) throws Exception {
        int porta = Integer.parseInt(app.getEnvironment().getProperty("local.server.port"));
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + porta + endpoint))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test void pedidosSobrevivemAReiniciosFalhaSmtpEQuedaDepoisDoEnvio() throws Exception {
        String base = System.getenv("M365_TEST_PG_URL");
        String schema = "reinicio_" + UUID.randomUUID().toString().replace("-", "");
        try (var c = DriverManager.getConnection(base, "m365_test", ""); var st = c.createStatement()) {
            st.execute("CREATE SCHEMA " + schema);
        }
        String url = base + "?currentSchema=" + schema;
        String jwt = CifraCodigoRecuperacaoTest.chave();
        String cifra = CifraCodigoRecuperacaoTest.chave();
        String email = UUID.randomUUID() + "@example.test";
        String id = RecuperacaoSenhaService.hash(email);
        Long usuarioId;
        // 1. HTTP 202 somente depois da gravação; duplicatas não duplicam o pedido.
        try (var app = iniciar(url, jwt, cifra, "create")) {
            app.getBean(JdbcTemplate.class).execute(Files.readString(Path.of("sql/004_fila_duravel_recuperacao.sql")));
            var usuario = new Usuario();
            usuario.setNome("Teste isolado");
            usuario.setEmail(email);
            usuario.setSenhaHash(app.getBean(PasswordEncoder.class).encode("Antiga123!"));
            usuarioId = app.getBean(UsuarioRepository.class).saveAndFlush(usuario).getId();
            var primeira = post(app, "/api/auth/recuperacao", "{\"email\":\"" + email + "\"}");
            var duplicada = post(app, "/api/auth/recuperacao", "{\"email\":\"" + email + "\"}");
            assertEquals(202, primeira.statusCode());
            assertEquals(primeira.body(), duplicada.body());
            assertEquals(1, app.getBean(PedidoRecuperacaoSenhaRepository.class).countByPendenteTrue());
            var desconhecido = post(app, "/api/auth/recuperacao", "{\"email\":\"ausente@example.test\"}");
            assertEquals(202, desconhecido.statusCode());
            assertEquals(primeira.body(), desconhecido.body());
            verifyNoInteractions(app.getBean(JavaMailSender.class));
        }
        String hash;
        java.time.Instant vencimento;
        // 2. Novo contexto prepara e COMMITA o código, mas encerra antes do SMTP.
        try (var app = iniciar(url, jwt, cifra, "validate")) {
            var service = app.getBean(RecuperacaoSenhaService.class);
            assertEquals(id, service.prepararProximo());
            var token = app.getBean(RecuperacaoSenhaRepository.class).findById(usuarioId).orElseThrow();
            hash = token.getTokenHash();
            vencimento = token.getExpiraEm();
            assertEquals(id, service.prepararProximo());
            assertEquals(hash, app.getBean(RecuperacaoSenhaRepository.class).findById(usuarioId).orElseThrow().getTokenHash());
            verifyNoInteractions(app.getBean(JavaMailSender.class));
        }
        var primeiroEnvio = new AtomicReference<String>();
        // 3. Simular SMTP aceitando e queda antes do commit da confirmação de envio.
        try (var app = iniciar(url, jwt, cifra, "validate")) {
            doAnswer(inv -> {
                primeiroEnvio.set(((SimpleMailMessage) inv.getArgument(0)).getText().split("\n\n")[1]);
                throw new QuedaSimulada();
            }).when(app.getBean(JavaMailSender.class)).send(any(SimpleMailMessage.class));
            assertThrows(QuedaSimulada.class, () -> app.getBean(RecuperacaoSenhaService.class).enviarPreparado(id));
            assertTrue(app.getBean(PedidoRecuperacaoSenhaRepository.class).findById(id).orElseThrow().isPendente());
        }
        // 4. Falha SMTP mantém o mesmo código e agenda retentativa persistida.
        try (var app = iniciar(url, jwt, cifra, "validate")) {
            doThrow(new MailSendException("Falha simulada")).when(app.getBean(JavaMailSender.class)).send(any(SimpleMailMessage.class));
            app.getBean(RecuperacaoSenhaService.class).enviarPreparado(id);
            var pedido = app.getBean(PedidoRecuperacaoSenhaRepository.class).findById(id).orElseThrow();
            assertTrue(pedido.isPendente());
            assertEquals(1, pedido.getTentativas());
            assertTrue(pedido.getProximaTentativa().isAfter(java.time.Instant.now()));
        }
        // 5. Nova API retoma a tentativa, envia o MESMO código, sem renovar a expiração.
        try (var app = iniciar(url, jwt, cifra, "validate")) {
            var service = app.getBean(RecuperacaoSenhaService.class);
            app.getBean(JdbcTemplate.class).update("UPDATE pedidos_recuperacao_senha SET proxima_tentativa = CURRENT_TIMESTAMP WHERE id = ?", id);
            var mail = app.getBean(JavaMailSender.class);
            var mensagem = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
            emParalelo(() -> service.enviarPreparado(id));
            verify(mail).send(mensagem.capture());
            String codigo = mensagem.getValue().getText().split("\n\n")[1];
            assertEquals(primeiroEnvio.get(), codigo);
            assertEquals(hash, RecuperacaoSenhaService.hash(codigo));
            assertEquals(vencimento, app.getBean(RecuperacaoSenhaRepository.class).findById(usuarioId).orElseThrow().getExpiraEm());
            assertFalse(app.getBean(PedidoRecuperacaoSenhaRepository.class).findById(id).orElseThrow().isPendente());
            service.enviarPreparado(id);
            verify(mail, times(1)).send(any(SimpleMailMessage.class));
            var reset = post(app, "/api/auth/redefinicao", "{\"codigo\":\"" + codigo + "\",\"senha\":\"NovaSegura123!\"}");
            assertEquals(200, reset.statusCode());
            assertEquals(400, post(app, "/api/auth/redefinicao", "{\"codigo\":\"" + codigo + "\",\"senha\":\"OutraSegura123!\"}").statusCode());
            assertEquals(200, post(app, "/api/auth/login", "{\"email\":\"" + email + "\",\"senha\":\"NovaSegura123!\"}").statusCode());
            assertEquals(401, post(app, "/api/auth/login", "{\"email\":\"" + email + "\",\"senha\":\"Antiga123!\"}").statusCode());
            // Processar a conta desconhecida sem enviar e-mail.
            assertNull(service.prepararProximo());
            assertEquals(0, app.getBean(PedidoRecuperacaoSenhaRepository.class).countByPendenteTrue());
            emParalelo(() -> service.solicitar("duplicada_concorrente@example.test"));
            assertEquals(1, app.getBean(PedidoRecuperacaoSenhaRepository.class).countByPendenteTrue());
            assertNull(service.prepararProximo());
            for (int i = 0; i < 50; i++) service.solicitar("fila" + i + "@example.test");
            assertEquals(503, assertThrows(ResponseStatusException.class,
                    () -> service.solicitar("fila_cheia@example.test")).getStatusCode().value());
            assertEquals(50, app.getBean(PedidoRecuperacaoSenhaRepository.class).countByPendenteTrue());
        }
        // 6. Consumo continua persistido depois de outro reinício; não há nova autenticação automática.
        try (var app = iniciar(url, jwt, cifra, "validate")) {
            assertTrue(app.getBean(RecuperacaoSenhaRepository.class).findById(usuarioId).orElseThrow().isUsado());
            assertThrows(ResponseStatusException.class, () -> app.getBean(RecuperacaoSenhaService.class)
                    .redefinir(primeiroEnvio.get(), "OutraSegura123!"));
        }
    }
}
