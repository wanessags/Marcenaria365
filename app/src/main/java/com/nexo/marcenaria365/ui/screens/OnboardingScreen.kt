
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoPolitica = Color(0xFFFAF6F0)
private val AzulPolitica = Color(0xFF193447)
private val CinzaPolitica = Color(0xFF929292)
private val CobrePolitica = Color(0xFFBD906E)

@Composable
fun PoliticaPrivacidadeScreen(
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),

        containerColor = FundoPolitica,

        // Cabeçalho azul com seta de voltar
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(AzulPolitica)
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 7.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Text(
                    text = "Política de Privacidade",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 54.dp)
                )
            }
        },

        // Botão fixo na parte inferior
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FundoPolitica)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    )
            ) {
                Button(
                    onClick = onVoltar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPolitica,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Entendi",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->

        // Conteúdo rolável
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 25.dp)
        ) {
            Spacer(Modifier.height(26.dp))

            SecaoPolitica(
                titulo = "1. Coleta de informações",
                descricao = "O Marcenaria 365 permite cadastrar " +
                        "informações como nome e contato de clientes, " +
                        "descrições de serviços, orçamentos e " +
                        "movimentações financeiras. Os dados " +
                        "informados devem ser necessários à " +
                        "organização das atividades da marcenaria."
            )

            SecaoPolitica(
                titulo = "2. Uso dos dados",
                descricao = "As informações cadastradas são " +
                        "utilizadas nas funcionalidades do aplicativo, " +
                        "como organização de clientes, acompanhamento " +
                        "de serviços, elaboração de orçamentos e " +
                        "controle de recebimentos e despesas."
            )

            SecaoPolitica(
                titulo = "3. Compartilhamento",
                descricao = "O compartilhamento de informações " +
                        "depende das ações realizadas pelo usuário. " +
                        "Por exemplo, ao escolher enviar um orçamento " +
                        "por outro aplicativo, o usuário decide com " +
                        "quem compartilhar seu conteúdo."
            )

            SecaoPolitica(
                titulo = "4. Armazenamento e segurança",
                descricao = "O Marcenaria 365 está em desenvolvimento. " +
                        "Nesta versão, parte dos registros utiliza " +
                        "dados fictícios e armazenamento temporário. " +
                        "O armazenamento permanente, a autenticação " +
                        "e os controles de segurança deverão ser " +
                        "avaliados e implementados antes do uso " +
                        "com informações reais."
            )

            SecaoPolitica(
                titulo = "5. Seus direitos",
                descricao = "A Lei Geral de Proteção de Dados " +
                        "Pessoais (LGPD) estabelece direitos " +
                        "relacionados ao tratamento de dados pessoais, " +
                        "incluindo acesso, correção e exclusão nas " +
                        "hipóteses previstas em lei. Os procedimentos " +
                        "para atender solicitações deverão ser " +
                        "definidos antes da disponibilização do " +
                        "aplicativo para uso real."
            )

            SecaoPolitica(
                titulo = "6. Contato",
                descricao = "Em caso de dúvidas sobre o tratamento " +
                        "de dados, o usuário deverá consultar o " +
                        "responsável pelo aplicativo. Um canal " +
                        "oficial de atendimento ainda será definido."
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = "Informações importantes",
                color = CobrePolitica,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = "Este documento apresenta informações " +
                        "preliminares sobre o projeto acadêmico " +
                        "Marcenaria 365. O conteúdo e as práticas " +
                        "de tratamento de dados deverão ser " +
                        "revisados antes do uso com dados pessoais reais.",
                color = CinzaPolitica,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SecaoPolitica(
    titulo: String,
    descricao: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 26.dp)
    ) {
        Text(
            text = titulo,
            color = AzulPolitica,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(9.dp))

        Text(
            text = descricao,
            color = CinzaPolitica,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
