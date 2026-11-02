package com.nexo.marcenaria365.ui.screens.ajuda

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Marrom = Color(0xFFB88966)

private data class Pergunta(
    val titulo: String,
    val resposta: String
)

private val perguntas = listOf(
    Pergunta(
        "Como cadastrar um cliente?",
        "Acesse Clientes e utilize a opção de cadastro. Informe os dados necessários para identificar e contatar o cliente."
    ),
    Pergunta(
        "Como cadastrar um serviço?",
        "Acesse Serviços, escolha cadastrar e preencha as informações do trabalho, incluindo cliente, descrição e prazo."
    ),
    Pergunta(
        "Como criar um orçamento?",
        "Na área de orçamentos, selecione o cliente e informe os itens, materiais, mão de obra e prazo."
    ),
    Pergunta(
        "Como registrar um recebimento?",
        "Acesse a área de Caixa ou recebimentos, selecione o serviço correspondente e registre o valor recebido."
    ),
    Pergunta(
        "Como controlar os materiais?",
        "Acesse Mais > Materiais. Você poderá cadastrar materiais, consultar o estoque, editar informações e excluir registros."
    ),
    Pergunta(
        "Onde encontro meus dados?",
        "Acesse Mais > Meu perfil ou Mais > Configurações para consultar os dados da conta."
    ),
    Pergunta(
        "Como sair do aplicativo?",
        "Acesse Mais e escolha Sair da conta. Confirme para retornar à tela de login."
    )
)

@Composable
fun AjudaSuporteScreen(
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    Scaffold(
        containerColor = Fundo,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Azul)
                    .statusBarsPadding()
                    .height(62.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Ajuda e suporte",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 30.dp
            )
        ) {
            item {
                Text(
                    text = "Como podemos ajudar?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Azul
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Encontre respostas para as dúvidas mais comuns sobre o Marcenaria 365.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))
            }

            items(perguntas) { pergunta ->
                var expandida by remember(pergunta.titulo) {
                    mutableStateOf(false)
                }

                Card(
                    onClick = { expandida = !expandida },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = pergunta.titulo,
                            fontWeight = FontWeight.SemiBold,
                            color = Azul,
                            fontSize = 14.sp
                        )

                        if (expandida) {
                            Spacer(Modifier.height(10.dp))

                            HorizontalDivider(
                                color = Fundo
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = pergunta.resposta,
                                color = Color.DarkGray,
                                fontSize = 13.sp
                            )
                        } else {
                            Spacer(Modifier.height(5.dp))

                            Text(
                                text = "Toque para ver a resposta",
                                color = Marrom,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
