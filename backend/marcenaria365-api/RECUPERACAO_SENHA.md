# Recuperação de senha

## Implantação (não executada automaticamente)

1. Após backup e homologação, aplicar `sql/001_recuperacao_senha.sql` e
   `sql/004_fila_duravel_recuperacao.sql` no PostgreSQL com uma conta autorizada,
   antes de publicar a API. São aditivos, não excluem usuários nem modificam senhas existentes.
   O projeto mantém `spring.jpa.hibernate.ddl-auto=validate`.
2. Configurar no servidor, sem versionar valores:
   - `MAIL_HOST`: servidor SMTP do provedor.
   - `MAIL_PORT`: porta STARTTLS (padrão 587), conforme o provedor.
   - `MAIL_USERNAME`: usuário SMTP.
   - `MAIL_PASSWORD`: credencial SMTP (não é a senha do usuário do aplicativo).
   - `MAIL_FROM`: remetente verificado/autorizado pelo provedor.
   - `RECOVERY_ENCRYPTION_KEY`: chave AES de 32 bytes aleatórios, em Base64,
     gerada com ferramenta criptográfica e armazenada como segredo externo.
     **Manter a mesma chave entre reinícios e deploys**, diferente da chave JWT.
     Não gerar uma nova chave a cada inicialização. Fazer backup protegido da chave.
   - `RECOVERY_WORKER_ENABLED`: opcional, padrão `true`. `false` pausa o processamento
     sem apagar os pedidos persistidos; ao reativar, o processamento é retomado.
   As variáveis existentes `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`
   continuam com seus contratos originais. Não substituir a chave JWT existente.
3. Confirmar que a hospedagem permite saída SMTP e que o provedor aceita STARTTLS.
   Não desativar TLS para contornar falhas. Verificar domínio/remetente, SPF/DKIM
   e pasta de spam durante a homologação.
4. Publicar a API antes de distribuir o APK que usa os novos endpoints.

Sem host/remetente/chave de cifra válida, ou, com autenticação SMTP habilitada,
sem usuário/senha, a recuperação retorna 503, sem alterar cadastro e login.
Isso pressupõe o esquema obrigatório já aplicado: `validate` impede iniciar toda
a API se faltarem tabelas/colunas exigidas pelas entidades.

## Contratos novos

- `POST /api/auth/recuperacao`: `{ "email": "..." }`.
  HTTP 202 com mensagem genérica para e-mails cadastrados e desconhecidos.
  Confirma o recebimento **persistido por commit**, não a entrega do e-mail.
  O texto não afirma que o e-mail já foi enviado ou recebido; o envio continua
  pendente até o SMTP aceitar a mensagem. Pedidos de contas conhecidas e
  desconhecidas seguem a mesma admissão durável.
- `POST /api/auth/redefinicao`: `{ "codigo": "...", "senha": "..." }`.
  HTTP 200 somente depois do commit da nova senha e do consumo do código.
  Código inválido, usado ou expirado: 400. Não altera sessões/JWT existentes.

O código contém 256 bits gerados por `SecureRandom`, representados por 64 caracteres
hexadecimais para copiar e colar no aplicativo. A tabela de validação guarda somente
SHA-256. A fila guarda a cópia necessária para reenvio cifrada com AES-256-GCM,
nonce aleatório e autenticação vinculada ao identificador do pedido. Não há código
em texto puro nem chave de cifra no banco.
Validade: 15 minutos a partir da emissão; um código por conta. Pedidos duplicados e
reenvios de código ainda válido reutilizam o mesmo código e vencimento. Um retry
nunca reativa um código consumido. Se um pedido pendente atravessar o vencimento,
o processamento poderá emitir um novo código, somente após o anterior já expirar.
Consumo com bloqueio de linha e transação, junto da atualização BCrypt da senha.
Mantêm-se os cinco requisitos do cadastro; na recuperação há ainda o limite técnico
de 72 bytes do BCrypt, validado sem truncar a senha.

Pedidos são processados da mesma fila de recuperação, agora no PostgreSQL, com
limite global de 50 pendências protegido por bloqueio transacional de admissão.
Cada instância consulta a fila a cada cinco segundos. Bloqueios de linha impedem
preparação/envio simultâneo do mesmo pedido entre instâncias.

Há duas transações independentes: primeiro são confirmados o hash e o conteúdo
cifrado; **só depois** se chama SMTP. Falha SMTP mantém ambos e persiste retentativa
com espera de 30 segundos, crescendo até cinco minutos. Reiniciar retoma os pedidos.
Falhas de banco não são convertidas em confirmação de solicitação.

SMTP não oferece transação distribuída com o PostgreSQL: se aceitar a mensagem e
a API cair antes de registrar sucesso, haverá possível **entrega duplicada do mesmo
código**, ainda de uso único e com o mesmo vencimento, nunca promessa de "exactly once".
Entrega final e atrasos do provedor não são garantidos; um e-mail pode chegar após
o vencimento por atraso externo. A mensagem informa o instante real de expiração UTC.

Não rotacionar/perder a chave sem um procedimento de recifragem dos registros da fila:
a perda da chave impede recuperar o conteúdo cifrado, mas não expõe nem torna
reutilizáveis os códigos. Erro criptográfico preserva registros e gera alerta genérico.
Definir também retenção operacional dos pedidos (incluem e-mails); não foi criado
expurgo automático nem excluído histórico existente nesta correção.

Não habilitar logs de corpos HTTP, bind de SQL ou debug SMTP, pois contêm informações
sensíveis. Monitorar falhas e entrega no provedor antes de liberar em produção.

Limites por instância: cinco pedidos por minuto por endereço remoto, dez confirmações
por minuto e intervalo de um minuto por conta para reenvio. Em múltiplas instâncias,
adicionar limite compartilhado na infraestrutura. Atrás de proxy, homologar o endereço
remoto observado: não confiar indiscriminadamente em `X-Forwarded-For` enviado pelo cliente.

## Validação necessária em homologação

- Envio real, remetente autorizado e conteúdo sem senha.
- Mesmo HTTP e texto para conta existente/inexistente.
- Login com a nova senha; senha anterior recusada; código usado/expirado recusado.
- Reiniciar aplicativo/API e confirmar persistência no PostgreSQL.
- TalkBack, fonte ampliada, retorno ao login e tema claro no dispositivo.

Os testes de persistência usuais usam H2. `MigracoesPostgresTest` executa os SQLs
em PostgreSQL local isolado; `RecuperacaoReinicioPostgresTest` fecha e reabre seis
contextos completos da API contra esse PostgreSQL, exercita HTTP, queda simulada,
retentativa SMTP, duplicidade concorrente, limite de fila e uso único após reinício.
Esses testes exigem `M365_TEST_PG_URL` apontando somente para
`jdbc:postgresql://127.0.0.1:<porta>/m365_isolated`, com usuário local de teste
`m365_test`; nunca usam `DB_URL` de produção. São ignorados se essa variável não existir.
O SMTP nesses testes é simulado: não comprovam entrega real ao provedor.
