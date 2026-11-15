-- Aplicar após 001, em homologação primeiro. Não executado automaticamente.
-- Não modifica usuários, senhas ou códigos da tabela de validação existente.
BEGIN;
CREATE TABLE IF NOT EXISTS pedidos_recuperacao_senha (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(160) NOT NULL,
    solicitado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    proxima_tentativa TIMESTAMP WITH TIME ZONE NOT NULL,
    pendente BOOLEAN NOT NULL,
    preparado BOOLEAN NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    codigo_cifrado VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS pedidos_recuperacao_pendentes_idx
    ON pedidos_recuperacao_senha (proxima_tentativa, id) WHERE pendente = TRUE;
-- Mutex de admissão: mantém o limite global de 50 pedidos pendentes sob concorrência.
CREATE TABLE IF NOT EXISTS recuperacao_fila_controle (id INTEGER PRIMARY KEY CHECK (id = 1));
INSERT INTO recuperacao_fila_controle (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
COMMIT;
