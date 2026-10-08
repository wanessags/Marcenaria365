
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Verde = Color(0xFF239B7A)
private val Vermelho = Color(0xFFE26666)

@Composable
fun DetalhesServicoScreen(
    servico: ServicoUi,
    onVoltar: () -> Unit,
    onRegistrarRecebimento: (Double) -> Unit
) {
    var aba by remember { mutableStateOf("Detalhes") }
    var mostrarRecebimento by remember { mutableStateOf(false) }
    var valorRecebimento by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }

    val saldo = (servico.valorEstimado - servico.valorRecebido)
        .coerceAtLeast(0.0)

    BackHandler(onBack = onVoltar)

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
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Text(
                "Detalhes do serviço",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(91.dp)
                        .background(
                            Color(0xFFEDE4DA),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Chair,
                        contentDescription = null,
                        tint = Cobre,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(Modifier.width(13.dp))

                Column {
                    Text(
                        servico.titulo,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Azul
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        servico.clienteNome,
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
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            servico.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (servico.status) {
                                "Concluído" -> Verde
                                "Em produção" -> Cobre
                                else -> Azul
                            },
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Detalhes", "Orçamento", "Histórico")
                    .forEach { item ->
                        val selecionado = aba == item

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selecionado) Color(0xFFF6E8D8)
                                    else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { aba = item }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                item,
                                color = Azul,
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
                "Detalhes" -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                "Informações gerais",
                                color = Azul,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(23.dp))

                            LinhaDetalhe(
                                titulo = "Cliente",
                                valor = servico.clienteNome
                            )

                            Spacer(Modifier.height(28.dp))

                            LinhaDetalhe(
                                titulo = "Prazo de entrega",
                                valor = servico.entrega
                            )

                            Spacer(Modifier.height(28.dp))

                            LinhaDetalhe(
                                titulo = "Descrição",
                                valor = servico.descricao
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(17.dp)
                        ) {
                            Text(
                                "Financeiro",
                                color = Azul,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(19.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InfoFinanceira(
                                    "Total",
                                    moedaServico(servico.valorEstimado),
                                    Azul,
                                    Modifier.weight(1f)
                                )

                                InfoFinanceira(
                                    "Recebido",
                                    moedaServico(servico.valorRecebido),
                                    Verde,
                                    Modifier.weight(1f)
                                )

                                InfoFinanceira(
                                    "Saldo",
                                    moedaServico(saldo),
                                    Vermelho,
                                    Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                "Orçamento" -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                "Orçamento do serviço",
                                color = Azul,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(Modifier.height(20.dp))

                            Text("Descrição", color = Cinza, fontSize = 12.sp)

                            Spacer(Modifier.height(5.dp))

                            Text(servico.descricao, color = Azul)

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 18.dp)
                            )

                            Text(
                                "Valor estimado",
                                color = Cinza,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(5.dp))

                            Text(
                                moedaServico(servico.valorEstimado),
                                color = Azul,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                else -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                "Histórico financeiro",
                                color = Azul,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(Modifier.height(15.dp))

                            Text(
                                "Total recebido até o momento:",
                                color = Cinza,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(7.dp))

                            Text(
                                moedaServico(servico.valorRecebido),
                                color = Verde,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "O registro individual das movimentações será " +
                                        "incluído na etapa da Carteira.",
                                color = Cinza,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
        }

        Button(
            onClick = {
                valorRecebimento = ""
                erro = ""
                mostrarRecebimento = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul,
                contentColor = Color.White
            )
        ) {
            Text(
                "Registrar movimentação",
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
                    "Registrar recebimento",
                    color = Azul,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Saldo disponível: ${moedaServico(saldo)}",
                        color = Cinza,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = valorRecebimento,
                        onValueChange = {
                            valorRecebimento = it
                            erro = ""
                        },
                        label = { Text("Valor recebido (R$)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )

                    if (erro.isNotEmpty()) {
                        Spacer(Modifier.height(9.dp))

                        Text(
                            erro,
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
                            .toDoubleOrNull()

                        when {
                            valor == null || valor <= 0.0 ->
                                erro = "Informe um valor válido."

                            valor > saldo ->
                                erro = "O valor não pode superar o saldo."

                            else -> {
                                onRegistrarRecebimento(valor)
                                mostrarRecebimento = false
                            }
                        }
                    }
                ) {
                    Text("Registrar", color = Azul)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarRecebimento = false }
                ) {
                    Text("Cancelar", color = Cinza)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun LinhaDetalhe(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                titulo,
                color = Azul,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                valor,
                color = Cinza,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Cinza,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun InfoFinanceira(
    titulo: String,
    valor: String,
    cor: Color,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            titulo,
            color = Cinza,
            fontSize = 10.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            valor,
            color = cor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 2
        )
    }
}
