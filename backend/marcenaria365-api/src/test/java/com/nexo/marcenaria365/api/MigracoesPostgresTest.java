package com.nexo.marcenaria365.api;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.nio.file.*;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Somente banco de teste local explicitamente autorizado, nunca DB_URL de produção. */
@EnabledIfEnvironmentVariable(named = "M365_TEST_PG_URL", matches = "jdbc:postgresql://127\\.0\\.0\\.1:\\d+/m365_isolated")
class MigracoesPostgresTest {
    private Connection connection;

    @BeforeEach void esquemaIsolado() throws Exception {
        connection = DriverManager.getConnection(System.getenv("M365_TEST_PG_URL"), "m365_test", "");
        String schema = "migracao_" + UUID.randomUUID().toString().replace("-", "");
        executar("CREATE SCHEMA " + schema);
        executar("SET search_path TO " + schema);
        executar("CREATE TABLE orcamentos (id BIGINT PRIMARY KEY, status VARCHAR(20) NOT NULL "
                + "CONSTRAINT orcamentos_status_check CHECK (status IN ('RASCUNHO','ENVIADO','APROVADO','RECUSADO'))) ");
        executar("CREATE TABLE orcamento_itens (id BIGINT PRIMARY KEY)");
        executar("INSERT INTO orcamentos VALUES (1, 'APROVADO')");
    }

    @AfterEach void fechar() throws Exception { if (connection != null) connection.close(); }

    private void executar(String sql) throws SQLException {
        try (var statement = connection.createStatement()) { statement.execute(sql); }
    }

    private String consultar(String sql) throws SQLException {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private void migrar() throws Exception {
        try { executar(Files.readString(Path.of("sql/003_compatibilidade_orcamentos.sql"))); }
        catch (SQLException erro) { executar("ROLLBACK"); throw erro; }
    }

    private String definicao() throws Exception {
        return consultar("SELECT pg_get_constraintdef(oid) FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='orcamentos_status_check'");
    }

    @Test void esquemaOriginalMigraPreservaDadosEPermiteReexecucao() throws Exception {
        executar("ALTER TABLE orcamentos ADD CONSTRAINT regra_id CHECK (id > 0)");
        migrar();
        assertEquals("APROVADO", consultar("SELECT status FROM orcamentos WHERE id=1"));
        assertEquals("50", consultar("SELECT percentual_entrada FROM orcamentos WHERE id=1"));
        executar("INSERT INTO orcamentos (id,status) VALUES (2,'EM_APROVACAO')");
        assertTrue(definicao().contains("EM_APROVACAO"));
        String oid = consultar("SELECT oid FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='orcamentos_status_check'");
        migrar();
        assertEquals(oid, consultar("SELECT oid FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='orcamentos_status_check'"));
        assertEquals("2", consultar("SELECT count(*) FROM orcamentos"));
        assertEquals("1", consultar("SELECT count(*) FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='regra_id'"));
    }

    @Test void mesmoNomeComRegraPersonalizadaNaoEhSubstituido() throws Exception {
        executar("ALTER TABLE orcamentos DROP CONSTRAINT orcamentos_status_check");
        executar("ALTER TABLE orcamentos ADD CONSTRAINT orcamentos_status_check CHECK (status IN ('RASCUNHO','ENVIADO','APROVADO','RECUSADO') AND id > 0)");
        String antes = definicao();
        assertThrows(SQLException.class, this::migrar);
        assertEquals(antes, definicao());
        assertEquals("0", consultar("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='orcamentos' AND column_name='percentual_entrada'"));
        assertEquals("APROVADO", consultar("SELECT status FROM orcamentos WHERE id=1"));
    }

    @Test void nomeDiferenteInterrompeSemRemoverRegra() throws Exception {
        executar("ALTER TABLE orcamentos RENAME CONSTRAINT orcamentos_status_check TO regra_personalizada");
        assertThrows(SQLException.class, this::migrar);
        assertEquals("1", consultar("SELECT count(*) FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='regra_personalizada'"));
    }

    @Test void outroCheckSobreStatusEhPreservadoEImpedeMigracao() throws Exception {
        executar("ALTER TABLE orcamentos ADD CONSTRAINT regra_extra CHECK (status <> 'RECUSADO')");
        String antes = definicao();
        assertThrows(SQLException.class, this::migrar);
        assertEquals(antes, definicao());
        assertEquals("1", consultar("SELECT count(*) FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='regra_extra'"));
    }

    @Test void colunaExistenteIncompativelInterrompeAtomicamente() throws Exception {
        executar("ALTER TABLE orcamentos ADD COLUMN percentual_entrada TEXT");
        executar("UPDATE orcamentos SET percentual_entrada='dado a preservar'");
        String antes = definicao();
        assertThrows(SQLException.class, this::migrar);
        assertEquals(antes, definicao());
        assertEquals("dado a preservar", consultar("SELECT percentual_entrada FROM orcamentos WHERE id=1"));
    }

    @Test void constraintNaoValidadaInterrompe() throws Exception {
        executar("ALTER TABLE orcamentos DROP CONSTRAINT orcamentos_status_check");
        executar("ALTER TABLE orcamentos ADD CONSTRAINT orcamentos_status_check CHECK (status IN ('RASCUNHO','ENVIADO','APROVADO','RECUSADO')) NOT VALID");
        assertThrows(SQLException.class, this::migrar);
        assertEquals("f", consultar("SELECT convalidated FROM pg_constraint WHERE conrelid='orcamentos'::regclass AND conname='orcamentos_status_check'"));
    }

    @Test void detalheIncompativelNaoEhAlterado() throws Exception {
        executar("ALTER TABLE orcamento_itens ADD COLUMN detalhe INTEGER");
        assertThrows(SQLException.class, this::migrar);
        assertEquals("integer", consultar("SELECT data_type FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='orcamento_itens' AND column_name='detalhe'"));
    }

    @Test void filaDuravelEhAditivaReexecutavelEPreservaTokens() throws Exception {
        executar("CREATE TABLE usuarios (id BIGINT PRIMARY KEY)");
        executar("INSERT INTO usuarios VALUES (1)");
        executar(Files.readString(Path.of("sql/001_recuperacao_senha.sql")));
        executar("INSERT INTO recuperacoes_senha VALUES (1, '" + "a".repeat(64) + "', now()+interval '15 minutes', now(), false)");
        String fila = Files.readString(Path.of("sql/004_fila_duravel_recuperacao.sql"));
        executar(fila);
        executar(fila);
        assertEquals("1", consultar("SELECT count(*) FROM recuperacoes_senha"));
        assertEquals("1", consultar("SELECT count(*) FROM recuperacao_fila_controle"));
        assertEquals("0", consultar("SELECT count(*) FROM pedidos_recuperacao_senha"));
    }
}
