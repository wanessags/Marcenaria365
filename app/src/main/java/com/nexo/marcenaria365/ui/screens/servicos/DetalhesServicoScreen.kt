
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.ChevronRight
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

private val FundoServico = Color(0xFFFAF6F0)
private val AzulServico = Color(0xFF193447)
private val CobreServico = Color(0xFFBD906E)
private val CinzaServico = Color(0xFF929292)
private val VerdeServico = Color(0xFF239B7A)
private val VermelhoServico = Color(0xFFE26666)
private val BordaServico = Color(0xFFE9E3DD)

@Composable
fun DetalhesServicoScreen(
    servico: ServicoUi,
    orcamento: OrcamentoUi?,
    onVoltar: () -> Unit,
    onAbrirOrcamento: () -> Unit,
    onRegistrarRecebimento: (Double) -> Unit
) {
    var aba by remember {
        mutableStateOf("Detalhes")
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

    BackHandler(enabled = !mostrarRecebimento) {
        onVoltar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoServico)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(AzulServico)
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
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
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

            // Resumo do móvel
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Color(0xFFEDE4DA),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Chair,
                        contentDescription = null,
                        tint = CobreServico,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = servico.titulo,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulServico
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = servico.clienteNome,
                        fontSize = 13.sp,
                        color = CinzaServico
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = when (servico.status) {
                            "Concluído" -> Color(0xFFE4F5ED)
                            "Em produção" -> Color(0xFFFFEFD8)
                            else -> Color(0xFFF2EAE0)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = servico.status,
                            color = when (servico.status) {
                                "Concluído" -> VerdeServico
                                "Em produção" -> CobreServico
                                else -> AzulServico
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Abas
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
                    val selecionado = aba == item

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selecionado) {
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
                            color = AzulServico,
                            fontSize = 12.sp,
                            fontWeight = if (selecionado) {
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

                // DETALHES DO SERVIÇO
                "Detalhes" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = "Informações gerais",
                                color = AzulServico,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(Modifier.height(23.dp))

                            LinhaInformacaoServico(
                                titulo = "Cliente",
                                valor = servico.clienteNome
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    vertical = 18.dp
                                ),
                                color = BordaServico
                            )

                            LinhaInformacaoServico(
                                titulo = "Prazo de entrega",
                                valor = servico.entrega
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    vertical = 18.dp
                                ),
                                color = BordaServico
                            )

                            LinhaInformacaoServico(
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
                            modifier = Modifier.padding(17.dp)
                        ) {
                            Text(
                                text = "Financeiro",
                                color = AzulServico,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(19.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                InfoFinanceiraServico(
                                    titulo = "Total",
                                    valor = moedaServico(
                                        servico.valorEstimado
                                    ),
                                    cor = AzulServico,
                                    modifier = Modifier.weight(1f)
                                )

                                InfoFinanceiraServico(
                                    titulo = "Recebido",
                                    valor = moedaServico(
                                        servico.valorRecebido
                                    ),
                                    cor = VerdeServico,
                                    modifier = Modifier.weight(1f)
                                )

                                InfoFinanceiraServico(
                                    titulo = "Saldo",
                                    valor = moedaServico(saldo),
                                    cor = VermelhoServico,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // ORÇAMENTO VINCULADO AO SERVIÇO
                "Orçamento" -> {
                    if (orcamento == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = CobreServico,
                                    modifier = Modifier.size(38.dp)
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Nenhum orçamento cadastrado",
                                    color = AzulServico,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Crie um orçamento com materiais, " +
                                            "mão de obra e condições de pagamento.",
                                    color = CinzaServico,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(22.dp))

                                Button(
                                    onClick = onAbrirOrcamento,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(11.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AzulServico,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Criar orçamento",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Orçamento #${
                                        formatarNumeroOrcamento(
                                            orcamento.id
                                        )
                                    }",
                                    color = AzulServico,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = orcamento.status,
                                    color = CobreServico,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(17.dp))

                                HorizontalDivider(
                                    color = BordaServico
                                )

                                Spacer(Modifier.height(17.dp))

                                Text(
                                    text = "Itens cadastrados",
                                    color = CinzaServico,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "${orcamento.itens.size} item(ns)",
                                    color = AzulServico,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "Valor do orçamento",
                                    color = CinzaServico,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = formatarMoedaOrcamento(
                                        orcamento.total
                                    ),
                                    color = AzulServico,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(20.dp))

                                Button(
                                    onClick = onAbrirOrcamento,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(49.dp),
                                    shape = RoundedCornerShape(11.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AzulServico,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Ver orçamento",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // HISTÓRICO
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
                                color = AzulServico,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Total recebido",
                                color = CinzaServico,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(7.dp))

                            Text(
                                text = moedaServico(
                                    servico.valorRecebido
                                ),
                                color = VerdeServico,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
        }

        // Registrar movimentação
        Button(
            onClick = {
                valorRecebimento = ""
                erro = ""
                mostrarRecebimento = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
                .height(53.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulServico,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Registrar movimentação",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (mostrarRecebimento) {
        AlertDialog(
            onDismissRequest = {
                mostrarRecebimento = false
            },
            title = {
                Text(
                    text = "Registrar recebimento",
                    color = AzulServico,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Saldo disponível: ${
                            moedaServico(saldo)
                        }",
                        color = CinzaServico,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(14.dp))

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

                    if (erro.isNotEmpty()) {
                        Spacer(Modifier.height(9.dp))

                        Text(
                            text = erro,
                            color = VermelhoServico,
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
                                erro = "O valor não pode superar o saldo."
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
                        color = AzulServico
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
                        color = CinzaServico
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun LinhaInformacaoServico(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                color = AzulServico,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = valor,
                color = CinzaServico,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = CinzaServico,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun InfoFinanceiraServico(
    titulo: String,
    valor: String,
    cor: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = titulo,
            color = CinzaServico,
            fontSize = 10.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = valor,
            color = cor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
    }
}
