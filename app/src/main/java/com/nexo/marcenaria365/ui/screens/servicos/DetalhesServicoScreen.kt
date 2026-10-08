
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.formatarMoedaOrcamento
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.formatarNumeroOrcamento

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Borda = Color(0xFFE9E3DD)
private val Verde = Color(0xFF239B7A)
private val Vermelho = Color(0xFFB34D4D)

@Composable
fun DetalhesServicoScreen(
    servico: ServicoUi,
    orcamento: OrcamentoUi?,
    onVoltar: () -> Unit,
    onAbrirOrcamento: () -> Unit,
    onRegistrarRecebimento: (Double) -> Unit,
    onExcluirServico: (Long) -> Unit
) {
    var aba by remember { mutableStateOf("Detalhes") }
    var confirmarExclusao by remember { mutableStateOf(false) }
    var mostrarRecebimento by remember { mutableStateOf(false) }
    var valorRecebimento by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }

    val saldo = (
            servico.valorEstimado - servico.valorRecebido
            ).coerceAtLeast(0.0)

    BackHandler(
        enabled = !confirmarExclusao && !mostrarRecebimento,
        onBack = onVoltar
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(Azul)
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
                text = "Detalhes do serviço",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exibe a foto selecionada no cadastro.
                FotoServico(
                    fotoUri = servico.fotoUri,
                    modifier = Modifier.size(82.dp)
                )

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = servico.titulo,
                        color = Azul,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = servico.clienteNome,
                        color = Cinza,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = when (servico.status) {
                            "Concluído" -> Color(0xFFE4F5ED)
                            "Em produção" -> Color(0xFFFFEFD8)
                            else -> Color(0xFFF2EAE0)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = servico.status,
                            color = when (servico.status) {
                                "Concluído" -> Verde
                                "Em produção" -> Cobre
                                else -> Azul
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(
                                horizontal = 7.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    "Detalhes",
                    "Orçamento",
                    "Histórico"
                ).forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (aba == item) {
                                    Color(0xFFF6E8D8)
                                } else {
                                    Color.Transparent
                                },
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                aba = item
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            color = Azul,
                            fontSize = 12.sp,
                            fontWeight = if (aba == item) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            when (aba) {
                "Detalhes" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                text = "Informações gerais",
                                color = Azul,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            CampoDetalhe(
                                titulo = "Cliente",
                                valor = servico.clienteNome
                            )

                            HorizontalDivider(color = Borda)

                            CampoDetalhe(
                                titulo = "Prazo de entrega",
                                valor = servico.entrega
                            )

                            HorizontalDivider(color = Borda)

                            CampoDetalhe(
                                titulo = "Descrição",
                                valor = servico.descricao
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Financeiro",
                                color = Azul,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            ResumoValor(
                                titulo = "Valor estimado",
                                valor = moedaServico(
                                    servico.valorEstimado
                                ),
                                cor = Azul
                            )

                            HorizontalDivider(color = Borda)

                            ResumoValor(
                                titulo = "Recebido",
                                valor = moedaServico(
                                    servico.valorRecebido
                                ),
                                cor = Verde
                            )

                            HorizontalDivider(color = Borda)

                            ResumoValor(
                                titulo = "Saldo",
                                valor = moedaServico(saldo),
                                cor = Vermelho
                            )
                        }
                    }
                }

                "Orçamento" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = Cobre,
                                modifier = Modifier.size(35.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            if (orcamento == null) {
                                Text(
                                    text = "Nenhum orçamento cadastrado",
                                    color = Azul,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Cadastre materiais, mão de obra " +
                                            "e condições de pagamento.",
                                    color = Cinza,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Orçamento #${
                                        formatarNumeroOrcamento(
                                            orcamento.id
                                        )
                                    }",
                                    color = Azul,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = orcamento.status,
                                    color = Cobre,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(17.dp))

                                Text(
                                    text = formatarMoedaOrcamento(
                                        orcamento.total
                                    ),
                                    color = Azul,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = onAbrirOrcamento,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(49.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Azul
                                )
                            ) {
                                Text(
                                    text = if (orcamento == null) {
                                        "Criar orçamento"
                                    } else {
                                        "Ver orçamento"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Histórico financeiro",
                                color = Azul,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Total recebido",
                                color = Cinza,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = moedaServico(
                                    servico.valorRecebido
                                ),
                                color = Verde,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = {
                    valorRecebimento = ""
                    erro = ""
                    mostrarRecebimento = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Azul,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Registrar movimentação",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(9.dp))

            TextButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Vermelho
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = Vermelho,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir serviço",
                    color = Vermelho,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(25.dp))
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },
            title = {
                Text(
                    text = "Excluir serviço?",
                    color = Azul,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja excluir \"${servico.titulo}\"? " +
                            "O orçamento vinculado também será excluído. " +
                            "Essa ação não poderá ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluirServico(servico.id)
                    }
                ) {
                    Text(
                        text = "Excluir",
                        color = Vermelho,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = Azul
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    if (mostrarRecebimento) {
        AlertDialog(
            onDismissRequest = {
                mostrarRecebimento = false
            },
            title = {
                Text(
                    text = "Registrar recebimento",
                    color = Azul,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Saldo: ${moedaServico(saldo)}",
                        color = Cinza,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = valorRecebimento,
                        onValueChange = {
                            valorRecebimento = it
                            erro = ""
                        },
                        label = {
                            Text("Valor recebido (R$)")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )

                    if (erro.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = erro,
                            color = Vermelho,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val valor = valorRecebimento
                            .replace(".", "")
                            .replace(",", ".")
                            .replace("R$", "")
                            .trim()
                            .toDoubleOrNull()

                        when {
                            valor == null || valor <= 0.0 -> {
                                erro = "Informe um valor válido."
                            }

                            valor > saldo -> {
                                erro = "O valor supera o saldo."
                            }

                            else -> {
                                onRegistrarRecebimento(valor)
                                mostrarRecebimento = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Registrar",
                        color = Azul
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarRecebimento = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = Cinza
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun CampoDetalhe(
    titulo: String,
    valor: String
) {
    Column {
        Text(
            text = titulo,
            color = Azul,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = valor,
            color = Cinza,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ResumoValor(
    titulo: String,
    valor: String,
    cor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = Cinza,
            fontSize = 12.sp
        )

        Text(
            text = valor,
            color = cor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
