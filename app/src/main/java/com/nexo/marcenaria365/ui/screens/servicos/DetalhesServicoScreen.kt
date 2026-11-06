
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
import androidx.compose.ui.graphics.luminance
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

private val AzulServico = Color(0xFF193447)
private val CobreServico = Color(0xFFA47752)
private val VerdeServico = Color(0xFF237A58)
private val VermelhoServico = Color(0xFFB3261E)

private fun converterValorRecebido(texto: String): Double? {
    val limpo = texto.replace("R$", "").trim()

    val normalizado = if (limpo.contains(',')) {
        limpo.replace(".", "").replace(',', '.')
    } else {
        limpo
    }

    return normalizado.toDoubleOrNull()
}

private fun categoriaSituacaoServico(situacao: String): String {
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
private fun IndicadorSituacaoServico(situacao: String) {
    val cores = MaterialTheme.colorScheme
    val categoria = categoriaSituacaoServico(situacao)

    val corIndicador = when (categoria) {
        "cancelado" -> cores.error
        "concluido" -> VerdeServico
        "producao" -> cores.primary
        else -> CobreServico
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
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Situação do serviço: $situacao"
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(corIndicador, CircleShape)
            )

            Text(
                text = situacao,
                color = corTexto,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
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

    val escuro = cores.background.luminance() < 0.5f

    val corFundo = cores.background
    val corCard = cores.surface
    val corTexto = cores.onSurface
    val corSecundaria = cores.onSurfaceVariant
    val corBorda = cores.outlineVariant
    val corIcone = if (escuro) {
        Color(0xFFD8AE84)
    } else {
        CobreServico
    }
    val fundoIcone = if (escuro) {
        Color(0xFF354A57)
    } else {
        Color(0xFFF4EBE0)
    }

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

    LaunchedEffect(servico.id) {
        if (aba != "Detalhes" && aba != "Orçamento") {
            aba = "Detalhes"
        }
    }

    fun validarRecebimento() {
        val valor = converterValorRecebido(valorRecebimento)

        erroRecebimento = when {
            valor == null ||
                    !valor.isFinite() ||
                    valor <= 0.0 ->
                "Informe um valor válido maior que zero."

            valor > saldo ->
                "O valor informado supera o saldo disponível."

            else -> ""
        }

        if (erroRecebimento.isBlank() && valor != null) {
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
            .background(corFundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // CABEÇALHO — SEM CANETINHA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulServico)
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

            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            // CARD PRINCIPAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = corCard
                ),
                border = BorderStroke(
                    1.dp,
                    corBorda.copy(alpha = 0.65f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(13.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(15.dp),
                            color = cores.surfaceVariant
                        ) {
                            FotoServico(
                                fotoUri = servico.fotoUri,
                                modifier = Modifier
                                    .size(92.dp)
                                    .semantics {
                                        contentDescription =
                                            if (servico.fotoUri.isNullOrBlank()) {
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
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = servico.titulo,
                                color = corTexto,
                                fontSize = 20.sp,
                                lineHeight = 25.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics {
                                    heading()
                                }
                            )

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                    tint = corSecundaria,
                                    modifier = Modifier.size(17.dp)
                                )

                                Text(
                                    text = servico.clienteNome,
                                    color = corSecundaria,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IndicadorSituacaoServico(
                                situacao = servico.status
                            )
                        }
                    }

                    HorizontalDivider(color = corBorda)

                    // PRAZO + ÚNICA CANETINHA DE EDIÇÃO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(11.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = corIcone,
                            modifier = Modifier.size(24.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Prazo de entrega",
                                color = corSecundaria,
                                fontSize = 13.sp
                            )

                            Text(
                                text = servico.entrega.ifBlank {
                                    "Não informado"
                                },
                                color = corTexto,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            onClick = onEditarServico,
                            color = fundoIcone,
                            shape = RoundedCornerShape(13.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription =
                                        "Editar serviço ${servico.titulo}, incluindo a situação"
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = if (escuro) {
                                        Color.White
                                    } else {
                                        AzulServico
                                    },
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ABAS MODERNIZADAS
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (escuro) {
                    cores.surfaceVariant
                } else {
                    Color(0xFFE8EBED)
                },
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(
                    1.dp,
                    corBorda.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(5.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    listOf("Detalhes", "Orçamento").forEach { item ->
                        val selecionada = aba == item

                        Surface(
                            onClick = { aba = item },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .semantics {
                                    contentDescription =
                                        "Aba $item${if (selecionada) ", selecionada" else ""}"
                                },
                            shape = RoundedCornerShape(11.dp),
                            color = if (selecionada) {
                                if (escuro) {
                                    Color(0xFFE8D0B5)
                                } else {
                                    AzulServico
                                }
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    color = if (selecionada) {
                                        if (escuro) {
                                            AzulServico
                                        } else {
                                            Color.White
                                        }
                                    } else {
                                        corTexto
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (selecionada) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (aba) {
                "Orçamento" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = corCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            corBorda.copy(alpha = 0.6f)
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
                                color = fundoIcone,
                                shape = RoundedCornerShape(15.dp)
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = corIcone,
                                    modifier = Modifier
                                        .padding(15.dp)
                                        .size(32.dp)
                                )
                            }

                            Spacer(Modifier.height(15.dp))

                            if (orcamento == null) {
                                Text(
                                    text = "Nenhum orçamento cadastrado",
                                    color = corTexto,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(9.dp))

                                Text(
                                    text = "Organize materiais, mão de obra e condições de pagamento em um só lugar.",
                                    color = corSecundaria,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Orçamento #${
                                        formatarNumeroOrcamento(orcamento.id)
                                    }",
                                    color = corTexto,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(9.dp))

                                Surface(
                                    color = cores.secondaryContainer,
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text(
                                        text = orcamento.status,
                                        color = cores.onSecondaryContainer,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            horizontal = 13.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                Text(
                                    text = "Valor do orçamento",
                                    color = corSecundaria,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = formatarMoedaOrcamento(
                                        orcamento.total
                                    ),
                                    color = corTexto,
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
                                    containerColor = if (escuro) {
                                        Color(0xFFE8D0B5)
                                    } else {
                                        AzulServico
                                    },
                                    contentColor = if (escuro) {
                                        AzulServico
                                    } else {
                                        Color.White
                                    }
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

                else -> {
                    // INFORMAÇÕES GERAIS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = corCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            corBorda.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(15.dp)
                        ) {
                            TituloSecaoServico(
                                titulo = "Informações gerais"
                            )

                            CampoInfoServico(
                                titulo = "Cliente",
                                valor = servico.clienteNome,
                                icone = {
                                    Icon(
                                        Icons.Outlined.PersonOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            )

                            HorizontalDivider(color = corBorda)

                            CampoInfoServico(
                                titulo = "Prazo de entrega",
                                valor = servico.entrega.ifBlank {
                                    "Não informado"
                                },
                                icone = {
                                    Icon(
                                        Icons.Outlined.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            )

                            HorizontalDivider(color = corBorda)

                            CampoInfoServico(
                                titulo = "Descrição",
                                valor = servico.descricao.ifBlank {
                                    "Não informada"
                                },
                                icone = {
                                    Icon(
                                        Icons.Outlined.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // RESUMO FINANCEIRO
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = corCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            corBorda.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(15.dp)
                        ) {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = fundoIcone,
                                    shape = RoundedCornerShape(11.dp)
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = corIcone,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(22.dp)
                                    )
                                }

                                TituloSecaoServico(
                                    "Resumo financeiro"
                                )
                            }

                            ValorLinhaServico(
                                titulo = "Valor estimado",
                                valor = moedaServico(
                                    servico.valorEstimado
                                )
                            )

                            HorizontalDivider(color = corBorda)

                            ValorLinhaServico(
                                titulo = "Total recebido",
                                valor = moedaServico(
                                    servico.valorRecebido
                                )
                            )

                            HorizontalDivider(color = corBorda)

                            Surface(
                                color = if (saldo > 0.0) {
                                    if (escuro) {
                                        Color(0xFF553D43)
                                    } else {
                                        Color(0xFFFBE4E2)
                                    }
                                } else {
                                    cores.tertiaryContainer
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Saldo a receber",
                                        color = if (saldo > 0.0) {
                                            if (escuro) {
                                                Color(0xFFFFD5D0)
                                            } else {
                                                VermelhoServico
                                            }
                                        } else {
                                            cores.onTertiaryContainer
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = moedaServico(saldo),
                                        color = if (saldo > 0.0) {
                                            if (escuro) {
                                                Color(0xFFFFD5D0)
                                            } else {
                                                VermelhoServico
                                            }
                                        } else {
                                            cores.onTertiaryContainer
                                        },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
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
                                        containerColor = if (escuro) {
                                            Color(0xFFE8D0B5)
                                        } else {
                                            AzulServico
                                        },
                                        contentColor = if (escuro) {
                                            AzulServico
                                        } else {
                                            Color.White
                                        }
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
                                        modifier = Modifier.size(19.dp)
                                    )

                                    Text(
                                        text = "Não há saldo pendente para este serviço.",
                                        color = corSecundaria,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // EXCLUSÃO DO SERVIÇO
            OutlinedButton(
                onClick = {
                    confirmarExclusao = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(13.dp),
                border = BorderStroke(
                    1.dp,
                    cores.error
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = cores.error
                )
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Excluir serviço",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    // CONFIRMAR EXCLUSÃO
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

    // REGISTRAR RECEBIMENTO
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
                        text = "Saldo disponível: ${moedaServico(saldo)}",
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
                        isError = erroRecebimento.isNotBlank(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (erroRecebimento.isNotBlank()) {
                                    Modifier.semantics {
                                        error(erroRecebimento)
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cores.secondary,
                            unfocusedBorderColor = cores.outline,
                            focusedTextColor = cores.onSurface,
                            unfocusedTextColor = cores.onSurface,
                            focusedLabelColor = cores.onSurface,
                            unfocusedLabelColor =
                                cores.onSurfaceVariant,
                            errorBorderColor = cores.error,
                            errorLabelColor = cores.error,
                            cursorColor = cores.secondary
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
                        text = "Confira os dados antes de registrar.",
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
private fun TituloSecaoServico(titulo: String) {
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
    val escuro = cores.background.luminance() < 0.5f

    val corFundoIcone = if (escuro) {
        Color(0xFF354A57)
    } else {
        Color(0xFFF4EBE0)
    }

    val corIcone = if (escuro) {
        Color(0xFFD8AE84)
    } else {
        CobreServico
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo: $valor"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icone != null) {
            Surface(
                color = corFundoIcone,
                shape = RoundedCornerShape(11.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides corIcone
                    ) {
                        icone()
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                color = cores.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = valor,
                color = cores.onSurface,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = cores.onSurfaceVariant,
            fontSize = 14.sp,
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
