
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.formatarMoedaOrcamento
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.formatarNumeroOrcamento

private val CabecalhoServico = Color(0xFF193447)

private fun converterValorRecebido(texto: String): Double? {
    val limpo = texto.replace("R$", "").trim()
    val normalizado = if (limpo.contains(',')) {
        limpo.replace(".", "").replace(',', '.')
    } else {
        limpo
    }
    return normalizado.toDoubleOrNull()
}

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
    val cores = MaterialTheme.colorScheme
    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current

    val saldo = (
            servico.valorEstimado - servico.valorRecebido
            ).coerceAtLeast(0.0)

    var aba by rememberSaveable(servico.id) {
        mutableStateOf("Detalhes")
    }

    var confirmarExclusao by remember {
        mutableStateOf(false)
    }

    var mostrarRecebimento by remember {
        mutableStateOf(false)
    }

    var confirmarRecebimento by remember {
        mutableStateOf(false)
    }

    var valorRecebimento by rememberSaveable(servico.id) {
        mutableStateOf("")
    }

    var erroRecebimento by remember {
        mutableStateOf("")
    }

    var valorValidado by remember {
        mutableStateOf<Double?>(null)
    }

    // Se a aba antiga Histórico estiver salva,
    // retorna para Detalhes.
    LaunchedEffect(servico.id) {
        if (aba != "Detalhes" && aba != "Orçamento") {
            aba = "Detalhes"
        }
    }

    fun validarRecebimento() {
        val valor = converterValorRecebido(
            valorRecebimento
        )

        erroRecebimento = when {
            valor == null ||
                    !valor.isFinite() ||
                    valor <= 0.0 ->
                "Informe um valor válido maior que zero."

            valor > saldo ->
                "O valor informado supera o saldo disponível."

            else -> ""
        }

        if (
            erroRecebimento.isBlank() &&
            valor != null
        ) {
            valorValidado = valor

            teclado?.hide()
            foco.clearFocus()

            mostrarRecebimento = false
            confirmarRecebimento = true
        }
    }

    BackHandler(
        enabled = !confirmarExclusao &&
                !mostrarRecebimento &&
                !confirmarRecebimento,
        onBack = onVoltar
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cores.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // CABEÇALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CabecalhoServico)
                .heightIn(min = 60.dp)
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
                    contentDescription = "Voltar para serviços",
                    tint = Color.White
                )
            }

            Text(
                text = "Detalhes do serviço",
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        heading()
                    },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onEditarServico,
                modifier = Modifier.sizeIn(
                    minWidth = 48.dp,
                    minHeight = 48.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription =
                        "Editar serviço ${servico.titulo}",
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // IDENTIFICAÇÃO DO SERVIÇO
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FotoServico(
                    fotoUri = servico.fotoUri,
                    modifier = Modifier
                        .size(82.dp)
                        .semantics {
                            contentDescription =
                                if (servico.fotoUri.isNullOrBlank()) {
                                    "Serviço sem foto cadastrada"
                                } else {
                                    "Foto cadastrada do serviço ${servico.titulo}"
                                }
                        }
                )

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = servico.titulo,
                        color = cores.onBackground,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Cliente: ${servico.clienteNome}",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = cores.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Situação: ${servico.status}",
                            color = cores.onSecondaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ATALHO PARA A EDIÇÃO JÁ EXISTENTE
            OutlinedButton(
                onClick = onEditarServico,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = cores.onBackground
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Alterar situação do serviço",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(18.dp))

            // APENAS DUAS ABAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "Detalhes",
                    "Orçamento"
                ).forEach { item ->

                    val selecionada = aba == item

                    FilterChip(
                        selected = selecionada,
                        onClick = {
                            aba = item
                        },
                        label = {
                            Text(
                                text = item,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(11.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = cores.surface,
                                labelColor = cores.onSurface,
                                selectedContainerColor =
                                    cores.primaryContainer,
                                selectedLabelColor =
                                    cores.onPrimaryContainer
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selecionada,
                                borderColor = cores.outline,
                                selectedBorderColor = cores.secondary
                            )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            when (aba) {

                // ABA ORÇAMENTO
                "Orçamento" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = cores.secondary,
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            if (orcamento == null) {
                                Text(
                                    text = "Nenhum orçamento cadastrado",
                                    color = cores.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Cadastre materiais, mão de obra e condições de pagamento.",
                                    color = cores.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Orçamento #${
                                        formatarNumeroOrcamento(
                                            orcamento.id
                                        )
                                    }",
                                    color = cores.onSurface,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Situação: ${orcamento.status}",
                                    color = cores.onSurfaceVariant,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(15.dp))

                                Text(
                                    text = formatarMoedaOrcamento(
                                        orcamento.total
                                    ),
                                    color = cores.onSurface,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = onAbrirOrcamento,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp),
                                shape = RoundedCornerShape(11.dp)
                            ) {
                                Text(
                                    text = if (orcamento == null) {
                                        "Criar orçamento"
                                    } else {
                                        "Ver orçamento"
                                    }
                                )
                            }
                        }
                    }
                }

                // ABA DETALHES
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {
                            TituloSecaoServico(
                                "Informações gerais"
                            )

                            CampoInfoServico(
                                "Cliente",
                                servico.clienteNome
                            )

                            HorizontalDivider(
                                color = cores.outlineVariant
                            )

                            CampoInfoServico(
                                "Prazo de entrega",
                                servico.entrega
                            )

                            HorizontalDivider(
                                color = cores.outlineVariant
                            )

                            CampoInfoServico(
                                "Descrição",
                                servico.descricao.ifBlank {
                                    "Não informada"
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // RESUMO FINANCEIRO
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {
                            TituloSecaoServico(
                                "Resumo financeiro"
                            )

                            ValorLinhaServico(
                                "Valor estimado",
                                moedaServico(
                                    servico.valorEstimado
                                )
                            )

                            HorizontalDivider(
                                color = cores.outlineVariant
                            )

                            ValorLinhaServico(
                                "Total recebido",
                                moedaServico(
                                    servico.valorRecebido
                                )
                            )

                            HorizontalDivider(
                                color = cores.outlineVariant
                            )

                            ValorLinhaServico(
                                "Saldo a receber",
                                moedaServico(saldo)
                            )

                            Spacer(Modifier.height(3.dp))

                            // ÚNICO BOTÃO DE RECEBIMENTO
                            if (saldo > 0.0) {
                                Button(
                                    onClick = {
                                        valorRecebimento = ""
                                        erroRecebimento = ""
                                        mostrarRecebimento = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp),
                                    shape = RoundedCornerShape(11.dp)
                                ) {
                                    Text(
                                        text = "Registrar recebimento",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Não há saldo pendente para este serviço.",
                                    color = cores.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // EXCLUSÃO MANTIDA
            TextButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = cores.error
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir serviço",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(26.dp))
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
                    color = cores.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja excluir \"${servico.titulo}\"? " +
                            "O orçamento vinculado também será excluído. " +
                            "Essa ação não poderá ser desfeita.",
                    color = cores.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluirServico(servico.id)
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Excluir definitivamente",
                        color = cores.error
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
                    Text("Cancelar")
                }
            },
            containerColor = cores.surface,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // INFORMAR VALOR RECEBIDO
    if (mostrarRecebimento) {
        AlertDialog(
            onDismissRequest = {
                mostrarRecebimento = false
            },
            title = {
                Text(
                    text = "Registrar recebimento",
                    color = cores.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    Text(
                        text = "Saldo disponível: ${
                            moedaServico(saldo)
                        }",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = valorRecebimento,
                        onValueChange = {
                            valorRecebimento = it
                            erroRecebimento = ""
                        },
                        label = {
                            Text("Valor recebido (R$) *")
                        },
                        singleLine = true,
                        isError =
                            erroRecebimento.isNotBlank(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (
                                    erroRecebimento.isNotBlank()
                                ) {
                                    Modifier.semantics {
                                        error(erroRecebimento)
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    cores.secondary,
                                unfocusedBorderColor =
                                    cores.outline,
                                focusedTextColor =
                                    cores.onSurface,
                                unfocusedTextColor =
                                    cores.onSurface,
                                focusedLabelColor =
                                    cores.onSurface,
                                unfocusedLabelColor =
                                    cores.onSurfaceVariant,
                                errorBorderColor =
                                    cores.error,
                                errorLabelColor =
                                    cores.error,
                                cursorColor =
                                    cores.secondary
                            )
                    )

                    if (erroRecebimento.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = erroRecebimento,
                            color = cores.error,
                            fontSize = 12.sp,
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
                        validarRecebimento()
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarRecebimento = false
                        erroRecebimento = ""
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = cores.surface,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // CONFIRMAÇÃO FINANCEIRA
    if (confirmarRecebimento) {
        AlertDialog(
            onDismissRequest = {
                confirmarRecebimento = false
                mostrarRecebimento = true
            },
            title = {
                Text(
                    text = "Confirmar recebimento?",
                    color = cores.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Confira os dados antes de registrar.",
                        color = cores.onSurfaceVariant
                    )

                    CampoInfoServico(
                        "Serviço",
                        servico.titulo
                    )

                    CampoInfoServico(
                        "Cliente",
                        servico.clienteNome
                    )

                    CampoInfoServico(
                        "Valor a receber",
                        moedaServico(
                            valorValidado ?: 0.0
                        )
                    )

                    Text(
                        text = "Após confirmar, o recebimento será enviado para registro.",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor =
                            valorValidado ?: return@Button

                        confirmarRecebimento = false
                        valorValidado = null
                        valorRecebimento = ""

                        onRegistrarRecebimento(valor)
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarRecebimento = false
                        mostrarRecebimento = true
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text("Corrigir valor")
                }
            },
            containerColor = cores.surface,
            shape = RoundedCornerShape(17.dp)
        )
    }
}

@Composable
private fun TituloSecaoServico(
    titulo: String
) {
    Text(
        text = titulo,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

@Composable
private fun CampoInfoServico(
    titulo: String,
    valor: String
) {
    val cores = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.semantics(
            mergeDescendants = true
        ) {
            contentDescription = "$titulo: $valor"
        }
    ) {
        Text(
            text = titulo,
            color = cores.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = valor,
            color = cores.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ValorLinhaServico(
    titulo: String,
    valor: String
) {
    val cores = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo: $valor"
            },
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = cores.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = valor,
            color = cores.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
