
package com.nexo.marcenaria365.ui.screens.ajuda

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoAjuda = Color(0xFFFAF6F0)
private val AzulAjuda = Color(0xFF193447)
private val CobreAjuda = Color(0xFF94613C)
private val CinzaAjuda = Color(0xFF53616A)
private val BordaAjuda = Color(0xFF89969E)

private data class PerguntaAjuda(
    val titulo: String,
    val resposta: String
)

private val perguntasAjuda = listOf(
    PerguntaAjuda(
        titulo = "Como cadastrar um cliente?",
        resposta = "Acesse Clientes e utilize a opção de cadastro. " +
                "Informe os dados necessários para identificar e " +
                "contatar o cliente."
    ),
    PerguntaAjuda(
        titulo = "Como cadastrar um serviço?",
        resposta = "Acesse Serviços, escolha cadastrar e preencha " +
                "as informações do trabalho, incluindo cliente, " +
                "descrição e prazo."
    ),
    PerguntaAjuda(
        titulo = "Como criar um orçamento?",
        resposta = "Na área de orçamentos, selecione o cliente e " +
                "informe os itens, materiais, mão de obra e prazo."
    ),
    PerguntaAjuda(
        titulo = "Como registrar um recebimento?",
        resposta = "Acesse a área de Caixa ou recebimentos, " +
                "selecione o serviço correspondente e registre " +
                "o valor recebido."
    ),
    PerguntaAjuda(
        titulo = "Como controlar os materiais?",
        resposta = "Acesse Mais > Materiais. Você poderá " +
                "cadastrar materiais, consultar o estoque, " +
                "editar informações e excluir registros."
    ),
    PerguntaAjuda(
        titulo = "Onde encontro meus dados?",
        resposta = "Acesse Mais > Meu perfil ou " +
                "Mais > Configurações para consultar " +
                "os dados da conta."
    ),
    PerguntaAjuda(
        titulo = "Como sair do aplicativo?",
        resposta = "Acesse Mais e escolha Sair da conta. " +
                "Confirme para retornar à tela de login."
    )
)

@Composable
fun AjudaSuporteScreen(
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FundoAjuda,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulAjuda)
                    .statusBarsPadding()
                    .heightIn(min = 62.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar para Mais",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "Ajuda e suporte",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.semantics {
                        heading()
                    }
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(
                12.dp
            ),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 32.dp
            )
        ) {

            // APRESENTAÇÃO
            item {
                Column {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = CobreAjuda,
                            modifier = Modifier.size(27.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "Como podemos ajudar?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulAjuda,
                            modifier = Modifier.semantics {
                                heading()
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Encontre respostas para as " +
                                "dúvidas mais comuns sobre o " +
                                "Marcenaria 365.",
                        color = CinzaAjuda,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Selecione uma pergunta para " +
                                "mostrar ou ocultar a resposta.",
                        color = CinzaAjuda,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            // PERGUNTAS FREQUENTES
            items(
                items = perguntasAjuda,
                key = { it.titulo }
            ) { pergunta ->

                var expandida by rememberSaveable(
                    pergunta.titulo
                ) {
                    mutableStateOf(false)
                }

                var focado by remember {
                    mutableStateOf(false)
                }

                Card(
                    onClick = {
                        expandida = !expandida
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            focado = it.isFocused
                        }
                        .then(
                            if (focado) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = AzulAjuda,
                                    shape = RoundedCornerShape(
                                        14.dp
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                        .semantics {
                            stateDescription = if (
                                expandida
                            ) {
                                "Expandida. Toque para recolher."
                            } else {
                                "Recolhida. Toque para expandir."
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (expandida) {
                            CobreAjuda
                        } else {
                            BordaAjuda
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = pergunta.titulo,
                                fontWeight =
                                    FontWeight.SemiBold,
                                color = AzulAjuda,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.weight(
                                    1f
                                )
                            )

                            Spacer(
                                Modifier.width(10.dp)
                            )

                            Icon(
                                imageVector = if (
                                    expandida
                                ) {
                                    Icons.Outlined.ExpandLess
                                } else {
                                    Icons.Outlined.ExpandMore
                                },
                                contentDescription = null,
                                tint = AzulAjuda,
                                modifier = Modifier.size(
                                    24.dp
                                )
                            )
                        }

                        if (expandida) {
                            Spacer(
                                Modifier.height(10.dp)
                            )

                            HorizontalDivider(
                                color = BordaAjuda,
                                thickness = 1.dp
                            )

                            Spacer(
                                Modifier.height(12.dp)
                            )

                            Text(
                                text = pergunta.resposta,
                                color = CinzaAjuda,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        } else {
                            Spacer(
                                Modifier.height(5.dp)
                            )

                            Text(
                                text = "Mostrar resposta",
                                color = CobreAjuda,
                                fontSize = 13.sp,
                                fontWeight =
                                    FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
