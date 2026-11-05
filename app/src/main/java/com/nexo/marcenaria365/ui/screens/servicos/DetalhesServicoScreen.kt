
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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

private val FundoDetalhes = Color(0xFFFAF6F0)
private val AzulDetalhes = Color(0xFF193447)
private val CobreDetalhes = Color(0xFF94613C)
private val CinzaDetalhes = Color(0xFF53616A)
private val BordaDetalhes = Color(0xFFB5B5B0)
private val VerdeDetalhes = Color(0xFF237A58)
private val VermelhoDetalhes = Color(0xFFB3261E)

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

    var erro by remember {
        mutableStateOf("")
    }

    var valorValidado by remember {
        mutableStateOf<Double?>(null)
    }

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val saldo = (
            servico.valorEstimado - servico.valorRecebido
            ).coerceAtLeast(0.0)

    fun converterValor(texto: String): Double? {
        val limpo = texto
            .replace("R$", "")
            .trim()

        val normalizado = if (limpo.contains(",")) {
            limpo
                .replace(".", "")
                .replace(",", ".")
        } else {
            limpo
        }

        return normalizado.toDoubleOrNull()
    }

    fun validarRecebimento() {
        val valor = converterValor(valorRecebimento)

        erro = when {
            valor == null ||
                    !valor.isFinite() ||
                    valor <= 0.0 ->
                "Informe um valor válido maior que zero."

            valor > saldo ->
                "O valor informado supera o saldo disponível."

            else -> ""
        }

        if (erro.isEmpty() && valor != null) {
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
            .background(FundoDetalhes)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // CABECALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .background(AzulDetalhes)
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
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
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
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
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

            // FOTO E IDENTIFICACAO
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
                        color = AzulDetalhes,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Cliente: ${servico.clienteNome}",
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
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text(
                            text = "Situação: ${servico.status}",
                            color = when (servico.status) {
                                "Concluído" -> VerdeDetalhes
                                "Em produção" -> CobreDetalhes
                                else -> AzulDetalhes
                            },
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ABAS ACESSIVEIS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Detalhes",
                    "Orçamento",
                    "Histórico"
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
                                fontSize = 12.sp,
                                fontWeight = if (selecionada) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                },
                                maxLines = 1
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = AzulDetalhes,
                                selectedContainerColor =
                                    Color(0xFFF6E8D8),
                                selectedLabelColor = AzulDetalhes
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selecionada,
                                borderColor = BordaDetalhes,
                                selectedBorderColor = CobreDetalhes
                            )
                    )
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
                            TituloSecaoDetalhes(
                                "Informações gerais"
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
                                    .ifBlank {
                                        "Não informada"
                                    }
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
                            TituloSecaoDetalhes(
                                "Financeiro"
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
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
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
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics {
                                        heading()
                                    }
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Situação: ${orcamento.status}",
                                    color = CobreDetalhes,
                                    fontSize = 13.sp
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
                                    .heightIn(min = 50.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AzulDetalhes,
                                    contentColor = Color.White
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

                "Histórico" -> {
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
                            TituloSecaoDetalhes(
                                "Histórico financeiro"
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Total recebido",
                                color = CinzaDetalhes,
                                fontSize = 13.sp
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

            // REGISTRAR MOVIMENTACAO
            Button(
                onClick = {
                    valorRecebimento = ""
                    erro = ""
                    mostrarRecebimento = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
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

            // EXCLUIR SERVICO
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
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir serviço",
                    color = VermelhoDetalhes,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(25.dp))
        }
    }

    // CONFIRMACAO DE EXCLUSAO
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
                            "Essa ação não poderá ser desfeita.",
                    color = CinzaDetalhes,
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
                        color = VermelhoDetalhes,
                        fontWeight = FontWeight.SemiBold
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

    // INFORMAR VALOR DO RECEBIMENTO
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Saldo disponível: ${moedaServico(saldo)}",
                        color = AzulDetalhes,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = valorRecebimento,
                        onValueChange = {
                            valorRecebimento = it
                            erro = ""
                        },
                        label = {
                            Text("Valor recebido (R$) *")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (erro.isNotBlank()) {
                                    Modifier.semantics {
                                        error(erro)
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        singleLine = true,
                        isError = erro.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CobreDetalhes,
                            unfocusedBorderColor = BordaDetalhes,
                            focusedTextColor = AzulDetalhes,
                            unfocusedTextColor = AzulDetalhes,
                            focusedLabelColor = AzulDetalhes,
                            unfocusedLabelColor = CinzaDetalhes,
                            errorBorderColor = VermelhoDetalhes,
                            errorLabelColor = VermelhoDetalhes,
                            cursorColor = AzulDetalhes
                        )
                    )

                    if (erro.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = erro,
                            color = VermelhoDetalhes,
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
                        validarRecebimento()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Continuar",
                        color = AzulDetalhes,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarRecebimento = false
                        erro = ""
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = CinzaDetalhes
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // CONFIRMACAO FINANCEIRA
    if (confirmarRecebimento) {
        AlertDialog(
            onDismissRequest = {
                confirmarRecebimento = false
                mostrarRecebimento = true
            },
            title = {
                Text(
                    text = "Confirmar recebimento?",
                    color = AzulDetalhes,
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
                        color = CinzaDetalhes,
                        fontSize = 14.sp
                    )

                    CampoDetalheServico(
                        titulo = "Serviço",
                        valor = servico.titulo
                    )

                    CampoDetalheServico(
                        titulo = "Cliente",
                        valor = servico.clienteNome
                    )

                    CampoDetalheServico(
                        titulo = "Valor a receber",
                        valor = moedaServico(
                            valorValidado ?: 0.0
                        )
                    )

                    Text(
                        text = "Após confirmar, o recebimento " +
                                "será enviado para registro.",
                        color = CinzaDetalhes,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor = valorValidado
                            ?: return@Button

                        confirmarRecebimento = false
                        valorValidado = null
                        valorRecebimento = ""

                        onRegistrarRecebimento(valor)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulDetalhes
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
                        confirmarRecebimento = false
                        mostrarRecebimento = true
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Corrigir valor",
                        color = CinzaDetalhes
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }
}

@Composable
private fun TituloSecaoDetalhes(
    titulo: String
) {
    Text(
        text = titulo,
        color = AzulDetalhes,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

@Composable
private fun CampoDetalheServico(
    titulo: String,
    valor: String
) {
    Column(
        modifier = Modifier.semantics(
            mergeDescendants = true
        ) {
            contentDescription = "$titulo: $valor"
        }
    ) {
        Text(
            text = titulo,
            color = AzulDetalhes,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = valor,
            color = CinzaDetalhes,
            fontSize = 14.sp,
            lineHeight = 20.sp
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
            color = CinzaDetalhes,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = valor,
            color = cor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
