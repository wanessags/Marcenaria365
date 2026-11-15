package com.nexo.marcenaria365.api;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica as configurações de conexão usadas em produção sem acessar o banco real.
 * Os valores esperados respeitam eventuais sobrescritas por variáveis de ambiente.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:conexao;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ConfiguracaoConexaoTest {

    @DynamicPropertySource
    static void chaveDeTeste(DynamicPropertyRegistry properties) {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        properties.add("app.jwt.secret",
            () -> java.util.Base64.getEncoder().encodeToString(bytes));
    }

    @Autowired DataSource dataSource;
    @Autowired EntityManagerFactory entityManagerFactory;

    private static int esperado(String variavel, int padrao) {
        String valor = System.getenv(variavel);
        return valor == null || valor.isBlank() ? padrao : Integer.parseInt(valor.trim());
    }

    @Test void poolDeConexoesTemLimiteCompativelComRenderESupabase() throws Exception {
        HikariDataSource hikari = dataSource.unwrap(HikariDataSource.class);

        assertEquals(esperado("DB_POOL_MAX", 5), hikari.getMaximumPoolSize());
        assertEquals(esperado("DB_POOL_MIN_IDLE", 1), hikari.getMinimumIdle());
        assertEquals(esperado("DB_CONNECTION_TIMEOUT_MS", 15000), hikari.getConnectionTimeout());
        assertTrue(hikari.getMaximumPoolSize() <= 10,
            "O pool não deve voltar ao padrão de 10 conexões ociosas.");
    }

    @Test void consultaLentaTemTempoLimite() {
        Object timeout = entityManagerFactory.getProperties()
            .get("jakarta.persistence.query.timeout");

        assertEquals(
            String.valueOf(esperado("DB_QUERY_TIMEOUT_MS", 20000)),
            String.valueOf(timeout)
        );
    }
}
