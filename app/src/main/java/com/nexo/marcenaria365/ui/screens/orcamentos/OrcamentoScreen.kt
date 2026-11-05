
package com.nexo.marcenaria365.ui.screens.servicos.orcamentos

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoOrcamento = Color(0xFFFAF6F0)
private val AzulOrcamento = Color(0xFF193447)
private val CobreOrcamento = Color(0xFF94613C)
private val CinzaOrcamento = Color(0xFF53616A)
private val BordaOrcamento = Color(0xFF89969E)
private val VermelhoOrcamento = Color(0xFFB3261E)
private val VerdeOrcamento = Color(0xFF237A58)

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

    BackHandler(
        enabled = !confirmarExclusao,
        onBack = onVoltar
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOrcamento)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // CABEÇALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .background(AzulOrcamento)
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
                    contentDescription = "Voltar aos detalhes do serviço",
                    tint = Color.White
                )
            }

            Text(
                text = "Orçamento",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        heading()
                    }
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

            // IDENTIFICAÇÃO E SITUAÇÃO
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Orçamento #${
                        formatarNumeroOrcamento(orcamento.id)
                    }",
                    color = AzulOrcamento,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        heading()
                    }
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Cliente: ${orcamento.clienteNome}",
                    color = CinzaOrcamento,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(12.dp))

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
                        text = "Situação: ${orcamento.status}",
                        color = when (orcamento.status) {
                            StatusOrcamento.APROVADO ->
                                VerdeOrcamento

                            StatusOrcamento.RECUSADO ->
                                VermelhoOrcamento

                            else ->
                                Color(0xFF825B12)
                        },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ITENS DO ORÇAMENTO
            TituloSecaoOrcamento(
                "Itens do orçamento"
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        16.dp
                    )
                ) {
                    if (orcamento.itens.isEmpty()) {
                        Text(
                            text = "Nenhum item cadastrado.",
                            color = CinzaOrcamento,
                            fontSize = 14.sp
                        )
                    }

                    orcamento.itens.forEachIndexed { indice, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(
                                    mergeDescendants = true
                                ) {
                                    contentDescription =
                                        buildString {
                                            append(
                                                "Item ${indice + 1}: "
                                            )
                                            append(item.descricao)
                                            append(". ")

                                            if (
                                                item.detalhe.isNotBlank()
                                            ) {
                                                append(item.detalhe)
                                                append(". ")
                                            }

                                            append(
                                                "Quantidade: ${
                                                    item.quantidade
                                                }. "
                                            )
                                            append(
                                                "Valor unitário: ${
                                                    formatarMoedaOrcamento(
                                                        item.valorUnitario
                                                    )
                                                }. "
                                            )
                                            append(
                                                "Subtotal: ${
                                                    formatarMoedaOrcamento(
                                                        item.subtotal
                                                    )
                                                }."
                                            )
                                        }
                                },
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
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
                                    tint = CobreOrcamento,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.descricao,
                                    color = AzulOrcamento,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (item.detalhe.isNotBlank()) {
                                    Spacer(Modifier.height(5.dp))

                                    Text(
                                        text = item.detalhe,
                                        color = CinzaOrcamento,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "Quantidade: ${
                                        item.quantidade
                                    }",
                                    color = CinzaOrcamento,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Unitário: ${
                                        formatarMoedaOrcamento(
                                            item.valorUnitario
                                        )
                                    }",
                                    color = CinzaOrcamento,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(7.dp))

                                Text(
                                    text = "Subtotal: ${
                                        formatarMoedaOrcamento(
                                            item.subtotal
                                        )
                                    }",
                                    color = AzulOrcamento,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (indice < orcamento.itens.lastIndex) {
                            HorizontalDivider(
                                color = BordaOrcamento
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // TOTAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF6E8D8),
                        RoundedCornerShape(12.dp)
                    )
                    .semantics(
                        mergeDescendants = true
                    ) {
                        contentDescription =
                            "Total do orçamento: ${
                                formatarMoedaOrcamento(
                                    orcamento.total
                                )
                            }"
                    }
                    .padding(
                        horizontal = 16.dp,
                        vertical = 18.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = AzulOrcamento,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = formatarMoedaOrcamento(
                        orcamento.total
                    ),
                    color = AzulOrcamento,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(27.dp))

            // CONDIÇÕES DE PAGAMENTO
            TituloSecaoOrcamento(
                "Condições de pagamento"
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
                    verticalArrangement = Arrangement.spacedBy(
                        18.dp
                    )
                ) {
                    LinhaPagamentoOrcamento(
                        titulo = "Entrada (${
                            orcamento.percentualEntrada
                        }%)",
                        descricao = "Na aprovação do orçamento",
                        valor = formatarMoedaOrcamento(
                            orcamento.entrada
                        )
                    )

                    HorizontalDivider(
                        color = BordaOrcamento
                    )

                    LinhaPagamentoOrcamento(
                        titulo = "Saldo (${
                            100 - orcamento.percentualEntrada
                        }%)",
                        descricao = "Na entrega do serviço",
                        valor = formatarMoedaOrcamento(
                            orcamento.saldo
                        )
                    )
                }
            }

            // OBSERVAÇÕES
            if (orcamento.observacoes.isNotBlank()) {
                Spacer(Modifier.height(24.dp))

                TituloSecaoOrcamento(
                    "Observações"
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = orcamento.observacoes,
                        color = CinzaOrcamento,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
        }

        // BOTÕES INFERIORES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                10.dp
            )
        ) {
            OutlinedButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    VermelhoOrcamento
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VermelhoOrcamento,
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Excluir",
                    color = VermelhoOrcamento,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    // Preserva o compartilhamento atual
                    // utilizando o menu do Android.
                    val texto = buildString {
                        appendLine(
                            "ORÇAMENTO #${
                                formatarNumeroOrcamento(
                                    orcamento.id
                                )
                            }"
                        )

                        appendLine(
                            "Cliente: ${orcamento.clienteNome}"
                        )

                        appendLine(
                            "Situação: ${orcamento.status}"
                        )

                        appendLine()

                        orcamento.itens.forEach { item ->
                            appendLine(
                                "${item.descricao} " +
                                        "(${item.quantidade}x): " +
                                        formatarMoedaOrcamento(
                                            item.subtotal
                                        )
                            )
                        }

                        appendLine()

                        appendLine(
                            "Total: ${
                                formatarMoedaOrcamento(
                                    orcamento.total
                                )
                            }"
                        )

                        appendLine(
                            "Entrada: ${
                                formatarMoedaOrcamento(
                                    orcamento.entrada
                                )
                            }"
                        )

                        appendLine(
                            "Saldo: ${
                                formatarMoedaOrcamento(
                                    orcamento.saldo
                                )
                            }"
                        )

                        if (
                            orcamento.observacoes.isNotBlank()
                        ) {
                            appendLine()
                            appendLine(
                                "Observações: ${
                                    orcamento.observacoes
                                }"
                            )
                        }
                    }

                    val intent = Intent(
                        Intent.ACTION_SEND
                    ).apply {
                        type = "text/plain"

                        putExtra(
                            Intent.EXTRA_TEXT,
                            texto
                        )
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
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulOrcamento,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Enviar",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                    text = "Excluir orçamento?",
                    color = AzulOrcamento,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Deseja excluir o orçamento " +
                                "#${
                                    formatarNumeroOrcamento(
                                        orcamento.id
                                    )
                                }?",
                        color = CinzaOrcamento,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Text(
                        text = "Cliente: ${
                            orcamento.clienteNome
                        }",
                        color = AzulOrcamento,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "Total: ${
                            formatarMoedaOrcamento(
                                orcamento.total
                            )
                        }",
                        color = AzulOrcamento,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Essa ação não poderá ser desfeita.",
                        color = VermelhoOrcamento,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluir(orcamento.id)
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Excluir definitivamente",
                        color = VermelhoOrcamento,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulOrcamento,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun TituloSecaoOrcamento(
    titulo: String
) {
    Text(
        text = titulo,
        color = AzulOrcamento,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

@Composable
private fun LinhaPagamentoOrcamento(
    titulo: String,
    descricao: String,
    valor: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(
                mergeDescendants = true
            ) {
                contentDescription =
                    "$titulo. $descricao. Valor: $valor."
            }
    ) {
        Text(
            text = titulo,
            color = AzulOrcamento,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = descricao,
            color = CinzaOrcamento,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = valor,
            color = AzulOrcamento,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
