
package com.nexo.marcenaria365.ui.screens.caixa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AlertTriangle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexo.marcenaria365.data.repository.MovimentacaoCaixaRepository
import com.nexo.marcenaria365.data.repository.RecebimentoRepository
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

private val CaixaAzul = Color(0xFF193447)
private val CaixaFundo = Color(0xFFFAF6F0)
private val CaixaVerde = Color(0xFF237A58)
private val CaixaLaranja = Color(0xFF94613C)
private val CaixaCinza = Color(0xFF53616A)
private val CaixaBorda = Color(0xFF89969E)
private val CaixaVermelho = Color(0xFFB3261E)
private val CobreCaixa = Color(0xFFA47752)

private fun valorCaixaDigitado(texto: String): Double? {
    val limpo = texto.replace("R$", "").replace(" ", "").trim()
    val normalizado = if (limpo.contains(",")) {
        limpo.replace(".", "").replace(",", ".")
    } else {
        limpo
    }
    return normalizado.toDoubleOrNull()
}

@Composable
fun CaixaScreen(
    onVoltar: () -> Unit,
    servicos: List<ServicoUi>
) {
    val scope = rememberCoroutineScope()
    val movimentacoes = remember {
        mutableStateListOf<MovimentacaoUi>()
    }

    var erroCaixa by remember { mutableStateOf<String?>(null) }
    var carregandoCaixa by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf(false) }
    var movimentacaoExcluir by remember {
        mutableStateOf<MovimentacaoUi?>(null)
    }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }
    var atualizacao by remember { mutableIntStateOf(0) }
    var mostrarCadastro by remember { mutableStateOf(false) }

    var periodo by rememberSaveable { mutableIntStateOf(0) }
    var menuPeriodo by remember { mutableStateOf(false) }
    var mostrarTodas by rememberSaveable { mutableStateOf(false) }
    var visualizacao by rememberSaveable { mutableStateOf("Todas") }

    LaunchedEffect(atualizacao, servicos) {
        carregandoCaixa = true

        val servicosPorId = servicos.associateBy { it.id }

        val resultado = withContext(Dispatchers.IO) {
            runCatching {
                val recebimentos =
                    RecebimentoRepository.listar().getOrThrow()
                val avulsas =
                    MovimentacaoCaixaRepository.listar().getOrThrow()

                val entradasRecebidas =
                    recebimentos.mapNotNull { recebimento ->
                        if (!recebimento.status.equals("RECEBIDO", true)) {
                            return@mapNotNull null
                        }

                        val data = recebimento.dataRecebimento?.let {
                            runCatching {
                                LocalDate.parse(it)
                            }.getOrNull()
                        } ?: return@mapNotNull null

                        val servico = servicosPorId[recebimento.servicoId]

                        MovimentacaoUi(
                            id = recebimento.id,
                            titulo = "Pagamento recebido",
                            descricao = servico?.let {
                                "${it.clienteNome} - ${it.titulo}"
                            } ?: "Serviço #${recebimento.servicoId}",
                            valor = recebimento.valor,
                            tipo = TipoMovimentacao.ENTRADA,
                            dia = data.dayOfMonth,
                            mes = data.monthValue,
                            ano = data.year,
                            servicoId = recebimento.servicoId
                        )
                    }

                val movimentosAvulsos =
                    avulsas.mapNotNull { movimento ->
                        if (movimento.recebimentoId != null) {
                            return@mapNotNull null
                        }

                        val data = runCatching {
                            LocalDate.parse(movimento.dataMovimentacao)
                        }.getOrNull() ?: return@mapNotNull null

                        MovimentacaoUi(
                            id = -movimento.id,
                            titulo = movimento.categoria,
                            descricao = movimento.descricao,
                            valor = movimento.valor,
                            tipo = if (movimento.tipo == "ENTRADA") {
                                TipoMovimentacao.ENTRADA
                            } else {
                                TipoMovimentacao.SAIDA
                            },
                            dia = data.dayOfMonth,
                            mes = data.monthValue,
                            ano = data.year
                        )
                    }

                entradasRecebidas + movimentosAvulsos
            }
        }

        resultado.fold(
            onSuccess = {
                movimentacoes.clear()
                movimentacoes.addAll(it)
                erroCaixa = null
            },
            onFailure = {
                erroCaixa = it.message
                    ?: "Não foi possível carregar o caixa."
            }
        )

        carregandoCaixa = false
    }

    // Três meses: atual e dois anteriores.
    // Exemplo: NOV/26, OUT/26 e SET/26.
    val periodos = remember {
        val abreviacoes = listOf(
            "JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
            "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"
        )

        (0..2).map { deslocamento ->
            val calendario = Calendar.getInstance().apply {
                add(Calendar.MONTH, -deslocamento)
            }

            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH) + 1
            val anoCurto = (ano % 100).toString().padStart(2, '0')

            Triple(
                ano,
                mes,
                "${abreviacoes[mes - 1]}/$anoCurto"
            )
        }
    }

    val anoSelecionado = periodos[periodo].first
    val mesSelecionado = periodos[periodo].second

    val filtradas = movimentacoes
        .filter {
            it.ano == anoSelecionado &&
                    it.mes == mesSelecionado
        }
        .sortedWith(
            compareByDescending<MovimentacaoUi> { it.ano }
                .thenByDescending { it.mes }
                .thenByDescending { it.dia }
                .thenByDescending { it.id }
        )

    val entradas = filtradas
        .filter { it.tipo == TipoMovimentacao.ENTRADA }
        .sumOf { it.valor }

    val saidas = filtradas
        .filter { it.tipo == TipoMovimentacao.SAIDA }
        .sumOf { it.valor }

    val saldo = entradas - saidas

    val selecionadas = when (visualizacao) {
        "Entradas" -> filtradas.filter {
            it.tipo == TipoMovimentacao.ENTRADA
        }
        "Saídas" -> filtradas.filter {
            it.tipo == TipoMovimentacao.SAIDA
        }
        else -> filtradas
    }

    val exibidas = if (
        visualizacao != "Todas" || mostrarTodas
    ) {
        selecionadas
    } else {
        selecionadas.take(4)
    }

    fun voltar() {
        if (visualizacao != "Todas" || mostrarTodas) {
            visualizacao = "Todas"
            mostrarTodas = false
        } else {
            onVoltar()
        }
    }

    BackHandler(
        enabled = !mostrarCadastro && movimentacaoExcluir == null
    ) {
        voltar()
    }

    // JANELA DE CONFIRMAÇÃO DE EXCLUSÃO
    // Única parte com o novo design solicitado.
    movimentacaoExcluir?.let { selecionada ->
        val coresDialogo = MaterialTheme.colorScheme
        val modoEscuro = coresDialogo.background.luminance() < 0.5f

        val fundoDialogo = if (modoEscuro) {
            Color(0xFF182D3B)
        } else {
            Color.White
        }

        val fundoDetalhes = if (modoEscuro) {
            Color(0xFF203A49)
        } else {
            Color(0xFFF5F7F8)
        }

        val corTexto = if (modoEscuro) {
            Color(0xFFF4F7FA)
        } else {
            CaixaAzul
        }

        val corSecundaria = if (modoEscuro) {
            Color(0xFFC0CED6)
        } else {
            CaixaCinza
        }

        val corBorda = if (modoEscuro) {
            Color(0xFF526B7A)
        } else {
            Color(0xFFDDE4E7)
        }

        val corExclusao = if (modoEscuro) {
            Color(0xFFFFB4AB)
        } else {
            CaixaVermelho
        }

        val corBotaoExcluir = if (modoEscuro) {
            Color(0xFFC43B31)
        } else {
            CaixaVermelho
        }

        AlertDialog(
            onDismissRequest = {
                if (!excluindo) {
                    movimentacaoExcluir = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(22.dp),
            containerColor = fundoDialogo,
            icon = {
                Surface(
                    color = if (modoEscuro) {
                        Color(0xFF52332F)
                    } else {
                        Color(0xFFFCEAE7)
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = corExclusao,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Excluir movimentação?",
                    color = corTexto,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Confira os dados antes de excluir.",
                        color = corSecundaria,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = fundoDetalhes,
                        border = BorderStroke(1.dp, corBorda)
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Título",
                                    color = corSecundaria,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = selecionada.titulo.ifBlank {
                                        "Sem título"
                                    },
                                    color = corTexto,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Descrição",
                                    color = corSecundaria,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = selecionada.descricao.ifBlank {
                                        "Não informada"
                                    },
                                    color = corTexto,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Data",
                                    color = corSecundaria,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = dataCaixa(selecionada),
                                    color = corTexto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            HorizontalDivider(color = corBorda)

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Valor da movimentação",
                                    color = corSecundaria,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = moedaCaixa(selecionada.valor),
                                    color = corTexto,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement =
                            Arrangement.spacedBy(9.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AlertTriangle,
                            contentDescription = null,
                            tint = corExclusao,
                            modifier = Modifier.size(19.dp)
                        )

                        Text(
                            text = "Esta ação é permanente e não poderá ser desfeita.",
                            color = corExclusao,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !excluindo,
                    onClick = {
                        if (!excluindo) {
                            excluindo = true

                            scope.launch {
                                val resultado = withContext(
                                    Dispatchers.IO
                                ) {
                                    if (selecionada.id > 0L) {
                                        RecebimentoRepository.excluir(
                                            selecionada.id
                                        )
                                    } else {
                                        MovimentacaoCaixaRepository.excluir(
                                            -selecionada.id
                                        )
                                    }
                                }

                                excluindo = false

                                resultado.fold(
                                    onSuccess = {
                                        movimentacaoExcluir = null
                                        atualizacao++
                                        mensagemSucesso =
                                            "Movimentação excluída com sucesso."
                                    },
                                    onFailure = { falha ->
                                        movimentacaoExcluir = null
                                        erroCaixa = falha.message
                                            ?: "Não foi possível excluir a movimentação."
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = corBotaoExcluir,
                        contentColor = Color.White
                    )
                ) {
                    if (excluindo) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(19.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "Excluir",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    enabled = !excluindo,
                    onClick = {
                        movimentacaoExcluir = null
                    },
                    modifier = Modifier.heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, corBorda),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = corTexto
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    mensagemSucesso?.let { mensagem ->
        AlertDialog(
            onDismissRequest = {
                mensagemSucesso = null
            },
            title = {
                Text("Caixa atualizado")
            },
            text = {
                Text(mensagem)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensagemSucesso = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (erroCaixa != null) {
        AlertDialog(
            onDismissRequest = {
                erroCaixa = null
            },
            title = {
                Text(
                    "Erro no caixa",
                    color = CaixaAzul
                )
            },
            text = {
                Text(
                    erroCaixa ?: "Não foi possível acessar o caixa.",
                    color = CaixaCinza
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        erroCaixa = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        "Entendi",
                        color = CaixaAzul
                    )
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = CaixaFundo,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    mostrarCadastro = true
                },
                containerColor = CaixaAzul,
                contentColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription =
                        "Registrar nova movimentação"
                )
            }
        },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp)
                    .background(CaixaAzul)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        voltar()
                    },
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = when (visualizacao) {
                        "Entradas" -> "Entradas"
                        "Saídas" -> "Saídas"
                        else -> if (mostrarTodas) {
                            "Movimentações"
                        } else {
                            "Caixa"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(48.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 95.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            if (visualizacao == "Todas" && !mostrarTodas) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumo financeiro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CaixaAzul,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { heading() }
                        )

                        CaixaPeriodoAcessivel(
                            titulo = periodos[periodo].third,
                            periodos = periodos.map { it.third },
                            selecionado = periodo,
                            aberto = menuPeriodo,
                            onAbrir = {
                                menuPeriodo = true
                            },
                            onFechar = {
                                menuPeriodo = false
                            },
                            onSelecionar = {
                                periodo = it
                                menuPeriodo = false
                            }
                        )
                    }
                }

                item {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            CaixaResumoAcessivel(
                                titulo = "Entradas",
                                valor = entradas,
                                cor = CaixaVerde,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    visualizacao = "Entradas"
                                }
                            )

                            CaixaResumoAcessivel(
                                titulo = "Saídas",
                                valor = saidas,
                                cor = CaixaLaranja,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    visualizacao = "Saídas"
                                }
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                CaixaBorda
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .semantics(
                                        mergeDescendants = true
                                    ) {
                                        contentDescription =
                                            "Saldo do período: ${moedaCaixa(saldo)}"
                                    }
                            ) {
                                Text(
                                    text = "Saldo do período",
                                    color = CaixaCinza,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = moedaCaixa(saldo),
                                    color = CaixaAzul,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (visualizacao) {
                                "Entradas" -> "Entradas recebidas"
                                "Saídas" -> "Saídas registradas"
                                else -> if (mostrarTodas) {
                                    "Todas as movimentações"
                                } else {
                                    "Últimas movimentações"
                                }
                            },
                            color = CaixaAzul,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { heading() }
                        )

                        if (
                            visualizacao == "Todas" &&
                            !mostrarTodas &&
                            filtradas.size > 4
                        ) {
                            TextButton(
                                onClick = {
                                    mostrarTodas = true
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    "Ver todas",
                                    color = CaixaAzul
                                )
                            }
                        }
                    }

                    if (visualizacao != "Todas") {
                        Text(
                            text = if (visualizacao == "Entradas") {
                                "Total recebido: ${moedaCaixa(entradas)}"
                            } else {
                                "Total gasto: ${moedaCaixa(saidas)}"
                            },
                            color = CaixaAzul,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    if (visualizacao != "Todas" || mostrarTodas) {
                        CaixaPeriodoAcessivel(
                            titulo = periodos[periodo].third,
                            periodos = periodos.map { it.third },
                            selecionado = periodo,
                            aberto = menuPeriodo,
                            onAbrir = {
                                menuPeriodo = true
                            },
                            onFechar = {
                                menuPeriodo = false
                            },
                            onSelecionar = {
                                periodo = it
                                menuPeriodo = false
                            }
                        )
                    }

                    Text(
                        text = "${selecionadas.size} movimentações neste período",
                        color = CaixaCinza,
                        fontSize = 12.sp,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }

            if (carregandoCaixa) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = CaixaAzul
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = "Carregando movimentações...",
                            color = CaixaCinza,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                    }
                }
            } else if (exibidas.isEmpty()) {
                item {
                    Text(
                        text = when (visualizacao) {
                            "Entradas" ->
                                "Nenhuma entrada neste período."
                            "Saídas" ->
                                "Nenhuma saída neste período."
                            else ->
                                "Nenhuma movimentação neste período."
                        },
                        color = CaixaCinza,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                    )
                }
            } else {
                items(
                    items = exibidas,
                    key = { it.id }
                ) { movimentacao ->
                    CaixaLinhaAcessivel(
                        movimentacao = movimentacao,
                        habilitado = !excluindo,
                        onExcluir = {
                            movimentacaoExcluir = movimentacao
                        }
                    )

                    HorizontalDivider(
                        color = CaixaBorda.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (mostrarCadastro) {
        CaixaCadastroAcessivel(
            salvando = salvando,
            onCancelar = {
                if (!salvando) {
                    mostrarCadastro = false
                }
            },
            onSalvar = { titulo, descricao, valor, tipo ->
                if (!salvando) {
                    salvando = true

                    scope.launch {
                        val resultado = withContext(
                            Dispatchers.IO
                        ) {
                            MovimentacaoCaixaRepository.cadastrar(
                                tipo = tipo.name,
                                categoria = titulo,
                                descricao = descricao.ifBlank {
                                    titulo
                                },
                                valor = valor
                            )
                        }

                        salvando = false

                        resultado.fold(
                            onSuccess = {
                                mostrarCadastro = false
                                periodo = 0

                                visualizacao = if (
                                    tipo == TipoMovimentacao.ENTRADA
                                ) {
                                    "Entradas"
                                } else {
                                    "Saídas"
                                }

                                mostrarTodas = false
                                atualizacao++
                            },
                            onFailure = {
                                erroCaixa = it.message
                                    ?: "Não foi possível salvar a movimentação."
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun CaixaPeriodoAcessivel(
    titulo: String,
    periodos: List<String>,
    selecionado: Int,
    aberto: Boolean,
    onAbrir: () -> Unit,
    onFechar: () -> Unit,
    onSelecionar: (Int) -> Unit
) {
    val cores = MaterialTheme.colorScheme
    val modoEscuro = cores.background.luminance() < 0.5f

    val fundoBotao = if (modoEscuro) {
        Color(0xFF193447)
    } else {
        Color.White
    }

    val fundoMenu = if (modoEscuro) {
        Color(0xFF203A49)
    } else {
        Color.White
    }

    val fundoSelecionado = if (modoEscuro) {
        Color(0xFF345365)
    } else {
        Color(0xFFE8EFEF)
    }

    val texto = if (modoEscuro) {
        Color(0xFFF4F7FA)
    } else {
        CaixaAzul
    }

    val borda = if (modoEscuro) {
        Color(0xFF8195A3)
    } else {
        CaixaBorda
    }

    val verdeCheck = if (modoEscuro) {
        Color(0xFFA2DDBB)
    } else {
        CaixaVerde
    }

    Box {
        OutlinedButton(
            onClick = onAbrir,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(1.dp, borda),
            contentPadding = PaddingValues(
                horizontal = 12.dp,
                vertical = 5.dp
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = texto,
                containerColor = fundoBotao
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }

        DropdownMenu(
            expanded = aberto,
            onDismissRequest = onFechar,
            containerColor = fundoMenu,
            shape = RoundedCornerShape(14.dp)
        ) {
            periodos.forEachIndexed { indice, nome ->
                val ativo = indice == selecionado

                DropdownMenuItem(
                    modifier = Modifier
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                        .background(
                            color = if (ativo) {
                                fundoSelecionado
                            } else {
                                fundoMenu
                            },
                            shape = RoundedCornerShape(9.dp)
                        ),
                    text = {
                        Text(
                            text = nome,
                            color = texto,
                            fontSize = 14.sp,
                            fontWeight = if (ativo) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )
                    },
                    onClick = {
                        onSelecionar(indice)
                    },
                    trailingIcon = {
                        if (ativo) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Mês selecionado",
                                tint = verdeCheck,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = texto,
                        trailingIconColor = verdeCheck
                    )
                )
            }
        }
    }
}

@Composable
private fun CaixaResumoAcessivel(
    titulo: String,
    valor: Double,
    cor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 120.dp)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Ver $titulo. Total: ${moedaCaixa(valor)}"
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (titulo == "Entradas") {
                Color(0xFFE5F4EC)
            } else {
                Color(0xFFF6E8D8)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (titulo == "Entradas") {
                    Icons.Outlined.ArrowDownward
                } else {
                    Icons.Outlined.ArrowUpward
                },
                contentDescription = null,
                tint = cor
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = titulo,
                color = CaixaAzul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = moedaCaixa(valor),
                color = CaixaAzul,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun CaixaLinhaAcessivel(
    movimentacao: MovimentacaoUi,
    habilitado: Boolean,
    onExcluir: () -> Unit
) {
    val tipoTexto = if (
        movimentacao.tipo == TipoMovimentacao.ENTRADA
    ) {
        "Entrada"
    } else {
        "Saída"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "$tipoTexto. ${movimentacao.titulo}. " +
                            "${movimentacao.descricao}. " +
                            "Valor: ${moedaCaixa(movimentacao.valor)}. " +
                            "Data: ${dataCaixa(movimentacao)}."
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (tipoTexto == "Entrada") {
                Color(0xFFE5F4EC)
            } else {
                Color(0xFFF6E8D8)
            },
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = if (tipoTexto == "Entrada") {
                    Icons.Outlined.ArrowDownward
                } else {
                    Icons.Outlined.ArrowUpward
                },
                contentDescription = null,
                tint = if (tipoTexto == "Entrada") {
                    CaixaVerde
                } else {
                    CaixaLaranja
                },
                modifier = Modifier
                    .padding(10.dp)
                    .size(21.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = movimentacao.titulo,
                color = CaixaAzul,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = movimentacao.descricao,
                color = CaixaCinza,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Text(
                text = "$tipoTexto: ${moedaCaixa(movimentacao.valor)}",
                color = CaixaAzul,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = dataCaixa(movimentacao),
                color = CaixaCinza,
                fontSize = 12.sp
            )

            IconButton(
                onClick = onExcluir,
                enabled = habilitado,
                modifier = Modifier.sizeIn(
                    minWidth = 48.dp,
                    minHeight = 48.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription =
                        "Excluir ${movimentacao.titulo}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// FORMULÁRIO DE NOVA MOVIMENTAÇÃO
// Campos, validação e confirmação preservados.

@Composable
private fun CaixaCadastroAcessivel(
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (
        String,
        String,
        Double,
        TipoMovimentacao
    ) -> Unit
) {
    var tipo by remember {
        mutableStateOf(TipoMovimentacao.ENTRADA)
    }

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var tentouSalvar by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf(false) }

    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current
    val cores = MaterialTheme.colorScheme
    val modoEscuro = cores.background.luminance() < 0.5f

    val corDialogo = if (modoEscuro) {
        Color(0xFF182D3B)
    } else {
        CaixaFundo
    }

    val corCampo = if (modoEscuro) {
        Color(0xFF203A49)
    } else {
        Color.White
    }

    val corTexto = if (modoEscuro) {
        Color(0xFFF4F7F8)
    } else {
        CaixaAzul
    }

    val corSecundaria = if (modoEscuro) {
        Color(0xFFC0CED6)
    } else {
        CaixaCinza
    }

    val corBorda = if (modoEscuro) {
        Color(0xFF8195A3)
    } else {
        CaixaBorda
    }

    val corChipAtivo = if (modoEscuro) {
        Color(0xFF345366)
    } else {
        Color(0xFFF6E8D8)
    }

    val corErro = if (modoEscuro) {
        cores.error
    } else {
        CaixaVermelho
    }

    val valorConvertido = valorCaixaDigitado(valor)
    val tituloInvalido = tentouSalvar && titulo.isBlank()

    val valorInvalido = tentouSalvar && (
            valorConvertido == null ||
                    !valorConvertido.isFinite() ||
                    valorConvertido <= 0.0
            )

    val tipoTexto = if (
        tipo == TipoMovimentacao.ENTRADA
    ) {
        "Entrada"
    } else {
        "Saída"
    }

    val formularioValido =
        titulo.isNotBlank() &&
                valorConvertido != null &&
                valorConvertido.isFinite() &&
                valorConvertido > 0.0

    fun validar() {
        tentouSalvar = true

        erro = when {
            titulo.isBlank() ->
                "Informe o título da movimentação."

            valorConvertido == null ||
                    !valorConvertido.isFinite() ||
                    valorConvertido <= 0.0 ->
                "Informe um valor maior que zero."

            else -> ""
        }

        if (erro.isBlank()) {
            teclado?.hide()
            foco.clearFocus()
            confirmar = true
        }
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = corCampo,
        unfocusedContainerColor = corCampo,
        disabledContainerColor = corCampo,
        focusedTextColor = corTexto,
        unfocusedTextColor = corTexto,
        disabledTextColor = corTexto.copy(alpha = 0.7f),
        focusedBorderColor = corBorda,
        unfocusedBorderColor = corBorda,
        disabledBorderColor = corBorda.copy(alpha = 0.5f),
        focusedLabelColor = corSecundaria,
        unfocusedLabelColor = corSecundaria,
        focusedPlaceholderColor = corSecundaria,
        unfocusedPlaceholderColor = corSecundaria,
        cursorColor = corTexto,
        errorBorderColor = corErro,
        errorLabelColor = corErro
    )

    Dialog(
        onDismissRequest = {
            if (!salvando) {
                onCancelar()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !salvando,
            dismissOnClickOutside = !salvando,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(
                    horizontal = 12.dp,
                    vertical = 16.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(22.dp),
                color = corDialogo,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Nova movimentação",
                        color = corTexto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Registre uma entrada ou saída.",
                        color = corSecundaria,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(
                        color = corBorda.copy(alpha = 0.35f)
                    )

                    Spacer(Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement =
                            Arrangement.spacedBy(13.dp)
                    ) {
                        Text(
                            text = "Tipo de movimentação",
                            color = corTexto,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(9.dp)
                        ) {
                            listOf(
                                TipoMovimentacao.ENTRADA,
                                TipoMovimentacao.SAIDA
                            ).forEach { opcao ->
                                val selecionada = tipo == opcao
                                val nome = if (
                                    opcao == TipoMovimentacao.ENTRADA
                                ) {
                                    "Entrada"
                                } else {
                                    "Saída"
                                }

                                FilterChip(
                                    selected = selecionada,
                                    onClick = {
                                        tipo = opcao
                                        erro = ""
                                    },
                                    label = {
                                        Text(
                                            nome,
                                            fontSize = 14.sp,
                                            fontWeight =
                                                FontWeight.SemiBold
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    shape =
                                        RoundedCornerShape(11.dp),
                                    colors =
                                        FilterChipDefaults
                                            .filterChipColors(
                                                containerColor =
                                                    corCampo,
                                                selectedContainerColor =
                                                    corChipAtivo,
                                                labelColor = corTexto,
                                                selectedLabelColor =
                                                    corTexto
                                            ),
                                    border =
                                        FilterChipDefaults
                                            .filterChipBorder(
                                                enabled = true,
                                                selected = selecionada,
                                                borderColor = corBorda,
                                                selectedBorderColor =
                                                    CobreCaixa
                                            )
                                )
                            }
                        }

                        Spacer(Modifier.height(1.dp))

                        OutlinedTextField(
                            value = titulo,
                            onValueChange = {
                                titulo = it
                                erro = ""
                            },
                            label = {
                                Text("Título *")
                            },
                            placeholder = {
                                Text(
                                    if (
                                        tipo == TipoMovimentacao.ENTRADA
                                    ) {
                                        "Ex.: Pagamento recebido"
                                    } else {
                                        "Ex.: Compra de materiais"
                                    }
                                )
                            },
                            singleLine = true,
                            enabled = !salvando,
                            isError = tituloInvalido,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (tituloInvalido) {
                                        Modifier.semantics {
                                            error(
                                                "Informe o título da movimentação."
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            colors = coresCampo,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (tituloInvalido) {
                            CaixaMensagemErro(
                                "Informe o título da movimentação.",
                                cor = corErro
                            )
                        }

                        OutlinedTextField(
                            value = descricao,
                            onValueChange = {
                                descricao = it
                            },
                            label = {
                                Text("Descrição (opcional)")
                            },
                            placeholder = {
                                Text("Detalhes da movimentação")
                            },
                            minLines = 2,
                            maxLines = 3,
                            enabled = !salvando,
                            modifier = Modifier.fillMaxWidth(),
                            colors = coresCampo,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = valor,
                            onValueChange = {
                                valor = it
                                erro = ""
                            },
                            label = {
                                Text("Valor (R$) *")
                            },
                            placeholder = {
                                Text("0,00")
                            },
                            singleLine = true,
                            enabled = !salvando,
                            isError = valorInvalido,
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    teclado?.hide()
                                    foco.clearFocus()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (valorInvalido) {
                                        Modifier.semantics {
                                            error(
                                                "Informe um valor maior que zero."
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            colors = coresCampo,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (valorInvalido) {
                            CaixaMensagemErro(
                                "Informe um valor maior que zero.",
                                cor = corErro
                            )
                        }

                        if (
                            erro.isNotBlank() &&
                            !tituloInvalido &&
                            !valorInvalido
                        ) {
                            CaixaMensagemErro(
                                erro,
                                cor = corErro
                            )
                        }

                        Text(
                            text = "* Campos obrigatórios",
                            color = corSecundaria,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(15.dp))

                    HorizontalDivider(
                        color = corBorda.copy(alpha = 0.35f)
                    )

                    Spacer(Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                teclado?.hide()
                                onCancelar()
                            },
                            enabled = !salvando,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, corBorda),
                            colors =
                                ButtonDefaults
                                    .outlinedButtonColors(
                                        containerColor =
                                            Color.Transparent,
                                        contentColor = corTexto,
                                        disabledContentColor =
                                            corSecundaria
                                    )
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                validar()
                            },
                            enabled = !salvando &&
                                    formularioValido,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CaixaAzul,
                                contentColor = Color.White,
                                disabledContainerColor =
                                    if (modoEscuro) {
                                        Color(0xFF355367)
                                    } else {
                                        Color(0xFFB9C5CA)
                                    },
                                disabledContentColor =
                                    if (modoEscuro) {
                                        Color(0xFFD5DFE5)
                                    } else {
                                        Color(0xFF435965)
                                    }
                            )
                        ) {
                            if (salvando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Continuar",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = {
                if (!salvando) {
                    confirmar = false
                }
            },
            title = {
                Text(
                    text = "Confirmar $tipoTexto?",
                    color = corTexto,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = "Confira os dados antes de registrar.",
                        color = corSecundaria
                    )

                    Text(
                        text = "Tipo: $tipoTexto",
                        color = corTexto
                    )

                    Text(
                        text = "Título: ${titulo.trim()}",
                        color = corTexto
                    )

                    Text(
                        text = "Valor: ${
                            moedaCaixa(valorConvertido ?: 0.0)
                        }",
                        color = corTexto,
                        fontWeight = FontWeight.Bold
                    )

                    if (descricao.isNotBlank()) {
                        Text(
                            text = "Descrição: ${descricao.trim()}",
                            color = corSecundaria
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val numero = valorConvertido

                        if (
                            numero != null &&
                            numero.isFinite() &&
                            numero > 0.0 &&
                            titulo.isNotBlank() &&
                            !salvando
                        ) {
                            confirmar = false

                            onSalvar(
                                titulo.trim(),
                                descricao.trim(),
                                numero,
                                tipo
                            )
                        }
                    },
                    enabled = !salvando,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CaixaAzul,
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmar = false
                    },
                    enabled = !salvando,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        "Corrigir",
                        color = corTexto
                    )
                }
            },
            containerColor = corDialogo
        )
    }
}

@Composable
private fun CaixaMensagemErro(
    mensagem: String,
    cor: Color = CaixaVermelho
) {
    Text(
        text = mensagem,
        color = cor,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        }
    )
}
