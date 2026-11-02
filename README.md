# Marcenaria 365

**Projeto Integrador — Grupo NEXO**

O Marcenaria 365 é um aplicativo Android desenvolvido para ajudar marceneiros autônomos a organizar suas atividades profissionais, desde o cadastro de clientes e serviços até o acompanhamento de orçamentos e recebimentos.

**Slogan:** Do orçamento ao recebimento, tudo organizado.

## 1. Objetivo

Oferecer uma ferramenta simples e acessível para centralizar informações da marcenaria, reduzir o uso de anotações dispersas e facilitar o acompanhamento financeiro e operacional dos serviços.

## 2. Tecnologias utilizadas

### Aplicativo Android
- Kotlin
- Jetpack Compose
- Material Design 3
- Android Studio
- Gradle
- Comunicação com API REST por HTTPS

### Backend
- Java
- Spring Boot
- Spring Security
- Autenticação JWT
- API REST
- JPA/Hibernate

### Banco de dados e hospedagem
- PostgreSQL
- Supabase — banco de dados
- Render — hospedagem da API

### Controle de versão
- Git
- GitHub

## 3. Funcionalidades

O projeto contempla:

- Cadastro e autenticação de marceneiros.
- Login com token JWT.
- Cadastro, consulta, edição e exclusão de clientes.
- Cadastro e gerenciamento de serviços.
- Gerenciamento de orçamentos.
- Registro de recebimentos.
- Controle de caixa.
- Cadastro e gerenciamento de materiais.
- Tela de perfil do usuário.
- Política de privacidade.
- Tela de ajuda e suporte.
- Encerramento da sessão.
- Restrições de acesso aos registros por usuário autenticado.

Algumas funcionalidades ainda dependem de testes completos na interface Android antes da entrega definitiva.

## 4. Arquitetura do sistema

O aplicativo utiliza uma arquitetura cliente-servidor.

**Aplicativo Android → API Spring Boot → PostgreSQL (Supabase)**

O aplicativo realiza requisições HTTPS à API, que é responsável por autenticação, regras de negócio e acesso aos dados.

O banco de dados não é acessado diretamente pelo aplicativo Android.

## 5. Autenticação e segurança

A autenticação é realizada por meio de JWT.

Após o login, a API retorna um token, utilizado pelo aplicativo para acessar os endpoints protegidos.

Entre os mecanismos implementados estão:

- Senhas protegidas por BCrypt.
- Autenticação de endpoints por Spring Security.
- JWT assinado com HS256.
- Tokens com validade de uma hora.
- Identificação do usuário autenticado pelo JWT.
- Consultas de registros vinculados ao usuário.
- Encerramento da sessão local no logout.
- Tratamento de erros HTTP 401 e 403.

A expiração automática da sessão na interface e outros cenários de segurança ainda exigem validação adicional.

## 6. Testes realizados

Foram realizados os seguintes testes:

| Teste | Resultado |
|---|---|
| Compilação Android com assembleDebug | Aprovado |
| Login pela API com duas contas de teste | Aprovado |
| Requisição sem JWT para /api/clientes | HTTP 401 |
| Consulta autenticada de clientes | Aprovado |
| Cadastro de cliente fictício pela API | Aprovado |
| Acesso ao cliente por outra conta | HTTP 404 |
| Consulta ao cliente após exclusão | HTTP 404 |

Os testes indicam que os principais mecanismos de autenticação e isolamento de clientes estão funcionando nos cenários verificados.

Ainda são necessários testes funcionais completos no aplicativo Android.

## 7. Como executar

### Aplicativo Android

1. Abrir o projeto no Android Studio.
2. Aguardar a sincronização do Gradle.
3. Verificar a instalação do Android SDK e JDK compatíveis com o projeto.
4. Selecionar um emulador Android compatível.
5. Executar o aplicativo.

Para gerar uma versão de depuração pelo terminal no Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

### Backend

O backend está localizado em:

`backend/marcenaria365-api`

A aplicação utiliza Java, Spring Boot e PostgreSQL.

Para execução local, é necessário configurar adequadamente as variáveis de ambiente e a conexão com o banco de dados.

As credenciais, senhas e chaves de assinatura JWT não devem ser publicadas no repositório.

### API hospedada

`https://marcenaria365-api.onrender.com`

O serviço hospedado no plano gratuito do Render pode apresentar demora na primeira resposta após um período de inatividade.

## 8. Privacidade e proteção de dados

O projeto considera princípios da Lei Geral de Proteção de Dados Pessoais (LGPD).

As informações cadastradas devem ser utilizadas exclusivamente para as finalidades relacionadas à organização dos serviços da marcenaria.

A política de privacidade disponível no aplicativo ainda possui caráter preliminar e deverá passar por revisão antes de uma utilização com dados pessoais reais.

## 9. Limitações e melhorias futuras

- Validar todas as funcionalidades no emulador ou dispositivo Android.
- Melhorar o tratamento de sessões expiradas na interface.
- Ampliar os testes automatizados.
- Revisar acessibilidade e experiência de uso.
- Consolidar a documentação técnica.
- Avaliar melhorias de desempenho e disponibilidade da API.
- Revisar os requisitos de privacidade e segurança para utilização em produção.

## 10. Equipe

**Grupo NEXO**

Projeto acadêmico desenvolvido no contexto do Projeto Integrador, com foco no apoio à organização e à gestão de atividades de profissionais autônomos da marcenaria.

---

**Marcenaria 365 — Do orçamento ao recebimento, tudo organizado.**
