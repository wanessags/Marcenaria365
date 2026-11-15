-- Executar uma vez no PostgreSQL antes de iniciar esta versão da API.
-- Não altera nem exclui dados de tabelas existentes.
CREATE TABLE IF NOT EXISTS recuperacoes_senha (
    usuario_id BIGINT PRIMARY KEY REFERENCES usuarios(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    solicitado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);
