-- Aplicar somente após backup e homologação. Nenhum dado é apagado.
-- Definições desconhecidas são recusadas, mesmo se o nome da constraint for igual.
BEGIN;
SET LOCAL lock_timeout = '5s';
LOCK TABLE orcamentos, orcamento_itens IN ACCESS EXCLUSIVE MODE;

DO $$
DECLARE
    atual RECORD;
    coluna_status SMALLINT;
    original TEXT;
    desejada TEXT;
BEGIN
    SELECT attnum INTO coluna_status FROM pg_attribute
    WHERE attrelid = 'orcamentos'::regclass AND attname = 'status' AND NOT attisdropped
      AND atttypid = 'character varying'::regtype AND atttypmod = 24 AND attnotnull;
    IF coluna_status IS NULL THEN
        RAISE EXCEPTION 'Coluna status desconhecida: esperado VARCHAR(20) NOT NULL. Nenhuma alteração aplicada.';
    END IF;

    SELECT oid, convalidated, pg_get_constraintdef(oid) AS definicao INTO atual
    FROM pg_constraint WHERE conrelid = 'orcamentos'::regclass
      AND conname = 'orcamentos_status_check' AND contype = 'c';
    IF NOT FOUND OR NOT atual.convalidated THEN
        RAISE EXCEPTION 'Constraint de status ausente ou não validada. Revisão manual necessária.';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'orcamentos'::regclass
               AND contype = 'c' AND oid <> atual.oid AND coluna_status = ANY(conkey)) THEN
        RAISE EXCEPTION 'Há outra regra personalizada para status. Revisão manual necessária; regras preservadas.';
    END IF;

    -- O próprio PostgreSQL normaliza as duas expressões conhecidas. Comparação
    -- integral, sem remover casts, espaços, condições ou trechos de regras reais.
    CREATE TEMP TABLE m365_m003_modelo (LIKE orcamentos) ON COMMIT DROP;
    ALTER TABLE m365_m003_modelo ADD CONSTRAINT m365_original
        CHECK (status IN ('RASCUNHO','ENVIADO','APROVADO','RECUSADO'));
    ALTER TABLE m365_m003_modelo ADD CONSTRAINT m365_desejada
        CHECK (status IN ('RASCUNHO','ENVIADO','EM_APROVACAO','APROVADO','RECUSADO'));
    SELECT pg_get_constraintdef(oid) INTO original FROM pg_constraint
        WHERE conrelid = 'pg_temp.m365_m003_modelo'::regclass AND conname = 'm365_original';
    SELECT pg_get_constraintdef(oid) INTO desejada FROM pg_constraint
        WHERE conrelid = 'pg_temp.m365_m003_modelo'::regclass AND conname = 'm365_desejada';
    IF atual.definicao <> original
       AND atual.definicao <> desejada
       AND atual.definicao <> 'CHECK (((status)::text = ANY (ARRAY[(''RASCUNHO''::character varying)::text, (''ENVIADO''::character varying)::text, (''APROVADO''::character varying)::text, (''RECUSADO''::character varying)::text])))' THEN
        RAISE EXCEPTION 'Definição personalizada ou incompatível da constraint de status. Nenhuma regra substituída.';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = 'orcamentos'::regclass
               AND attname = 'percentual_entrada' AND NOT attisdropped
               AND (atttypid <> 'integer'::regtype OR NOT attnotnull)) THEN
        RAISE EXCEPTION 'percentual_entrada existente incompatível: esperado INTEGER NOT NULL.';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = 'orcamento_itens'::regclass
               AND attname = 'detalhe' AND NOT attisdropped AND atttypid <> 'text'::regtype) THEN
        RAISE EXCEPTION 'detalhe existente incompatível: esperado TEXT.';
    END IF;

    ALTER TABLE orcamentos ADD COLUMN IF NOT EXISTS percentual_entrada INTEGER NOT NULL DEFAULT 50;
    ALTER TABLE orcamento_itens ADD COLUMN IF NOT EXISTS detalhe TEXT;
    IF atual.definicao <> desejada THEN
        ALTER TABLE orcamentos DROP CONSTRAINT orcamentos_status_check;
        ALTER TABLE orcamentos ADD CONSTRAINT orcamentos_status_check
            CHECK (status IN ('RASCUNHO','ENVIADO','EM_APROVACAO','APROVADO','RECUSADO'));
    END IF;
END $$;
COMMIT;
