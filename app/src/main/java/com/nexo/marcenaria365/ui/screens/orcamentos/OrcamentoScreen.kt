
package com.nexo.marcenaria365.ui.screens.servicos.orcamentos

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Vermelho = Color(0xFFB34D4D)

@Composable
fun OrcamentoScreen(
    orcamento: OrcamentoUi,
    onVoltar: () -> Unit,
    onExcluir: (Long) -> Unit
) {
    var confirmarExclusao by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
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
                text = "Orçamento",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Orçamento #${formatarNumeroOrcamento(orcamento.id)}",
                        color = Azul,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = orcamento.clienteNome,
                        color = Cinza,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    color = when (orcamento.status) {
                        StatusOrcamento.APROVADO ->
                            Color(0xFFE5F4EC)
                        StatusOrcamento.RECUSADO ->
                            Color(0xFFF9E6E4)
                        else ->
                            Color(0xFFFFEFD8)
                    },
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Text(
                        text = orcamento.status,
                        color = when (orcamento.status) {
                            StatusOrcamento.APROVADO ->
                                Color(0xFF239B7A)
                            StatusOrcamento.RECUSADO ->
                                Vermelho
                            else -> Cobre
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 7.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Itens do orçamento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Azul
            )

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    orcamento.itens.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        Color(0xFFF6E8D8),
                                        RoundedCornerShape(9.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = Cobre,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.descricao,
                                    color = Azul,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = item.detalhe,
                                    color = Cinza,
                                    fontSize = 11.sp
                                )

                                if (item.quantidade > 1) {
                                    Text(
                                        text = "Quantidade: ${item.quantidade}",
                                        color = Cinza,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = formatarMoedaOrcamento(
                                    item.subtotal
                                ),
                                color = Azul,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF6E8D8),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 17.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = Cobre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatarMoedaOrcamento(orcamento.total),
                    color = Cobre,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Condições de pagamento",
                color = Azul,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Entrada (${orcamento.percentualEntrada}%)",
                                color = Azul,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Na aprovação do orçamento",
                                color = Cinza,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = formatarMoedaOrcamento(
                                orcamento.entrada
                            ),
                            color = Azul,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Saldo (${100 - orcamento.percentualEntrada}%)",
                                color = Azul,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Na entrega do serviço",
                                color = Cinza,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = formatarMoedaOrcamento(
                                orcamento.saldo
                            ),
                            color = Azul,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (orcamento.observacoes.isNotBlank()) {
                Spacer(Modifier.height(22.dp))

                Text(
                    text = "Observações",
                    color = Azul,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = orcamento.observacoes,
                    color = Cinza,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(25.dp))
        }

        // Botões inferiores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Azul,
                    containerColor = Color.White
                )
            ) {
                Text(
                    "Excluir",
                    color = Azul,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    val texto = buildString {
                        appendLine(
                            "ORÇAMENTO #${formatarNumeroOrcamento(orcamento.id)}"
                        )
                        appendLine("Cliente: ${orcamento.clienteNome}")
                        appendLine()

                        orcamento.itens.forEach { item ->
                            appendLine(
                                "${item.descricao} (${item.quantidade}x): " +
                                        formatarMoedaOrcamento(item.subtotal)
                            )
                        }

                        appendLine()
                        appendLine(
                            "Total: ${formatarMoedaOrcamento(orcamento.total)}"
                        )
                        appendLine(
                            "Entrada: ${formatarMoedaOrcamento(orcamento.entrada)}"
                        )
                        appendLine(
                            "Saldo: ${formatarMoedaOrcamento(orcamento.saldo)}"
                        )
                    }

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, texto)
                    }

                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "Enviar orçamento"
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Azul,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Enviar",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },
            title = {
                Text(
                    "Excluir orçamento?",
                    color = Azul,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Deseja realmente excluir o orçamento " +
                            "#${formatarNumeroOrcamento(orcamento.id)}? " +
                            "Essa ação não poderá ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluir(orcamento.id)
                    }
                ) {
                    Text(
                        "Excluir",
                        color = Vermelho,
                        fontWeight = FontWeight.Bold
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
                        "Cancelar",
                        color = Azul
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
