package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.formatarMoedaOrcamento
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.formatarNumeroOrcamento

private val CabecalhoServico = Color(0xFF193447)
private val CobreServico = Color(0xFFA47752)
private val VerdeServico = Color(0xFF237A58)
private val LaranjaServico = Color(0xFFAD792D)
private val VermelhoServico = Color(0xFFB3261E)

private fun converterValorRecebido(
    texto: String
): Double? {
    val limpo = texto
        .replace("R$", "")
        .trim()

    val normalizado = if (limpo.contains(',')) {
        limpo.replace(".", "").replace(',', '.')
    } else {
        limpo
    }

    return normalizado.toDoubleOrNull()
}

private fun categoriaSituacaoServico(
    situacao: String
): String {
    val texto = situacao.trim().lowercase()

    return when {
        texto.contains("cancel") -> "cancelado"

        texto.contains("conclu") ||
                texto.contains("finaliz") ||
                texto.contains("entregue") -> "concluido"

        texto.contains("produ") ||
                texto.contains("andamento") ||
                texto.contains("execu") -> "producao"

        else -> "aguardando"
    }
}

@Composable
private fun IndicadorSituacaoServico(
    situacao: String
) {
    val cores = MaterialTheme.colorScheme
    val categoria = categoriaSituacaoServico(situacao)

    val cor = when (categoria) {
        "cancelado" -> cores.error
        "concluido" -> VerdeServico
        "producao" -> cores.primary
        else -> LaranjaServico
    }

    val fundo = when (categoria) {
        "cancelado" -> cores.errorContainer
        "concluido" -> cores.tertiaryContainer
        "producao" -> cores.primaryContainer
        else -> cores.secondaryContainer
    }

    val corTexto = when (categoria) {
        "cancelado" -> cores.onErrorContainer
        "concluido" -> cores.onTertiaryContainer
        "producao" -> cores.onPrimaryContainer
        else -> cores.onSecondaryContainer
    }

    Surface(
        color = fundo,
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.semantics(
            mergeDescendants = true
        ) {
            contentDescription =
                "Situação do serviço: $situacao"
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        cor,
                        CircleShape
                    )
            )

            Text(
                text = situacao,
                color = corTexto,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
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

    val cancelado =
        categoriaSituacaoServico(servico.status) == "cancelado"

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

    LaunchedEffect(servico.id) {
        if (
            aba != "Detalhes" &&
            aba != "Orçamento"
        ) {
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

        // CABEÇALHO ORIGINAL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CabecalhoServico)
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
                    contentDescription = "Voltar para serviços",
                    tint = Color.White
                )
            }

            Text(
                text = "Detalhes do serviço",
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            // APRESENTAÇÃO PRINCIPAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cores.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = cores.surfaceVariant
                        ) {
                            FotoServico(
                                fotoUri = servico.fotoUri,
                                modifier = Modifier
                                    .size(94.dp)
                                    .semantics {
                                        contentDescription =
                                            if (
                                                servico.fotoUri.isNullOrBlank()
                                            ) {
                                                "Serviço sem foto cadastrada"
                                            } else {
                                                "Foto do serviço ${servico.titulo}"
                                            }
                                    }
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = servico.titulo,
                                color = cores.onSurface,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics {
                                    heading()
                                }
                            )

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                    tint = cores.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )

                                Text(
                                    text = servico.clienteNome,
                                    color = cores.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IndicadorSituacaoServico(
                                situacao = servico.status
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(
                        color = cores.outlineVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = CobreServico,
                            modifier = Modifier.size(19.dp)
                        )

                        Column {
                            Text(
                                text = "Prazo de entrega",
                                color = cores.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Text(
                                text = servico.entrega.ifBlank {
                                    "Não informado"
                                },
                                color = cores.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(13.dp))

            // ALTERAR SITUAÇÃO
            OutlinedButton(
                onClick = onEditarServico,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    cores.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = cores.surface,
                    contentColor = cores.onSurface
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            // ABAS
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
                        onClick = { aba = item },
                        label = {
                            Text(
                                text = item,
                                fontSize = 14.sp,
                                fontWeight = if (selecionada) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
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
                                selectedBorderColor = cores.primary
                            )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (aba) {

                // ORÇAMENTO
                "Orçamento" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(17.dp),
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
                            Surface(
                                color = cores.primaryContainer,
                                shape = RoundedCornerShape(15.dp)
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = cores.onPrimaryContainer,
                                    modifier = Modifier
                                        .padding(15.dp)
                                        .size(34.dp)
                                )
                            }

                            Spacer(Modifier.height(15.dp))

                            if (orcamento == null) {
                                Text(
                                    text = "Nenhum orçamento cadastrado",
                                    color = cores.onSurface,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text =
                                        "Organize materiais, mão de obra e condições de pagamento em um só lugar.",
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
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(8.dp))

                                Surface(
                                    color = cores.secondaryContainer,
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text(
                                        text = orcamento.status,
                                        color =
                                            cores.onSecondaryContainer,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            horizontal = 13.dp,
                                            vertical = 7.dp
                                        )
                                    )
                                }

                                Spacer(Modifier.height(18.dp))

                                Text(
                                    text = "Valor do orçamento",
                                    color = cores.onSurfaceVariant,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = formatarMoedaOrcamento(
                                        orcamento.total
                                    ),
                                    color = cores.onSurface,
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = onAbrirOrcamento,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CabecalhoServico,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(9.dp))

                                Text(
                                    text = if (orcamento == null) {
                                        "Criar orçamento"
                                    } else {
                                        "Ver orçamento"
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // DETALHES
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(17.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(15.dp)
                        ) {
                            TituloSecaoServico(
                                "Informações gerais"
                            )

                            CampoInfoServico(
                                titulo = "Cliente",
                                valor = servico.clienteNome,
                                icone = {
                                    Icon(
                                        Icons.Outlined.PersonOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                            HorizontalDivider(
                                color = cores.outlineVariant
                            )

                            CampoInfoServico(
                                titulo = "Prazo de entrega",
                                valor = servico.entrega.ifBlank {
                                    "Não informado"
                                },
                                icone = {
                                    Icon(
                                        Icons.Outlined.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                            HorizontalDivider(
                                color = cores.outlineVariant
                            )

                            CampoInfoServico(
                                titulo = "Descrição",
                                valor = servico.descricao.ifBlank {
                                    "Não informada"
                                },
                                icone = {
                                    Icon(
                                        Icons.Outlined.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(15.dp))

                    // RESUMO FINANCEIRO
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(17.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(9.dp)
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = cores.primary,
                                    modifier = Modifier.size(22.dp)
                                )

                                TituloSecaoServico(
                                    "Resumo financeiro"
                                )
                            }

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

                            Surface(
                                color = cores.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(13.dp),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saldo a receber",
                                        color =
                                            cores.onPrimaryContainer,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = moedaServico(saldo),
                                        color =
                                            cores.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                            }

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
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CabecalhoServico,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(19.dp)
                                    )

                                    Spacer(Modifier.width(9.dp))

                                    Text(
                                        text = "Registrar recebimento",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = VerdeServico,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Text(
                                        text =
                                            "Não há saldo pendente para este serviço.",
                                        color = cores.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // EXCLUIR SERVIÇO
            OutlinedButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    cores.error.copy(alpha = 0.55f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = cores.error
                )
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir serviço",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(30.dp))
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
                    text =
                        "Deseja excluir \"${servico.titulo}\"? " +
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
                    modifier = Modifier.heightIn(min = 48.dp)
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
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = cores.surface,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // INFORMAR RECEBIMENTO
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
                    modifier = Modifier.heightIn(min = 48.dp)
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
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = cores.surface,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // CONFIRMAR RECEBIMENTO
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
                        text =
                            "Confira os dados antes de registrar.",
                        color = cores.onSurfaceVariant
                    )

                    CampoInfoServico(
                        titulo = "Serviço",
                        valor = servico.titulo
                    )

                    CampoInfoServico(
                        titulo = "Cliente",
                        valor = servico.clienteNome
                    )

                    CampoInfoServico(
                        titulo = "Valor a receber",
                        valor = moedaServico(
                            valorValidado ?: 0.0
                        )
                    )

                    Text(
                        text =
                            "Após confirmar, o recebimento será enviado para registro.",
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
                    modifier = Modifier.heightIn(min = 48.dp)
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
                    modifier = Modifier.heightIn(min = 48.dp)
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
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

@Composable
private fun CampoInfoServico(
    titulo: String,
    valor: String,
    icone: (@Composable () -> Unit)? = null
) {
    val cores = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo: $valor"
            },
        verticalAlignment = Alignment.Top,
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        if (icone != null) {
            Surface(
                color = cores.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(39.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides
                                cores.onSurfaceVariant
                    ) {
                        icone()
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                color = cores.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = valor,
                color = cores.onSurface,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium
            )
        }
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