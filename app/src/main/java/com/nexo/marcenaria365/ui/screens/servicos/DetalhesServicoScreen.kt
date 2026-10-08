
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
import androidx.compose.material.icons.outlined.Edit
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

private val FundoDetalhes = Color(0xFFFAF6F0)
private val AzulDetalhes = Color(0xFF193447)
private val CobreDetalhes = Color(0xFFBD906E)
private val CinzaDetalhes = Color(0xFF929292)
private val BordaDetalhes = Color(0xFFE9E3DD)
private val VerdeDetalhes = Color(0xFF239B7A)
private val VermelhoDetalhes = Color(0xFFB34D4D)

@Composable
fun DetalhesServicoScreen(
    servico: ServicoUi,
    orcamento: OrcamentoUi?,
    onVoltar: () -> Unit,
    onAbrirOrcamento: () -> Unit,
    onRegistrarRecebimento: (Double) -> Unit,
    onExcluirServico: (Long) -> Unit,
    onEditarServico: () -> Unit = {}
) {
    var aba by remember {
        mutableStateOf("Detalhes")
    }

    var confirmarExclusao by remember {
        mutableStateOf(false)
    }

    var mostrarRecebimento by remember {
        mutableStateOf(false)
    }

    var valorRecebimento by remember {
        mutableStateOf("")
    }

    var erro by remember {
        mutableStateOf("")
    }

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
            .background(FundoDetalhes)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // CABEÇALHO COM BOTÃO DE EDIÇÃO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(AzulDetalhes)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Text(
                text = "Detalhes do serviço",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onEditarServico) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar serviço",
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            // FOTO, NOME, CLIENTE E STATUS
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        color = AzulDetalhes,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = servico.clienteNome,
                        color = CinzaDetalhes,
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
                                "Concluído" -> VerdeDetalhes
                                "Em produção" -> CobreDetalhes
                                else -> AzulDetalhes
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

            // ABAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(5.dp)
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
                            color = AzulDetalhes,
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
                            verticalArrangement =
                                Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                text = "Informações gerais",
                                color = AzulDetalhes,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            CampoDetalheServico(
                                titulo = "Cliente",
                                valor = servico.clienteNome
                            )

                            HorizontalDivider(
                                color = BordaDetalhes
                            )

                            CampoDetalheServico(
                                titulo = "Prazo de entrega",
                                valor = servico.entrega
                            )

                            HorizontalDivider(
                                color = BordaDetalhes
                            )

                            CampoDetalheServico(
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
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Financeiro",
                                color = AzulDetalhes,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            ResumoValorServico(
                                titulo = "Valor estimado",
                                valor = moedaServico(
                                    servico.valorEstimado
                                ),
                                cor = AzulDetalhes
                            )

                            HorizontalDivider(
                                color = BordaDetalhes
                            )

                            ResumoValorServico(
                                titulo = "Recebido",
                                valor = moedaServico(
                                    servico.valorRecebido
                                ),
                                cor = VerdeDetalhes
                            )

                            HorizontalDivider(
                                color = BordaDetalhes
                            )

                            ResumoValorServico(
                                titulo = "Saldo",
                                valor = moedaServico(saldo),
                                cor = VermelhoDetalhes
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
                                imageVector =
                                    Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = CobreDetalhes,
                                modifier = Modifier.size(35.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            if (orcamento == null) {
                                Text(
                                    text = "Nenhum orçamento cadastrado",
                                    color = AzulDetalhes,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Cadastre materiais, mão de obra " +
                                            "e condições de pagamento.",
                                    color = CinzaDetalhes,
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
                                    color = AzulDetalhes,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = orcamento.status,
                                    color = CobreDetalhes,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(17.dp))

                                Text(
                                    text = formatarMoedaOrcamento(
                                        orcamento.total
                                    ),
                                    color = AzulDetalhes,
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
                                    containerColor = AzulDetalhes
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
                                color = AzulDetalhes,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Total recebido",
                                color = CinzaDetalhes,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = moedaServico(
                                    servico.valorRecebido
                                ),
                                color = VerdeDetalhes,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            // RECEBIMENTOS
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
                    containerColor = AzulDetalhes,
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

            // EXCLUSÃO
            TextButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VermelhoDetalhes
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = VermelhoDetalhes,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir serviço",
                    color = VermelhoDetalhes,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(25.dp))
        }
    }

    // CONFIRMAÇÃO DE EXCLUSÃO
    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },
            title = {
                Text(
                    text = "Excluir serviço?",
                    color = AzulDetalhes,
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
                        color = VermelhoDetalhes,
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
                        color = AzulDetalhes
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // REGISTRAR RECEBIMENTO
    if (mostrarRecebimento) {
        AlertDialog(
            onDismissRequest = {
                mostrarRecebimento = false
            },
            title = {
                Text(
                    text = "Registrar recebimento",
                    color = AzulDetalhes,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Saldo: ${moedaServico(saldo)}",
                        color = CinzaDetalhes,
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
                            color = VermelhoDetalhes,
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
                        color = AzulDetalhes
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
                        color = CinzaDetalhes
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun CampoDetalheServico(
    titulo: String,
    valor: String
) {
    Column {
        Text(
            text = titulo,
            color = AzulDetalhes,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = valor,
            color = CinzaDetalhes,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ResumoValorServico(
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
            color = CinzaDetalhes,
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
