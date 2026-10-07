# Marcenaria 365

Primeira versão Android em Kotlin do PI do grupo NEXO. Interface nativa com cores de madeira, fundo claro e navegação por serviços e clientes.

## Abrir

No Android Studio, escolha **Open** e selecione esta pasta. Aguarde a sincronização Gradle e use **Run** em um emulador ou celular Android 8 ou superior. Use o JDK integrado ao Android Studio (17 ou superior). A compilação utiliza AGP 9.2.1, Gradle 9.4.1 e SDK 35. O suporte Kotlin está integrado ao AGP.

## O que funciona nesta etapa

- Entrada na demonstração sem senha fictícia ou falsa autenticação.
- Cadastro e edição de clientes; cadastro e consulta de serviços.
- Orçamentos com itens, quantidades decimais, valores em centavos, versões, aprovação e recusa.
- Registro manual de pagamentos com revisão, alerta de excedente e cancelamento com motivo.
- Execução e conclusão independentes da quitação; histórico da sessão.
- Exemplo de R$ 2.000,00, com R$ 600,00 recebidos e saldo de R$ 1.400,00.

Os dados são fictícios e ficam em memória. O estado é preservado em recriações normais da Activity, mas não há persistência garantida após encerrar o processo. Não inserir dados reais. Esta etapa não substitui o MVP completo documentado.

## Próximas etapas do projeto

1. API Java/Spring Boot, PostgreSQL, autenticação real e isolamento por usuário; substituir DemoStore por repositório conectado ao servidor. O Android nunca deverá acessar o banco diretamente.
2. Complementar orçamento com validade, datas, categorias, registro de entrega e cópia do resumo. Incluir busca de clientes, edição do serviço e correção vinculada de pagamentos.
3. Foto opcional e descrição por serviço via backend/Cloudinary; tratar autorização, exclusão e falhas. Nenhuma chave do fornecedor deve ficar no aplicativo.
4. Aviso de privacidade definitivo, solicitações de dados, backup e verificação dos demais requisitos legais e de segurança.

## Acessibilidade e verificação

Os controles usam texto, rótulos associados aos campos, unidades sp e altura mínima de 52 dp. Telas rolam verticalmente e não dependem só de cor. Ainda precisam ser testadas com TalkBack, fonte ampliada e teclado; não há declaração de conformidade. Anúncios automáticos ficam para etapa posterior.

Execute `gradlew.bat testDebugUnitTest assembleDebug` no Windows. Os testes cobrem centavos, arredondamento, saldo, cancelamento, excedente e a separação entre conclusão e quitação.

Referências técnicas: https://developer.android.com/build/releases/agp-9-2-0-release-notes e https://developer.android.com/build/migrate-to-built-in-kotlin.
