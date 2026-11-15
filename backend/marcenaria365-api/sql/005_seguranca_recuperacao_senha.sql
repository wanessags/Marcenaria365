-- 005_seguranca_recuperacao_senha.sql
-- Proteção das tabelas de recuperação de senha. NÃO é migração de esquema.
--
-- Aplicar SOMENTE depois de:
--   sql/001_recuperacao_senha.sql
--   sql/004_fila_duravel_recuperacao.sql
-- e antes de publicar a API que usa recuperação de senha.
--
-- Execute no SQL Editor do Supabase com o papel postgres (proprietário das
-- tabelas criadas pelas migrações). Não altera as dez tabelas originais,
-- não cria políticas RLS e não modifica dados.
--
-- Acesso do backend (Spring Boot/JDBC):
--   * DB_USERNAME padrão = postgres, proprietário das tabelas. O proprietário
--     mantém todos os privilégios e ignora RLS enquanto FORCE ROW LEVEL
--     SECURITY não for ativado (este script não ativa).
--   * service_role (exclusivo de servidor, com BYPASSRLS) permanece com
--     acesso e não é revogado aqui.
--   * Se DB_USERNAME usar outro papel, ele NÃO terá acesso até receber GRANT
--     explícito e uma política RLS (ou BYPASSRLS). Veja as instruções finais.
--
-- Reaplique este arquivo se as tabelas forem recriadas: as permissões padrão
-- do Supabase voltam a conceder ALL a anon/authenticated em novas tabelas.
--
-- Idempotente. Tudo ocorre em uma transação: qualquer falha = rollback.

BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '60s';

-- 0. Pré-condições ---------------------------------------------------------
DO $$
DECLARE
    faltando text;
BEGIN
    SELECT string_agg(t, ', ' ORDER BY t) INTO faltando
    FROM unnest(ARRAY[
        'public.recuperacoes_senha',
        'public.pedidos_recuperacao_senha',
        'public.recuperacao_fila_controle'
    ]) AS t
    WHERE to_regclass(t) IS NULL;

    IF faltando IS NOT NULL THEN
        RAISE EXCEPTION 'Tabelas ausentes: %. Aplique 001 e 004 antes.', faltando;
    END IF;

    IF to_regrole('anon') IS NULL OR to_regrole('authenticated') IS NULL THEN
        RAISE EXCEPTION 'Papéis anon/authenticated não encontrados. Execute no Supabase.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname IN ('anon', 'authenticated')
          AND (rolsuper OR rolbypassrls)
    ) THEN
        RAISE EXCEPTION 'anon/authenticated com superuser ou BYPASSRLS: revisão manual necessária.';
    END IF;
END $$;

-- 1. Revoga concessões diretas de tabela ----------------------------------
-- Inclui as concessões automáticas do Supabase e qualquer GRANT a PUBLIC.
REVOKE ALL PRIVILEGES ON TABLE public.recuperacoes_senha
    FROM PUBLIC, anon, authenticated;
REVOKE ALL PRIVILEGES ON TABLE public.pedidos_recuperacao_senha
    FROM PUBLIC, anon, authenticated;
REVOKE ALL PRIVILEGES ON TABLE public.recuperacao_fila_controle
    FROM PUBLIC, anon, authenticated;

-- 2. Revoga concessões de coluna, se existirem ----------------------------
-- Os defaults do Supabase são de tabela, mas uma configuração anterior pode
-- ter criado GRANT por coluna. A revogação de tabela não remove esses GRANTs.
DO $$
DECLARE
    tabela text;
    papel text;
    coluna text;
BEGIN
    FOREACH tabela IN ARRAY ARRAY[
        'public.recuperacoes_senha',
        'public.pedidos_recuperacao_senha',
        'public.recuperacao_fila_controle'
    ] LOOP
        FOREACH papel IN ARRAY ARRAY['PUBLIC', 'anon', 'authenticated'] LOOP
            FOR coluna IN
                SELECT attname
                FROM pg_attribute
                WHERE attrelid = tabela::regclass
                  AND attnum > 0
                  AND NOT attisdropped
                ORDER BY attnum
            LOOP
                EXECUTE format(
                    'REVOKE ALL PRIVILEGES (%I) ON TABLE %s FROM %s',
                    coluna,
                    tabela,
                    CASE WHEN papel = 'PUBLIC' THEN 'PUBLIC' ELSE quote_ident(papel) END
                );
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

-- 3. Revoga concessões herdadas de papéis intermediários ------------------
-- anon/authenticated podem ser membros de outros papéis. Revogar apenas
-- deles não remove privilégios herdados desses papéis. Revogamos, somente
-- nestas três tabelas e colunas, de qualquer papel com ACL explícita que
-- esteja na árvore de herança de anon/authenticated.
DO $$
DECLARE
    tabela text;
    papel text;
    coluna text;
    dono oid;
BEGIN
    FOREACH tabela IN ARRAY ARRAY[
        'public.recuperacoes_senha',
        'public.pedidos_recuperacao_senha',
        'public.recuperacao_fila_controle'
    ] LOOP
        SELECT relowner INTO dono FROM pg_class WHERE oid = tabela::regclass;

        IF pg_has_role((SELECT oid FROM pg_roles WHERE rolname = 'anon'), dono, 'MEMBER')
           OR pg_has_role((SELECT oid FROM pg_roles WHERE rolname = 'authenticated'), dono, 'MEMBER') THEN
            RAISE EXCEPTION 'Papel público herda o proprietário de %; revisão manual necessária.', tabela;
        END IF;

        FOR papel IN
            WITH RECURSIVE herdados(roleid) AS (
                SELECT oid FROM pg_roles WHERE rolname IN ('anon', 'authenticated')
                UNION
                SELECT m.roleid
                FROM pg_auth_members m
                JOIN herdados h ON m.member = h.roleid
            ),
            acls AS (
                SELECT a.grantee
                FROM pg_class c
                CROSS JOIN LATERAL aclexplode(c.relacl) a
                WHERE c.oid = tabela::regclass
                UNION
                SELECT a.grantee
                FROM pg_class c
                JOIN pg_attribute at
                  ON at.attrelid = c.oid AND at.attnum > 0 AND NOT at.attisdropped
                CROSS JOIN LATERAL aclexplode(at.attacl) a
                WHERE c.oid = tabela::regclass
            )
            SELECT DISTINCT r.rolname
            FROM acls ac
            JOIN pg_roles r ON r.oid = ac.grantee
            JOIN herdados h ON h.roleid = r.oid
            WHERE ac.grantee <> 0
              AND r.rolname NOT IN
                  ('postgres', 'service_role', 'supabase_admin', 'authenticator')
              AND r.rolname NOT LIKE 'pg\_%'
        LOOP
            EXECUTE format('REVOKE ALL PRIVILEGES ON TABLE %s FROM %I', tabela, papel);
            FOR coluna IN
                SELECT attname
                FROM pg_attribute
                WHERE attrelid = tabela::regclass
                  AND attnum > 0
                  AND NOT attisdropped
            LOOP
                EXECUTE format(
                    'REVOKE ALL PRIVILEGES (%I) ON TABLE %s FROM %I',
                    coluna, tabela, papel
                );
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

-- 4. Ativa RLS sem criar políticas ----------------------------------------
-- Sem políticas, papéis não proprietários não leem nem escrevem linhas,
-- mesmo que recebam GRANT por engano. Não usar FORCE: o backend é owner.
ALTER TABLE public.recuperacoes_senha ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pedidos_recuperacao_senha ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recuperacao_fila_controle ENABLE ROW LEVEL SECURITY;

-- 5. Verificação final (falha = rollback) ----------------------------------
DO $$
DECLARE
    tabela text;
    privilegio text;
BEGIN
    FOREACH tabela IN ARRAY ARRAY[
        'public.recuperacoes_senha',
        'public.pedidos_recuperacao_senha',
        'public.recuperacao_fila_controle'
    ] LOOP
        IF NOT EXISTS (
            SELECT 1 FROM pg_class
            WHERE oid = tabela::regclass AND relrowsecurity
        ) THEN
            RAISE EXCEPTION 'RLS não está ativo em %.', tabela;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM pg_class c
            CROSS JOIN LATERAL aclexplode(c.relacl) a
            WHERE c.oid = tabela::regclass
              AND (a.grantee = 0 OR a.grantee IN (
                    SELECT oid FROM pg_roles
                    WHERE rolname IN ('anon', 'authenticated')
              ))
        ) THEN
            RAISE EXCEPTION 'Privilégio de tabela remanescente em %.', tabela;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM pg_class c
            JOIN pg_attribute at
              ON at.attrelid = c.oid AND at.attnum > 0 AND NOT at.attisdropped
            CROSS JOIN LATERAL aclexplode(at.attacl) a
            WHERE c.oid = tabela::regclass
              AND (a.grantee = 0 OR a.grantee IN (
                    SELECT oid FROM pg_roles
                    WHERE rolname IN ('anon', 'authenticated')
              ))
        ) THEN
            RAISE EXCEPTION 'Privilégio de coluna remanescente em %.', tabela;
        END IF;

        FOREACH privilegio IN ARRAY ARRAY[
            'SELECT', 'INSERT', 'UPDATE', 'DELETE',
            'TRUNCATE', 'REFERENCES', 'TRIGGER'
        ] LOOP
            IF has_table_privilege('anon', tabela, privilegio)
               OR has_table_privilege('authenticated', tabela, privilegio) THEN
                RAISE EXCEPTION 'Privilégio % efetivo para papel público em %.',
                    privilegio, tabela;
            END IF;
        END LOOP;

        IF EXISTS (
            SELECT 1
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = split_part(tabela, '.', 2)
              AND ('public'::name = ANY(roles)
                   OR 'anon'::name = ANY(roles)
                   OR 'authenticated'::name = ANY(roles))
        ) THEN
            RAISE EXCEPTION 'Política RLS para papel público em %.', tabela;
        END IF;
    END LOOP;

    RAISE NOTICE 'RLS ativo e sem privilégios para PUBLIC/anon/authenticated nas três tabelas.';
END $$;

COMMIT;

-- Instruções de acesso do backend -----------------------------------------
-- 1. Confirme o papel do backend: DB_USERNAME no Render (padrão: postgres).
-- 2. Confirme o proprietário das tabelas:
--      SELECT relname, relowner::regrole
--      FROM pg_class
--      WHERE relname IN ('recuperacoes_senha',
--                        'pedidos_recuperacao_senha',
--                        'recuperacao_fila_controle');
-- 3. Se o backend usar o proprietário (postgres), nada mais é necessário.
-- 4. Se usar um papel dedicado NÃO proprietário, execute como administrador
--    (substitua m365_backend pelo papel real):
--      GRANT USAGE ON SCHEMA public TO m365_backend;
--      GRANT SELECT, INSERT, UPDATE, DELETE
--        ON TABLE public.recuperacoes_senha,
--                 public.pedidos_recuperacao_senha,
--                 public.recuperacao_fila_controle
--        TO m365_backend;
--      CREATE POLICY m365_backend_recuperacao ON public.recuperacoes_senha
--        FOR ALL TO m365_backend USING (true) WITH CHECK (true);
--      CREATE POLICY m365_backend_pedidos ON public.pedidos_recuperacao_senha
--        FOR ALL TO m365_backend USING (true) WITH CHECK (true);
--      CREATE POLICY m365_backend_fila ON public.recuperacao_fila_controle
--        FOR ALL TO m365_backend USING (true) WITH CHECK (true);
--    Prefira um papel dedicado com BYPASSRLS apenas se já for o padrão da
--    operação; caso contrário, as políticas acima restringem o acesso a ele.
