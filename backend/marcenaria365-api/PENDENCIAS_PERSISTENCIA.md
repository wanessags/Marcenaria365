# Correções de persistência

## Clientes

Aplicar `sql/002_cliente_ativo.sql` antes da nova API. Acrescenta `ativo` com
valor inicial verdadeiro para clientes antigos, sem exclusões ou alterações de vínculos.
Pedidos antigos que omitem `ativo` não reativam clientes ao editar.
Android envia o valor escolhido e usa o status confirmado pela API.

O teste `ClientesPersistenciaTest` cobre gravação/releitura, compatibilidade com
campo omitido, preservação de endereço/observações e isolamento por usuário em H2.
Ainda é necessário homologar o PostgreSQL e reiniciar o aplicativo no dispositivo.

## Orçamentos

O SQL inicial versionado admite `ENVIADO`, mas não `EM_APROVACAO` enviado pelo
Android/API. Também não declara `percentual_entrada` e `detalhe` usados pelas entidades.
Isso comprova uma incompatibilidade nos artefatos, não o estado do banco hospedado.

Antes de aplicar `sql/003_compatibilidade_orcamentos.sql`, comparar o esquema real:

```sql
SELECT column_name, data_type FROM information_schema.columns
WHERE table_schema = current_schema()
  AND table_name IN ('orcamentos', 'orcamento_itens');
SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint
WHERE conrelid = 'orcamentos'::regclass;
```

A migração é transacional e não exclui dados. Confere a definição completa da
constraint, comparando com duas expressões conhecidas normalizadas pelo próprio
PostgreSQL: a original e a já migrada. Nome igual com definição personalizada,
nome diferente, regra adicional sobre status, constraint não validada e tipos de
coluna incompatíveis interrompem a operação. Mesmo uma expressão equivalente
desconhecida pode ser recusada conservadoramente: não tentar "corrigi-la" automaticamente.
Reexecução do esquema já migrado mantém a constraint, sem removê-la outra vez.

Há bloqueio exclusivo das tabelas, com espera máxima de cinco segundos para
adquiri-lo. Fazer backup protegido de dados/esquema, validar restauração e ensaiar
em homologação antes de agendar a aplicação no banco real. Confirmar o search_path
correto e revisar qualquer tabela/coluna preexistente; `IF NOT EXISTS` não valida
sozinho um esquema. Não rodar novamente o script inicial, pois contém dados de exemplo.
A API passa a aceitar também `ENVIADO`, mantendo compatibilidade com registros antigos.

Não foram alterados os textos atuais das telas: `Em aprovação`, `Aprovado` e `Recusado`.
Os nomes `Em análise`/`Reprovado` solicitados não existem no código atual;
uma eventual renomeação depende de confirmação.

A tela atual permite cadastrar/consultar/excluir orçamento; não oferece edição,
embora a API possua PUT. Não foi criada uma nova interface de edição sem autorização.
Materiais e mão de obra continuam representados pelos itens existentes.

Validar em homologação: salvar, reler, fechar/reabrir o aplicativo, conferir totais,
entrada, saldo, materiais e mão de obra, e examinar falhas HTTP/constraints da API.

Os oito cenários de `MigracoesPostgresTest` foram executados em um PostgreSQL 18.6
temporário e isolado. Isso não comprova compatibilidade com o esquema real hospedado,
que não foi consultado nem alterado. Inclui-se teste aditivo/reexecutável da fila 004,
preservando registros da tabela de códigos criada por 001.
