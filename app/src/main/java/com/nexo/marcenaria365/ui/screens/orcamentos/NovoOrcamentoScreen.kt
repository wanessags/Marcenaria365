
package com.nexo.marcenaria365.ui.screens.servicos.orcamentos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi

private val FundoNovoOrcamento = Color(0xFFFAF6F0)
private val AzulNovoOrcamento = Color(0xFF193447)
private val CobreNovoOrcamento = Color(0xFF94613C)
private val CinzaNovoOrcamento = Color(0xFF53616A)
private val BordaNovoOrcamento = Color(0xFF89969E)
private val VermelhoNovoOrcamento = Color(0xFFB3261E)

private fun converterMoedaNovoOrcamento(
    texto: String
): Double? {
    val limpo = texto
        .replace("R$", "")
        .replace(" ", "")
        .trim()

    val normalizado = if (limpo.contains(",")) {
        limpo.replace(".", "").replace(",", ".")
    } else {
        limpo
    }

    return normalizado.toDoubleOrNull()
}

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

    var itemParaRemover by remember {
        mutableStateOf<ItemOrcamentoUi?>(null)
    }

    var mostrarConfirmacao by remember {
        mutableStateOf(false)
    }

    var tentouSalvar by remember {
        mutableStateOf(false)
    }

    var erro by remember {
        mutableStateOf("")
    }

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val total = itens.sumOf { it.subtotal }

    val percentual = percentualEntrada.toIntOrNull()

    val percentualValido =
        percentual != null && percentual in 0..100

    val entrada = total * (percentual ?: 0)
        .coerceIn(0, 100) / 100.0

    val saldo = total - entrada

    val erroEntrada =
        tentouSalvar && !percentualValido

    fun validarOrcamento() {
        tentouSalvar = true

        erro = when {
            itens.isEmpty() ->
                "Adicione pelo menos um item ao orçamento."

            !percentualValido ->
                "Informe uma entrada entre 0% e 100%."

            !total.isFinite() || total <= 0.0 ->
                "O total do orçamento deve ser maior que zero."

            else -> ""
        }

        if (erro.isBlank()) {
            teclado?.hide()
            foco.clearFocus()
            mostrarConfirmacao = true
        }
    }

    BackHandler(
        enabled = !mostrarNovoItem &&
                itemParaRemover == null &&
                !mostrarConfirmacao,
        onBack = onVoltar
    )

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = AzulNovoOrcamento,
        unfocusedTextColor = AzulNovoOrcamento,
        focusedBorderColor = CobreNovoOrcamento,
        unfocusedBorderColor = BordaNovoOrcamento,
        focusedLabelColor = AzulNovoOrcamento,
        unfocusedLabelColor = CinzaNovoOrcamento,
        cursorColor = AzulNovoOrcamento,
        errorBorderColor = VermelhoNovoOrcamento,
        errorLabelColor = VermelhoNovoOrcamento
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoNovoOrcamento)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        // CABEÇALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .background(AzulNovoOrcamento)
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
                    contentDescription = "Voltar ao serviço",
                    tint = Color.White
                )
            }

            Text(
                text = "Novo orçamento",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
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

            // SERVIÇO VINCULADO
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
                        text = "Serviço vinculado",
                        color = CobreNovoOrcamento,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(9.dp))

                    Text(
                        text = servico.titulo,
                        color = AzulNovoOrcamento,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Cliente: ${servico.clienteNome}",
                        color = CinzaNovoOrcamento,
                        fontSize = 13.sp
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
                    color = AzulNovoOrcamento,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )

                Text(
                    text = "${itens.size} itens",
                    color = CinzaNovoOrcamento,
                    fontSize = 13.sp,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // LISTA DE ITENS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                if (itens.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(26.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = CobreNovoOrcamento,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Nenhum item adicionado",
                            color = AzulNovoOrcamento,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Adicione materiais, mão de obra ou outros custos.",
                            color = CinzaNovoOrcamento,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        itens.forEachIndexed { indice, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement =
                                        Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = item.descricao,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulNovoOrcamento
                                    )

                                    if (item.detalhe.isNotBlank()) {
                                        Text(
                                            text = item.detalhe,
                                            fontSize = 12.sp,
                                            color = CinzaNovoOrcamento
                                        )
                                    }

                                    Text(
                                        text = "${item.quantidade} × ${
                                            formatarMoedaOrcamento(
                                                item.valorUnitario
                                            )
                                        }",
                                        fontSize = 12.sp,
                                        color = CinzaNovoOrcamento
                                    )

                                    Text(
                                        text = "Subtotal: ${
                                            formatarMoedaOrcamento(
                                                item.subtotal
                                            )
                                        }",
                                        fontSize = 13.sp,
                                        color = AzulNovoOrcamento,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        itemParaRemover = item
                                    },
                                    modifier = Modifier.sizeIn(
                                        minWidth = 48.dp,
                                        minHeight = 48.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.DeleteOutline,
                                        contentDescription =
                                            "Remover item ${item.descricao}",
                                        tint = VermelhoNovoOrcamento
                                    )
                                }
                            }

                            if (indice < itens.lastIndex) {
                                HorizontalDivider(
                                    color = BordaNovoOrcamento
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ADICIONAR ITEM
            OutlinedButton(
                onClick = {
                    mostrarNovoItem = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(
                    1.dp,
                    CobreNovoOrcamento
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AzulNovoOrcamento,
                    containerColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Adicionar item",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(20.dp))

            // TOTAL DO ORÇAMENTO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF6E8D8),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(17.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription =
                            "Total do orçamento: ${
                                formatarMoedaOrcamento(total)
                            }"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                Text(
                    text = "Total do orçamento",
                    color = AzulNovoOrcamento,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    text = formatarMoedaOrcamento(total),
                    color = AzulNovoOrcamento,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Condições de pagamento",
                color = AzulNovoOrcamento,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
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
                            percentualEntrada = it.filter {
                                    caractere -> caractere.isDigit()
                            }.take(3)
                            erro = ""
                        },
                        label = {
                            Text("Entrada (%) *")
                        },
                        supportingText = {
                            Text(
                                text = "Informe um percentual entre 0 e 100.",
                                color = if (erroEntrada) {
                                    VermelhoNovoOrcamento
                                } else {
                                    CinzaNovoOrcamento
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                teclado?.hide()
                                foco.clearFocus()
                            }
                        ),
                        singleLine = true,
                        isError = erroEntrada,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (erroEntrada) {
                                    Modifier.semantics {
                                        error(
                                            "Informe uma entrada entre 0 e 100 por cento."
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = coresCampo
                    )

                    Spacer(Modifier.height(12.dp))

                    ResumoNovoOrcamento(
                        titulo = "Valor da entrada",
                        valor = formatarMoedaOrcamento(entrada)
                    )

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider(
                        color = BordaNovoOrcamento
                    )

                    Spacer(Modifier.height(12.dp))

                    ResumoNovoOrcamento(
                        titulo = "Saldo na entrega",
                        valor = formatarMoedaOrcamento(saldo)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // OBSERVAÇÕES
            Text(
                text = "Observações",
                color = AzulNovoOrcamento,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = observacoes,
                onValueChange = {
                    observacoes = it
                },
                label = {
                    Text("Observações (opcional)")
                },
                placeholder = {
                    Text(
                        text = "Informações adicionais sobre o orçamento",
                        color = CinzaNovoOrcamento
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(11.dp),
                colors = coresCampo
            )

            if (erro.isNotBlank()) {
                Spacer(Modifier.height(13.dp))

                Text(
                    text = erro,
                    color = VermelhoNovoOrcamento,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // BOTÕES INFERIORES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onVoltar,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(
                    1.dp,
                    BordaNovoOrcamento
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = AzulNovoOrcamento
                )
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    validarOrcamento()
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(11.dp),
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulNovoOrcamento
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

    // CADASTRAR ITEM
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

    // CONFIRMAR REMOÇÃO DE ITEM
    itemParaRemover?.let { item ->
        AlertDialog(
            onDismissRequest = {
                itemParaRemover = null
            },
            title = {
                Text(
                    text = "Remover item?",
                    color = AzulNovoOrcamento
                )
            },
            text = {
                Text(
                    text = "Deseja remover ${item.descricao} do orçamento?",
                    color = CinzaNovoOrcamento
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itens.remove(item)
                        itemParaRemover = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Remover",
                        color = VermelhoNovoOrcamento
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        itemParaRemover = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulNovoOrcamento
                    )
                }
            },
            containerColor = Color.White
        )
    }

    // CONFIRMAÇÃO ANTES DE SALVAR
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacao = false
            },
            title = {
                Text(
                    text = "Confirmar orçamento?",
                    color = AzulNovoOrcamento,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = "Confira os valores antes de salvar.",
                        color = CinzaNovoOrcamento
                    )

                    Text(
                        text = "Serviço: ${servico.titulo}",
                        color = AzulNovoOrcamento
                    )

                    Text(
                        text = "Cliente: ${servico.clienteNome}",
                        color = AzulNovoOrcamento
                    )

                    Text(
                        text = "Itens: ${itens.size}",
                        color = AzulNovoOrcamento
                    )

                    Text(
                        text = "Total: ${
                            formatarMoedaOrcamento(total)
                        }",
                        color = AzulNovoOrcamento,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Entrada: ${
                            formatarMoedaOrcamento(entrada)
                        } (${percentualEntrada}%)",
                        color = AzulNovoOrcamento
                    )

                    Text(
                        text = "Saldo: ${
                            formatarMoedaOrcamento(saldo)
                        }",
                        color = AzulNovoOrcamento
                    )

                    Text(
                        text = "Situação inicial: Em aprovação",
                        color = CinzaNovoOrcamento
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entradaConfirmada =
                            percentualEntrada.toIntOrNull()

                        if (
                            entradaConfirmada != null &&
                            entradaConfirmada in 0..100 &&
                            itens.isNotEmpty()
                        ) {
                            mostrarConfirmacao = false

                            onSalvar(
                                OrcamentoUi(
                                    id = System.currentTimeMillis(),
                                    servicoId = servico.id,
                                    clienteNome = servico.clienteNome,
                                    itens = itens.toList(),
                                    status = StatusOrcamento.EM_APROVACAO,
                                    percentualEntrada =
                                        entradaConfirmada,
                                    observacoes =
                                        observacoes.trim()
                                )
                            )
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulNovoOrcamento
                    )
                ) {
                    Text(
                        text = "Confirmar",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacao = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Revisar",
                        color = CinzaNovoOrcamento
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun ResumoNovoOrcamento(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo: $valor"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = CinzaNovoOrcamento,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = valor,
            color = AzulNovoOrcamento,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NovoItemOrcamentoDialog(
    onCancelar: () -> Unit,
    onAdicionar: (String, String, Int, Double) -> Unit
) {
    var descricao by remember {
        mutableStateOf("")
    }

    var detalhe by remember {
        mutableStateOf("")
    }

    var quantidade by remember {
        mutableStateOf("1")
    }

    var valor by remember {
        mutableStateOf("")
    }

    var erro by remember {
        mutableStateOf("")
    }

    var tentouAdicionar by remember {
        mutableStateOf(false)
    }

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val qtd = quantidade.toIntOrNull()
    val preco = converterMoedaNovoOrcamento(valor)

    val descricaoInvalida =
        tentouAdicionar && descricao.isBlank()

    val quantidadeInvalida =
        tentouAdicionar && (
                qtd == null || qtd <= 0
                )

    val precoInvalido =
        tentouAdicionar && (
                preco == null ||
                        !preco.isFinite() ||
                        preco <= 0.0
                )

    fun adicionarItem() {
        tentouAdicionar = true

        erro = when {
            descricao.isBlank() ->
                "Informe a descrição do item."

            qtd == null || qtd <= 0 ->
                "Informe uma quantidade válida."

            preco == null ||
                    !preco.isFinite() ||
                    preco <= 0.0 ->
                "Informe um valor unitário maior que zero."

            qtd.toDouble() * preco == Double.POSITIVE_INFINITY ->
                "O total do item é muito alto."

            else -> ""
        }

        if (erro.isEmpty() && qtd != null && preco != null) {
            teclado?.hide()
            foco.clearFocus()

            onAdicionar(
                descricao.trim(),
                detalhe.trim(),
                qtd,
                preco
            )
        }
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CobreNovoOrcamento,
        unfocusedBorderColor = BordaNovoOrcamento,
        focusedTextColor = AzulNovoOrcamento,
        unfocusedTextColor = AzulNovoOrcamento,
        focusedLabelColor = AzulNovoOrcamento,
        unfocusedLabelColor = CinzaNovoOrcamento,
        cursorColor = AzulNovoOrcamento,
        errorBorderColor = VermelhoNovoOrcamento,
        errorLabelColor = VermelhoNovoOrcamento
    )

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = "Adicionar item",
                color = AzulNovoOrcamento,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement =
                    Arrangement.spacedBy(11.dp)
            ) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = {
                        descricao = it
                        erro = ""
                    },
                    label = {
                        Text("Descrição *")
                    },
                    placeholder = {
                        Text("Ex.: Materiais")
                    },
                    singleLine = true,
                    isError = descricaoInvalida,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (descricaoInvalida) {
                                Modifier.semantics {
                                    error(
                                        "Informe a descrição do item."
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = coresCampo
                )

                if (descricaoInvalida) {
                    ErroItemNovoOrcamento(
                        "Informe a descrição do item."
                    )
                }

                OutlinedTextField(
                    value = detalhe,
                    onValueChange = {
                        detalhe = it
                    },
                    label = {
                        Text("Detalhes (opcional)")
                    },
                    placeholder = {
                        Text("Ex.: MDF e ferragens")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 3,
                    colors = coresCampo
                )

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = {
                        quantidade = it.filter {
                                caractere -> caractere.isDigit()
                        }
                        erro = ""
                    },
                    label = {
                        Text("Quantidade *")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    isError = quantidadeInvalida,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (quantidadeInvalida) {
                                Modifier.semantics {
                                    error(
                                        "Informe uma quantidade válida."
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = coresCampo
                )

                if (quantidadeInvalida) {
                    ErroItemNovoOrcamento(
                        "Informe uma quantidade válida."
                    )
                }

                OutlinedTextField(
                    value = valor,
                    onValueChange = {
                        valor = it
                        erro = ""
                    },
                    label = {
                        Text("Valor unitário (R$) *")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            teclado?.hide()
                            foco.clearFocus()
                        }
                    ),
                    singleLine = true,
                    isError = precoInvalido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (precoInvalido) {
                                Modifier.semantics {
                                    error(
                                        "Informe um valor unitário maior que zero."
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = coresCampo
                )

                if (precoInvalido) {
                    ErroItemNovoOrcamento(
                        "Informe um valor unitário maior que zero."
                    )
                }

                if (erro.isNotBlank()) {
                    Text(
                        text = erro,
                        color = VermelhoNovoOrcamento,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    adicionarItem()
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Adicionar",
                    color = AzulNovoOrcamento,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Cancelar",
                    color = CinzaNovoOrcamento
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(17.dp)
    )
}

@Composable
private fun ErroItemNovoOrcamento(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = VermelhoNovoOrcamento,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                liveRegion = LiveRegionMode.Polite
            }
    )
}
