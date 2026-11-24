
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoLegal = Color(0xFFFAF6F0)
private val AzulLegal = Color(0xFF193447)
private val CobreLegal = Color(0xFF94613C)
private val CinzaLegal = Color(0xFF53616A)

enum class TipoDocumentoLegal {
    PRIVACIDADE,
    TERMOS
}

@Composable
fun DocumentosLegaisScreen(
    tipo: TipoDocumentoLegal,
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    val titulo = when (tipo) {
        TipoDocumentoLegal.PRIVACIDADE ->
            "Política de Privacidade"

        TipoDocumentoLegal.TERMOS ->
            "Termos de Uso"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoLegal,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulLegal)
                    .heightIn(min = 62.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Button(
                onClick = onVoltar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 12.dp
                    )
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulLegal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Entendi",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            Text(
                text = titulo,
                color = AzulLegal,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(16.dp))

            InformacoesLegais()

            Spacer(Modifier.height(24.dp))

            when (tipo) {
                TipoDocumentoLegal.PRIVACIDADE ->
                    ConteudoPrivacidade()

                TipoDocumentoLegal.TERMOS ->
                    ConteudoTermos()
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun InformacoesLegais() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Informações do documento",
                color = AzulLegal,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            LinhaLegal("Versão", "1.0")
            LinhaLegal("Vigência", "24/11/2026")
            LinhaLegal(
                "Última atualização",
                "24/11/2026"
            )
        }
    }
}

@Composable
private fun LinhaLegal(
    rotulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = rotulo,
            color = CinzaLegal,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = valor,
            color = AzulLegal,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SecaoLegal(
    titulo: String,
    descricao: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 23.dp)
    ) {
        Text(
            text = titulo,
            color = AzulLegal,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.semantics {
                heading()
            }
        )

        Spacer(Modifier.height(9.dp))

        Text(
            text = descricao,
            color = CinzaLegal,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AvisoLegal(
    texto: String
) {
    Surface(
        color = Color(0xFFF3E7DA),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = texto,
                color = AzulLegal,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun ConteudoPrivacidade() {
    SecaoLegal(
        titulo = "1. Coleta de informações",
        descricao =
            "O Marcenaria 365 permite cadastrar informações " +
                    "de clientes, contatos, serviços, orçamentos " +
                    "e movimentações financeiras. O usuário deve " +
                    "cadastrar somente os dados necessários " +
                    "à organização de sua atividade."
    )

    SecaoLegal(
        titulo = "2. Finalidade e uso dos dados",
        descricao =
            "As informações são utilizadas para organizar " +
                    "as atividades da marcenaria, acompanhar serviços, " +
                    "elaborar orçamentos e controlar recebimentos. " +
                    "Os dados não devem ser utilizados para " +
                    "finalidades incompatíveis com essas atividades."
    )

    SecaoLegal(
        titulo = "3. Compartilhamento",
        descricao =
            "O usuário pode escolher compartilhar informações " +
                    "ou orçamentos por outros aplicativos. " +
                    "O conteúdo compartilhado depende de sua ação. " +
                    "Antes do compartilhamento, recomenda-se verificar " +
                    "se os dados são adequados ao destinatário."
    )

    SecaoLegal(
        titulo = "4. Armazenamento e segurança",
        descricao =
            "O aplicativo utiliza autenticação e uma API " +
                    "para o acesso aos registros. O projeto ainda " +
                    "está em desenvolvimento e depende de testes " +
                    "e revisões adicionais de segurança, privacidade " +
                    "e retenção de dados antes do uso em produção."
    )

    SecaoLegal(
        titulo = "5. Direitos dos titulares",
        descricao =
            "A Lei Geral de Proteção de Dados Pessoais (LGPD) " +
                    "prevê direitos como acesso, correção e, " +
                    "nas condições legais, exclusão de dados pessoais. " +
                    "Os procedimentos para atendimento das solicitações " +
                    "precisam ser formalizados antes da disponibilização " +
                    "do sistema para uso comercial."
    )

    SecaoLegal(
        titulo = "6. Responsabilidades",
        descricao =
            "O profissional que cadastra informações de seus " +
                    "clientes deve observar a legislação aplicável, " +
                    "utilizar apenas os dados necessários e evitar " +
                    "o compartilhamento indevido dessas informações."
    )

    SecaoLegal(
        titulo = "7. Contato",
        descricao =
            "O Marcenaria 365 é um projeto acadêmico " +
                    "do Grupo NEXO. Um canal oficial para solicitações " +
                    "relacionadas a dados pessoais deverá ser " +
                    "definido antes do uso comercial."
    )

    SecaoLegal(
        titulo = "8. Atualizações e vigência",
        descricao =
            "Esta versão da Política de Privacidade " +
                    "entra em vigor em 24/11/2026. Alterações " +
                    "futuras deverão ser identificadas por nova " +
                    "versão e data de atualização. Mudanças " +
                    "relevantes deverão ser comunicadas aos usuários."
    )

    AvisoLegal(
        "Documento preliminar do projeto acadêmico " +
                "Marcenaria 365. O conteúdo não representa " +
                "certificação de conformidade integral com a LGPD " +
                "e deve ser revisado antes do tratamento " +
                "de dados pessoais reais em produção."
    )
}

@Composable
private fun ConteudoTermos() {
    SecaoLegal(
        titulo = "1. Apresentação",
        descricao =
            "O Marcenaria 365 é um aplicativo desenvolvido " +
                    "pelo Grupo NEXO como projeto acadêmico, " +
                    "com o objetivo de apoiar a organização " +
                    "das atividades de marceneiros autônomos."
    )

    SecaoLegal(
        titulo = "2. Funcionalidades",
        descricao =
            "O aplicativo oferece recursos para cadastro " +
                    "de clientes, gerenciamento de serviços, " +
                    "orçamentos, materiais e acompanhamento " +
                    "de movimentações financeiras. Algumas " +
                    "funcionalidades ainda estão em desenvolvimento " +
                    "ou dependem de validação."
    )

    SecaoLegal(
        titulo = "3. Cadastro e acesso",
        descricao =
            "O usuário deve fornecer informações corretas " +
                    "ao criar sua conta, proteger sua senha e evitar " +
                    "o compartilhamento de suas credenciais. " +
                    "O acesso às funções protegidas depende " +
                    "de autenticação."
    )

    SecaoLegal(
        titulo = "4. Responsabilidades do usuário",
        descricao =
            "O usuário é responsável pelas informações " +
                    "que cadastra, pela conferência de valores, " +
                    "prazos e orçamentos e pela utilização " +
                    "adequada do aplicativo. Também deve " +
                    "respeitar a privacidade dos clientes " +
                    "e as normas legais aplicáveis."
    )

    SecaoLegal(
        titulo = "5. Informações financeiras",
        descricao =
            "Os registros de entradas, saídas e recebimentos " +
                    "têm finalidade de organização e acompanhamento. " +
                    "O usuário deve conferir os lançamentos. " +
                    "O aplicativo não substitui serviços contábeis, " +
                    "financeiros ou fiscais especializados."
    )

    SecaoLegal(
        titulo = "6. Disponibilidade",
        descricao =
            "O sistema pode depender de conexão com a internet " +
                    "e de serviços externos de hospedagem. " +
                    "Por se tratar de uma versão acadêmica " +
                    "em desenvolvimento, podem ocorrer interrupções, " +
                    "limitações ou erros."
    )

    SecaoLegal(
        titulo = "7. Privacidade",
        descricao =
            "O tratamento de informações pessoais é abordado " +
                    "na Política de Privacidade do Marcenaria 365, " +
                    "disponível para consulta antes do cadastro " +
                    "e no menu Mais do aplicativo."
    )

    SecaoLegal(
        titulo = "8. Atualizações",
        descricao =
            "O Grupo NEXO poderá realizar melhorias " +
                    "nas funcionalidades e nos documentos " +
                    "do projeto. Novas versões dos Termos de Uso " +
                    "deverão informar a data de atualização " +
                    "e o início de sua vigência."
    )

    SecaoLegal(
        titulo = "9. Vigência",
        descricao =
            "Estes Termos de Uso, versão 1.0, " +
                    "entram em vigor em 24/11/2026."
    )

    AvisoLegal(
        "Estes Termos de Uso foram preparados " +
                "para a versão acadêmica do Marcenaria 365. " +
                "O conteúdo deverá passar por revisão jurídica " +
                "antes de eventual disponibilização comercial."
    )
}
