# Relatório das correções

Nenhum commit/push, deploy, alteração no banco hospedado/produção ou exclusão de backups foi realizado.
Os testes usam H2 e PostgreSQL 18.6 temporário isolado, sem acesso ao banco existente.

## Resultado final dos comandos

- `./gradlew.bat assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest`: **BUILD SUCCESSFUL**;
  seis testes Android (dois de mapper, três de conexão e um demonstrativo).
- `mvnw.cmd package`, com datasource sobrescrito para H2 em memória, usuário
  `sa` sem senha, `ddl-auto=create-drop` **somente nesse H2** e `JWT_SECRET`
  gerado aleatoriamente no processo de teste, mais `M365_TEST_PG_URL` apontando para
  o PostgreSQL temporário isolado: **BUILD SUCCESS**, 28 testes,
  nenhuma falha/erro/ignorado, JAR gerado. Não usar opções de criação de esquema no PostgreSQL de produção.
- `git diff --check`: sem erros de whitespace (somente avisos de LF/CRLF).
- `lintDebug`: passou após o escape local do caminho do SDK; sem supressão de verificações.
- `connectedDebugAndroidTest`: **BUILD SUCCESSFUL**, três testes no AVD
  `Marcenaria365_Teste`, API 35, iniciado em modo somente leitura. A primeira tentativa
  excedeu o timeout durante a inicialização; a reexecução terminou com os três aprovados.

## 1. Compilação

`rotuloSituacaoServico` já foi restaurada no commit `f2fb706` do repositório.
Não houve nova alteração nessa função. `assembleDebug`, inclusive com
`--rerun-tasks` na verificação inicial, terminou com `BUILD SUCCESSFUL`.
Não há novo commit a sugerir nesta etapa.

## 2. Recuperação de senha

Problema/causa: havia somente mensagem de indisponibilidade no botão de login;
nenhum mecanismo de recuperação ou configuração de provedor no backend.

Implementação: solicitação genérica HTTP 202 após commit do pedido, processamento SMTP fora da resposta
para não revelar contas, código aleatório de 256 bits, armazenamento SHA-256,
expiração de 15 minutos, uso único e troca BCrypt transacional com bloqueio de linha.
Não altera login, cadastro, logout ou validade das sessões existentes.
A nova tela é clara e reutiliza os cinco requisitos/indicador do cadastro.
No cadastro mudaram somente três declarações de `private` para `internal`, sem
alteração de conteúdo, validações, cores ou disposição.

Arquivos Android (sob `app/src/main/java/com/nexo/marcenaria365/`):
- `data/repository/AuthRepository.kt`
- `ui/screens/LoginScreen.kt`
- `ui/screens/RegisterScreen.kt`
- `ui/screens/RecuperacaoSenhaScreen.kt` (novo)

Arquivos backend (sob `backend/marcenaria365-api/`):
- `pom.xml` (SMTP em produção; H2 somente para testes)
- `src/main/resources/application.properties`
- `src/main/java/com/nexo/marcenaria365/api/config/SecurityConfig.java`
- `src/main/java/com/nexo/marcenaria365/api/config/RecuperacaoConfig.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/controller/RecuperacaoSenhaController.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/entity/RecuperacaoSenha.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/repository/RecuperacaoSenhaRepository.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/service/RecuperacaoSenhaService.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/service/SenhaRecuperacao.java` (novo)
- `sql/001_recuperacao_senha.sql` (novo, não executado)
- `RECUPERACAO_SENHA.md` (novo, implantação e limitações)

Testes novos sob `backend/marcenaria365-api/src/test/java/com/nexo/marcenaria365/api/`:
- `RecuperacaoSenhaServiceTest.java`: cinco requisitos, ausência de conta,
  hash/expiração, códigos usados/expirados/inexistentes, reenvio, SMTP e configuração.
- `RecuperacaoSenhaControllerTest.java`: resposta genérica, limitação de tentativas,
  falha sem falso sucesso e validação sem retornar conteúdo sensível.
- `RecuperacaoSenhaPersistenciaTest.java`: consumo concorrente, senha persistida,
  login com a nova senha, recusa da anterior e manutenção durável do código em falha SMTP em H2.

Após a auditoria, foram adicionados `CifraCodigoRecuperacaoTest` (dois testes),
`MigracoesPostgresTest` (oito testes) e `RecuperacaoReinicioPostgresTest` (um teste
integrado com seis contextos completos e chamadas HTTP reais locais).

Resultado: compilação Android e testes específicos passaram, inclusive PostgreSQL
temporário. Envio SMTP real, esquema hospedado, TalkBack e comparação visual completa
em dispositivo físico ainda não homologados.
Sugestão de commit, **não executado**: `feat: adiciona recuperacao segura de senha por email`.

## 3. Clientes inativos

Problema/causa: `ativo` não era enviado, persistido ou lido; mapper forçava `true`.
Correção: transportar o valor, persistir em coluna própria e atualizar a interface
somente com a resposta da API. Campo omitido por clientes antigos preserva o status.
Nenhum cliente ou vínculo com serviço é excluído. Inativações anteriores não
persistidas não podem ser reconstruídas automaticamente.

Arquivos Android (mesma base acima):
- `data/repository/ClienteRepository.kt`
- `data/mapper/ClienteMapper.kt`
- `ui/screens/OnboardingScreen.kt`
- `app/src/test/java/com/nexo/marcenaria365/ClienteMapperTest.kt` (novo)

Arquivos backend (mesma base acima):
- `src/main/java/com/nexo/marcenaria365/api/entity/Cliente.java`
- `src/main/java/com/nexo/marcenaria365/api/controller/ClienteController.java`
- `src/test/java/com/nexo/marcenaria365/api/ClientesPersistenciaTest.java` (novo)
- `sql/002_cliente_ativo.sql` (novo, não executado)
- `PENDENCIAS_PERSISTENCIA.md` (novo)

Resultado: compilação Android, testes de mapper e persistência/releitura em H2
passaram. Falta aplicar a coluna e homologar PostgreSQL/fechar e reabrir o aplicativo.
Sugestão de commit, **não executado**: `fix: persiste situacao ativa dos clientes`.

## 4. Orçamentos

Problema comprovado nos artefatos: SQL inicial não aceita `EM_APROVACAO` enviado
pelo aplicativo e não contém os campos `percentual_entrada`/`detalhe` usados na API.
O esquema real hospedado não foi consultado: não afirmar que essa é a única causa
da falha relatada. Preparada migração transacional para o esquema inicial e aceitação
compatível de `ENVIADO`, sem renomear status nem remover campos da interface.

Arquivos:
- `app/src/main/java/com/nexo/marcenaria365/data/repository/OrcamentoRepository.kt`
- `backend/marcenaria365-api/src/main/java/com/nexo/marcenaria365/api/controller/OrcamentoController.java`
- `backend/marcenaria365-api/sql/003_compatibilidade_orcamentos.sql` (novo, revisar antes de aplicar)
- `backend/marcenaria365-api/src/test/java/com/nexo/marcenaria365/api/OrcamentosPersistenciaTest.java` (novo)
- `backend/marcenaria365-api/PENDENCIAS_PERSISTENCIA.md`

Resultado: Android compilou; cadastro/releitura/edição pela API, itens de materiais
e mão de obra, totais e isolamento por usuário passaram em H2. A migração foi testada
em PostgreSQL temporário; esquema hospedado e orçamento após reabrir o aplicativo
ainda não foram homologados.
Sugestão de commit, **não executado**: `fix: compatibiliza persistencia dos orcamentos`.

## 5. Conexão e erros

Problema: exceções de rede podiam aparecer com mensagens técnicas sem distinguir
timeout, DNS, conexão recusada e TLS. Correção no cliente HTTP compartilhado,
sem tentativas automáticas de reenvio de gravações (evita duplicidade).
Ausência de rede é informada quando `ConnectivityManager` confirma que não há
rede ativa; DNS com rede ativa não é rotulado automaticamente como falta de internet.
Respostas de validação, sessão expirada e credenciais incorretas continuam específicas.
Não é possível inferir início do Render somente por timeout/503: não foi inventado
um detector. Os textos de conexão aparecem somente durante uma requisição pendente.

Arquivos:
- `app/src/main/AndroidManifest.xml` (permissão normal `ACCESS_NETWORK_STATE`)
- `app/src/main/java/com/nexo/marcenaria365/MainActivity.kt`
- `app/src/main/java/com/nexo/marcenaria365/data/remote/ApiClient.kt`
- `app/src/main/java/com/nexo/marcenaria365/data/remote/ErroConexao.kt` (novo)
- repositórios `AuthRepository.kt`, `ClienteRepository.kt` e `OrcamentoRepository.kt`
- `app/src/test/java/com/nexo/marcenaria365/ErroConexaoTest.kt` (novo)

Resultado: compilação e testes Android passaram. Cenários reais de rede/Render
ainda exigem dispositivo e servidor configurado.
Sugestão de commit, **não executado**: `fix: diferencia erros de conexao e indisponibilidade`.

## 6. Acessibilidade e limitações finais

Revisão estática dos filtros/abas de clientes e serviços: seleção já declarada.
Nenhuma mudança visual arbitrária foi feita. A recuperação nova usa labels,
descrições de ícones, título semântico, mensagem em live region e conteúdo rolável.
O indicador de força e os cinco requisitos do cadastro foram preservados.
Os testes de estado foram executados posteriormente em emulador somente leitura.
TalkBack, fonte ampliada, contraste e regressão visual completa **não foram comprovados
em execução**. A revisão não equivale a uma auditoria completa de todas as telas/gráficos.
Sem correção independente comprovada em telas existentes; nenhum commit extra sugerido.

O erro `PropertyEscape` de `local.properties:1` foi corrigido, após autorização,
somente escapando `C:` como `C\:` no caminho do SDK. O Lint passou, sem ser
desabilitado; avisos preexistentes não motivaram refatorações/atualizações.
O arquivo continua ignorado pelo Git e não foi adicionado ao índice.

Nenhuma alegação de funcionamento ponta a ponta em produção é feita. Aplicar os
SQLs após revisão, configurar SMTP, publicar backend e homologar os fluxos no Android
antes de considerar todas as pendências encerradas.

## 7. Correções autorizadas após auditoria

### Recuperação durável

Causa: fila em memória e SMTP dentro da mesma transação que criava o código.
Solução: mesma recuperação e mesmos endpoints, agora com pedido persistido antes
de HTTP 202, preparação confirmada em transação separada e envio recuperável.
Hash permanece na tabela de validação; cópia para retentativa fica cifrada com
AES-256-GCM e chave externa estável. Duplicatas/reinícios não trocam um código válido
nem renovam seu vencimento; consumo permanece transacional e de uso único.
Falha SMTP persiste backoff e preserva pedido/código. Fila global de 50 pedidos,
admissão serializada e bloqueios de envio protegem concorrência entre instâncias.

Arquivos desta correção, sob `backend/marcenaria365-api/`:
- `src/main/java/com/nexo/marcenaria365/api/service/RecuperacaoSenhaService.java`
- `src/main/java/com/nexo/marcenaria365/api/service/CifraCodigoRecuperacao.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/entity/PedidoRecuperacaoSenha.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/repository/PedidoRecuperacaoSenhaRepository.java` (novo)
- `src/main/java/com/nexo/marcenaria365/api/config/RecuperacaoConfig.java`
- `src/main/java/com/nexo/marcenaria365/api/controller/RecuperacaoSenhaController.java`
- `src/main/resources/application.properties`
- `sql/004_fila_duravel_recuperacao.sql` (novo; executado somente no PostgreSQL de testes)
- testes `RecuperacaoSenhaServiceTest.java`, `RecuperacaoSenhaPersistenciaTest.java`,
  `CifraCodigoRecuperacaoTest.java` (novo), `RecuperacaoReinicioPostgresTest.java` (novo),
  sob `src/test/java/com/nexo/marcenaria365/api/`
- `RECUPERACAO_SENHA.md`

O teste de reinício fecha/reabre seis contextos completos contra o mesmo PostgreSQL,
simula pedido aceito antes da parada, código preparado antes do SMTP, queda após
aceitação simulada pelo SMTP, erro SMTP, retentativa e consumo após outro reinício.
Testa também duplicatas concorrentes de pedido/envio, limite global de fila e HTTP
de recuperação, redefinição e login. O provedor de e-mail é simulado, não um envio real.
Não é teste de interrupção física da máquina nem de falha do storage do PostgreSQL.

Riscos remanescentes: SMTP permite entrega duplicada se houver queda após aceitação,
mas nesse caso é o mesmo código válido e de uso único. Atraso externo pode entregar
e-mail após o vencimento. A chave de cifra deve ser preservada, protegida e não rotacionada
sem procedimento de recifragem. Configuração do Render, retenção dos pedidos e
monitoramento operacional ainda precisam de homologação.
Sugestão de commit, não executado: `fix: torna recuperacao de senha duravel e retomavel`.

### Constraint da migração 003

Causa: o script verificava só o nome. Solução: comparar definição integral com as
expressões conhecidas normalizadas pelo PostgreSQL, sob bloqueio, antes de qualquer
alteração permanente. Regras desconhecidas, extras ou não validadas e colunas
incompatíveis interrompem atomicamente; reexecução preserva a constraint já migrada.
Arquivos: `sql/003_compatibilidade_orcamentos.sql`,
`src/test/java/com/nexo/marcenaria365/api/MigracoesPostgresTest.java` (novo) e
`PENDENCIAS_PERSISTENCIA.md`. Oito testes passaram em PostgreSQL isolado.
Backup/restauração e comparação com o esquema de produção continuam obrigatórios;
nenhum script foi aplicado nele.
Sugestão de commit, não executado: `fix: valida definicao das constraints antes de migrar orcamentos`.

### Estados Android

Causa: estados do login saíam da composição; a mensagem da recuperação não era salvável.
Solução: manter as declarações de estado antes da ramificação da recuperação e
salvar mensagem/indicador de erro com `rememberSaveable`. Código e nova senha não
foram incluídos no estado salvo. Sem alterações de layout, cores, campos ou botões.
Parâmetros internos de chamada com defaults existentes permitem testar sem acessar Render.
Arquivos Android: `ui/screens/LoginScreen.kt`, `ui/screens/RecuperacaoSenhaScreen.kt`
(sob `app/src/main/java/com/nexo/marcenaria365/`) e
`app/src/androidTest/java/com/nexo/marcenaria365/AutenticacaoEstadoTest.kt` (novo).
Os dois testes novos de interface passaram no emulador: retorno com e-mail/senha
preservados e restauração do estado de sucesso sem novas requisições. A restauração
usa `StateRestorationTester`; rotação física/manual e TalkBack permanecem pendentes.
Sugestão de commit, não executado: `fix: preserva estados de login e recuperacao de senha`.

### Lint local

Somente `local.properties`, já ignorado pelo Git, foi corrigido. Não deve entrar
em commit. Lint passou. Nenhum baseline ou supressão foi adicionado.

### Limites de regressão

Passaram os testes existentes de clientes/orçamentos e os testes HTTP locais de
autenticação. Cadastro, serviços, caixa e relatórios não receberam alterações nesta
rodada e compilam, mas isso não comprova todos os fluxos reais: a regressão funcional
completa dessas áreas, comparação visual e acessibilidade manual continuam pendentes.
