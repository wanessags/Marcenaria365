
package com.nexo.marcenaria365.ui.screens.servicos.orcamentos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Borda = Color(0xFFE9E3DD)
private val Vermelho = Color(0xFFB34D4D)

@Composable
fun NovoOrcamentoScreen(
    servico: ServicoUi,
    onVoltar: () -> Unit,
    onSalvar: (OrcamentoUi) -> Unit
) {
    val itens = remember(servico.id) {
        mutableStateListOf<ItemOrcamentoUi>()
    }

    var percentualEntrada by rememberSaveable(servico.id) {
        mutableStateOf("50")
    }

    var observacoes by rememberSaveable(servico.id) {
        mutableStateOf("")
    }

    var mostrarNovoItem by remember {
        mutableStateOf(false)
    }

    var erro by remember {
        mutableStateOf("")
    }

    val total = itens.sumOf { it.subtotal }
    val percentual = percentualEntrada.toIntOrNull() ?: 0
    val entrada = total * percentual.coerceIn(0, 100) / 100.0
    val saldo = total - entrada

    BackHandler(onBack = onVoltar)

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = Azul,
        unfocusedTextColor = Azul,
        focusedBorderColor = Cobre,
        unfocusedBorderColor = Borda,
        cursorColor = Cobre
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        // Cabeçalho
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
                text = "Novo orçamento",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
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

            // Serviço associado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(17.dp)
                ) {
                    Text(
                        text = "SERVIÇO VINCULADO",
                        color = Cobre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(9.dp))

                    Text(
                        text = servico.titulo,
                        color = Azul,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = servico.clienteNome,
                        color = Cinza,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Itens do orçamento",
                    color = Azul,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${itens.size} item(ns)",
                    color = Cinza,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Lista de itens
            if (itens.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = Cobre,
                            modifier = Modifier.size(31.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Nenhum item adicionado",
                            color = Azul,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = "Adicione materiais, mão de obra ou outros custos.",
                            color = Cinza,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itens.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color(0xFFF6E8D8),
                                            RoundedCornerShape(9.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = Cobre,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.descricao,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Azul
                                    )

                                    if (item.detalhe.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = item.detalhe,
                                            fontSize = 11.sp,
                                            color = Cinza
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = "${item.quantidade} × " +
                                                formatarMoedaOrcamento(item.valorUnitario),
                                        fontSize = 10.sp,
                                        color = Cinza
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = formatarMoedaOrcamento(item.subtotal),
                                        color = Azul,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = {
                                            itens.remove(item)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Remover item",
                                            tint = Vermelho,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Adicionar item
            OutlinedButton(
                onClick = {
                    mostrarNovoItem = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.dp, Cobre),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Cobre
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(7.dp))

                Text(
                    text = "Adicionar item",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(20.dp))

            // Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF6E8D8),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 15.dp,
                        vertical = 18.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = Cobre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatarMoedaOrcamento(total),
                    color = Cobre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Condições de pagamento",
                color = Azul,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = percentualEntrada,
                        onValueChange = {
                            percentualEntrada = it.filter { char ->
                                char.isDigit()
                            }.take(3)
                        },
                        label = {
                            Text("Entrada (%)")
                        },
                        supportingText = {
                            Text("Informe um valor entre 0 e 100.")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = coresCampo
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Entrada",
                            color = Cinza,
                            fontSize = 12.sp
                        )

                        Text(
                            text = formatarMoedaOrcamento(entrada),
                            color = Azul,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    HorizontalDivider(color = Borda)

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saldo na entrega",
                            color = Cinza,
                            fontSize = 12.sp
                        )

                        Text(
                            text = formatarMoedaOrcamento(saldo),
                            color = Azul,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Observações",
                color = Azul,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = observacoes,
                onValueChange = {
                    observacoes = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 105.dp),
                placeholder = {
                    Text(
                        "Informações adicionais sobre o orçamento...",
                        color = Cinza,
                        fontSize = 12.sp
                    )
                },
                minLines = 3,
                shape = RoundedCornerShape(11.dp),
                colors = coresCampo
            )

            if (erro.isNotBlank()) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = erro,
                    color = Vermelho,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // Botões
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onVoltar,
                modifier = Modifier
                    .weight(1f)
                    .height(51.dp),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.dp, Borda),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Azul
                )
            ) {
                Text("Cancelar", color = Azul)
            }

            Button(
                onClick = {
                    val entradaPercentual =
                        percentualEntrada.toIntOrNull()

                    erro = when {
                        itens.isEmpty() ->
                            "Adicione pelo menos um item."

                        entradaPercentual == null ||
                                entradaPercentual !in 0..100 ->
                            "Informe uma entrada entre 0% e 100%."

                        else -> {
                            onSalvar(
                                OrcamentoUi(
                                    id = System.currentTimeMillis(),
                                    servicoId = servico.id,
                                    clienteNome = servico.clienteNome,
                                    itens = itens.toList(),
                                    status = StatusOrcamento.EM_APROVACAO,
                                    percentualEntrada = entradaPercentual,
                                    observacoes = observacoes.trim()
                                )
                            )
                            ""
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(51.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Azul,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Salvar orçamento",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Janela para adicionar item
    if (mostrarNovoItem) {
        NovoItemOrcamentoDialog(
            onCancelar = {
                mostrarNovoItem = false
            },
            onAdicionar = { descricao, detalhe, quantidade, preco ->
                val proximoId =
                    (itens.maxOfOrNull { it.id } ?: 0L) + 1L

                itens.add(
                    ItemOrcamentoUi(
                        id = proximoId,
                        descricao = descricao,
                        detalhe = detalhe,
                        quantidade = quantidade,
                        valorUnitario = preco
                    )
                )

                erro = ""
                mostrarNovoItem = false
            }
        )
    }
}

@Composable
private fun NovoItemOrcamentoDialog(
    onCancelar: () -> Unit,
    onAdicionar: (String, String, Int, Double) -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var detalhe by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("1") }
    var valor by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Cobre,
        unfocusedBorderColor = Borda,
        focusedTextColor = Azul,
        unfocusedTextColor = Azul
    )

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = "Adicionar item",
                color = Azul,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = {
                        descricao = it
                        erro = ""
                    },
                    label = { Text("Descrição *") },
                    placeholder = {
                        Text("Ex.: Materiais")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCampo
                )

                OutlinedTextField(
                    value = detalhe,
                    onValueChange = { detalhe = it },
                    label = { Text("Detalhes (opcional)") },
                    placeholder = {
                        Text("Ex.: MDF e ferragens")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCampo
                )

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = {
                        quantidade = it.filter { char ->
                            char.isDigit()
                        }
                    },
                    label = { Text("Quantidade *") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCampo
                )

                OutlinedTextField(
                    value = valor,
                    onValueChange = {
                        valor = it
                        erro = ""
                    },
                    label = { Text("Valor unitário (R$) *") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCampo
                )

                if (erro.isNotBlank()) {
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
                    val qtd = quantidade.toIntOrNull()

                    val preco = valor
                        .replace(".", "")
                        .replace(",", ".")
                        .replace("R$", "")
                        .trim()
                        .toDoubleOrNull()

                    erro = when {
                        descricao.isBlank() ->
                            "Informe a descrição do item."

                        qtd == null || qtd <= 0 ->
                            "Informe uma quantidade válida."

                        preco == null || preco <= 0.0 ->
                            "Informe um valor válido."

                        else -> {
                            onAdicionar(
                                descricao.trim(),
                                detalhe.trim(),
                                qtd,
                                preco
                            )
                            ""
                        }
                    }
                }
            ) {
                Text(
                    "Adicionar",
                    color = Azul,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = Cinza)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(17.dp)
    )
}
