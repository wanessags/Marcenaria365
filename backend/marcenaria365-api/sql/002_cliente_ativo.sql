-- Alteração aditiva: preserva clientes, histórico e vínculos.
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
