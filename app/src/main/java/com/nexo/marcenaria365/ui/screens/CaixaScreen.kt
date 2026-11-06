
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

private val CaixaAzul = Color(0xFF193447)
private val CaixaFundo = Color(0xFFFAF6F0)
private val CaixaVerde = Color(0xFF237A58)
private val CaixaLaranja = Color(0xFF94613C)
private val CaixaCinza = Color(0xFF53616A)
private val CaixaBorda = Color(0xFF89969E)
private val CaixaVermelho = Color(0xFFB3261E)

private fun valorCaixaDigitado(texto: String): Double? {
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
fun CaixaScreen(
    onVoltar: () -> Unit,
    servicos: List<ServicoUi>
) {
    val scope = rememberCoroutineScope()
    val cores = MaterialTheme.colorScheme

    val fundo = if (cores.background.luminanceCaixa() < 0.5f) {
        cores.background
    } else {
        CaixaFundo
    }

    val cartao = cores.surface
    val textoPrincipal = cores.onSurface
    val textoSecundario = cores.onSurfaceVariant

    val movimentacoes = remember {
        mutableStateListOf<MovimentacaoUi>()
    }

    var erroCaixa by remember {
        mutableStateOf<String?>(null)
    }

    var mensagemSucesso by remember {
        mutableStateOf<String?>(null)
    }

    var carregandoCaixa by remember {
        mutableStateOf(true)
    }

    var salvando by remember {
        mutableStateOf(false)
    }

    var excluindo by remember {
        mutableStateOf(false)
    }

    var atualizacao by remember {
        mutableIntStateOf(0)
    }

    var mostrarCadastro by remember {
        mutableStateOf(false)
    }

    // Guarda o lançamento selecionado para exclusão.
    var movimentacaoExcluir by remember {
        mutableStateOf<MovimentacaoUi?>(null)
    }

    var periodo by rememberSaveable {
        mutableIntStateOf(0)
    }

    var menuPeriodo by remember {
        mutableStateOf(false)
    }

    var mostrarTodas by rememberSaveable {
        mutableStateOf(false)
    }

    var visualizacao by rememberSaveable {
        mutableStateOf("Todas")
    }

    // Recarrega os dados diretamente do backend.
    LaunchedEffect(atualizacao, servicos) {
        carregandoCaixa = true

        val servicosPorId = servicos.associateBy {
            it.id
        }

        val resultado = withContext(Dispatchers.IO) {
            runCatching {
                val recebimentos =
                    RecebimentoRepository.listar().getOrThrow()

                val avulsas =
                    MovimentacaoCaixaRepository.listar()
                        .getOrThrow()

                val entradasRecebidas = recebimentos.mapNotNull {
                        recebimento ->

                    if (!recebimento.status.equals(
                            "RECEBIDO",
                            ignoreCase = true
                        )
                    ) {
                        return@mapNotNull null
                    }

                    val data = recebimento.dataRecebimento
                        ?.let {
                            runCatching {
                                LocalDate.parse(it)
                            }.getOrNull()
                        } ?: return@mapNotNull null

                    val servico =
                        servicosPorId[recebimento.servicoId]

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

                val movimentosAvulsos = avulsas.mapNotNull {
                        movimento ->

                    // Evita somar duas vezes o mesmo recebimento.
                    if (movimento.recebimentoId != null) {
                        return@mapNotNull null
                    }

                    val data = runCatching {
                        LocalDate.parse(
                            movimento.dataMovimentacao
                        )
                    }.getOrNull() ?: return@mapNotNull null

                    MovimentacaoUi(
                        id = -movimento.id,
                        titulo = movimento.categoria,
                        descricao = movimento.descricao,
                        valor = movimento.valor,
                        tipo = if (
                            movimento.tipo.equals(
                                "ENTRADA",
                                ignoreCase = true
                            )
                        ) {
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
            onSuccess = { dados ->
                movimentacoes.clear()
                movimentacoes.addAll(dados)
                erroCaixa = null
            },
            onFailure = { falha ->
                erroCaixa = falha.message
                    ?: "Não foi possível carregar o caixa."
            }
        )

        carregandoCaixa = false
    }

    val periodos = remember {
        (0..5).map { deslocamento ->
            val calendario = Calendar.getInstance()
            calendario.add(Calendar.MONTH, -deslocamento)

            Triple(
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH) + 1,
                if (deslocamento == 0) {
                    "Este mês"
                } else {
                    SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.forLanguageTag("pt-BR")
                    ).format(calendario.time)
                        .replaceFirstChar { it.uppercase() }
                }
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
        .filter {
            it.tipo == TipoMovimentacao.ENTRADA
        }
        .sumOf { it.valor }

    val saidas = filtradas
        .filter {
            it.tipo == TipoMovimentacao.SAIDA
        }
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
        enabled = !mostrarCadastro &&
                movimentacaoExcluir == null
    ) {
        voltar()
    }

    // Confirmação antes de apagar qualquer registro.
    movimentacaoExcluir?.let { selecionada ->

        AlertDialog(
            onDismissRequest = {
                if (!excluindo) {
                    movimentacaoExcluir = null
                }
            },
            title = {
                Text(
                    "Excluir movimentação?",
                    color = textoPrincipal,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        "Você está prestes a excluir este lançamento:",
                        color = textoPrincipal
                    )

                    Text(
                        selecionada.titulo,
                        color = textoPrincipal,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        selecionada.descricao,
                        color = textoSecundario
                    )

                    Text(
                        moedaCaixa(selecionada.valor),
                        color = textoPrincipal,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Data: ${dataCaixa(selecionada)}",
                        color = textoSecundario
                    )

                    Text(
                        "Essa exclusão não poderá ser desfeita.",
                        color = cores.error,
                        fontSize = 13.sp
                    )
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
                                    // IDs positivos: recebimentos.
                                    // IDs negativos: lançamentos avulsos.
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

                                        // Recarrega os valores reais da API.
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cores.error
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    if (excluindo) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(19.dp),
                            color = cores.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Excluir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !excluindo,
                    onClick = {
                        movimentacaoExcluir = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancelar", color = textoPrincipal)
                }
            },
            containerColor = cartao
        )
    }

    // Mensagens de erro.
    erroCaixa?.let { mensagem ->
        AlertDialog(
            onDismissRequest = {
                erroCaixa = null
            },
            title = {
                Text(
                    "Erro no caixa",
                    color = textoPrincipal
                )
            },
            text = {
                Text(
                    mensagem,
                    color = textoSecundario
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        erroCaixa = null
                    }
                ) {
                    Text("Entendi")
                }
            },
            containerColor = cartao
        )
    }

    // Confirmação de sucesso.
    mensagemSucesso?.let { mensagem ->
        AlertDialog(
            onDismissRequest = {
                mensagemSucesso = null
            },
            title = {
                Text(
                    "Caixa atualizado",
                    color = textoPrincipal
                )
            },
            text = {
                Text(
                    mensagem,
                    color = textoSecundario
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensagemSucesso = null
                    }
                ) {
                    Text("OK")
                }
            },
            containerColor = cartao
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = fundo,
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
                        .semantics {
                            heading()
                        },
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        atualizacao++
                    },
                    enabled = !carregandoCaixa &&
                            !excluindo,
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Atualizar caixa",
                        tint = Color.White
                    )
                }
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
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumo financeiro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textoPrincipal,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    heading()
                                }
                        )

                        CaixaPeriodoAcessivel(
                            titulo = periodos[periodo].third,
                            periodos = periodos.map {
                                it.third
                            },
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
                            color = cartao,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                cores.outlineVariant
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
                                    color = textoSecundario,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = moedaCaixa(saldo),
                                    color = textoPrincipal,
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
                                "Entradas" ->
                                    "Entradas recebidas"

                                "Saídas" ->
                                    "Saídas registradas"

                                else -> if (mostrarTodas) {
                                    "Todas as movimentações"
                                } else {
                                    "Últimas movimentações"
                                }
                            },
                            color = textoPrincipal,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    heading()
                                }
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
                                modifier = Modifier.heightIn(
                                    min = 48.dp
                                )
                            ) {
                                Text("Ver todas")
                            }
                        }
                    }

                    if (visualizacao != "Todas") {
                        Text(
                            text = if (
                                visualizacao == "Entradas"
                            ) {
                                "Total recebido: ${moedaCaixa(entradas)}"
                            } else {
                                "Total gasto: ${moedaCaixa(saidas)}"
                            },
                            color = textoPrincipal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    if (
                        visualizacao != "Todas" ||
                        mostrarTodas
                    ) {
                        CaixaPeriodoAcessivel(
                            titulo = periodos[periodo].third,
                            periodos = periodos.map {
                                it.third
                            },
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
                        text =
                            "${selecionadas.size} movimentações neste período",
                        color = textoSecundario,
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
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = cores.primary
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = "Carregando movimentações...",
                            color = textoSecundario,
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
                        color = textoSecundario,
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

                    // Agora cada linha possui uma lixeira.
                    CaixaLinhaAcessivel(
                        movimentacao = movimentacao,
                        habilitado = !excluindo,
                        onExcluir = {
                            movimentacaoExcluir = movimentacao
                        }
                    )

                    HorizontalDivider(
                        color = cores.outlineVariant
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

    Box {
        OutlinedButton(
            onClick = onAbrir,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(
                1.dp,
                cores.outline
            ),
            contentPadding = PaddingValues(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = cores.onSurface,
                containerColor = cores.surface
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                titulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }

        DropdownMenu(
            expanded = aberto,
            onDismissRequest = onFechar
        ) {
            periodos.forEachIndexed { indice, nome ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = nome,
                            fontWeight = if (
                                indice == selecionado
                            ) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    onClick = {
                        onSelecionar(indice)
                    },
                    trailingIcon = {
                        if (indice == selecionado) {
                            Text(
                                "Selecionado",
                                color = cores.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
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
    val cores = MaterialTheme.colorScheme

    val fundoCartao = if (titulo == "Entradas") {
        if (cores.background.luminanceCaixa() < 0.5f) {
            cores.tertiaryContainer
        } else {
            Color(0xFFE5F4EC)
        }
    } else {
        if (cores.background.luminanceCaixa() < 0.5f) {
            cores.secondaryContainer
        } else {
            Color(0xFFF6E8D8)
        }
    }

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
            containerColor = fundoCartao
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (
                    titulo == "Entradas"
                ) {
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
                color = cores.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = moedaCaixa(valor),
                color = cores.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 21.sp
            )
        }
    }
}

/*
 * Linha de movimentação:
 * Mantém o desenho existente e acrescenta a lixeira.
 * O botão possui descrição própria para leitores de tela.
 */
@Composable
private fun CaixaLinhaAcessivel(
    movimentacao: MovimentacaoUi,
    habilitado: Boolean,
    onExcluir: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

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
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (tipoTexto == "Entrada") {
                if (cores.background.luminanceCaixa() < 0.5f) {
                    cores.tertiaryContainer
                } else {
                    Color(0xFFE5F4EC)
                }
            } else {
                if (cores.background.luminanceCaixa() < 0.5f) {
                    cores.secondaryContainer
                } else {
                    Color(0xFFF6E8D8)
                }
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

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        "$tipoTexto. ${movimentacao.titulo}. " +
                                "${movimentacao.descricao}. " +
                                "Valor: ${moedaCaixa(movimentacao.valor)}. " +
                                "Data: ${dataCaixa(movimentacao)}."
                },
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = movimentacao.titulo,
                color = cores.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = movimentacao.descricao,
                color = cores.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 3
            )

            Text(
                text = "$tipoTexto: ${moedaCaixa(movimentacao.valor)}",
                color = cores.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = dataCaixa(movimentacao),
                color = cores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        // BOTÃO DE EXCLUSÃO
        IconButton(
            onClick = onExcluir,
            enabled = habilitado,
            modifier = Modifier
                .sizeIn(
                    minWidth = 48.dp,
                    minHeight = 48.dp
                )
                .semantics {
                    contentDescription =
                        "Excluir ${movimentacao.titulo}, " +
                                "valor ${moedaCaixa(movimentacao.valor)}"
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = cores.error,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

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

    var titulo by remember {
        mutableStateOf("")
    }

    var descricao by remember {
        mutableStateOf("")
    }

    var valor by remember {
        mutableStateOf("")
    }

    var tentouSalvar by remember {
        mutableStateOf(false)
    }

    var erro by remember {
        mutableStateOf("")
    }

    var confirmar by remember {
        mutableStateOf(false)
    }

    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current
    val coresTema = MaterialTheme.colorScheme

    val modoEscuro =
        coresTema.background.luminanceCaixa() < 0.5f

    val corFundo = if (modoEscuro) {
        coresTema.surface
    } else {
        Color.White
    }

    val corTexto = coresTema.onSurface

    val valorConvertido = valorCaixaDigitado(valor)

    val tituloInvalido = tentouSalvar &&
            titulo.isBlank()

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

    val coresCampos = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = corFundo,
        unfocusedContainerColor = corFundo,
        focusedBorderColor = coresTema.primary,
        unfocusedBorderColor = coresTema.outline,
        focusedLabelColor = corTexto,
        unfocusedLabelColor = coresTema.onSurfaceVariant,
        focusedTextColor = corTexto,
        unfocusedTextColor = corTexto,
        cursorColor = coresTema.primary,
        errorBorderColor = coresTema.error,
        errorLabelColor = coresTema.error
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
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            color = if (modoEscuro) {
                coresTema.surface
            } else {
                CaixaFundo
            }
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Nova movimentação",
                    color = corTexto,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        heading()
                    }
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Registre uma entrada ou saída.",
                    color = coresTema.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tipo de movimentação",
                        color = corTexto,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
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
                                    Text(nome)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                colors = FilterChipDefaults
                                    .filterChipColors(
                                        containerColor = corFundo,
                                        selectedContainerColor =
                                            coresTema.primaryContainer,
                                        labelColor = corTexto,
                                        selectedLabelColor =
                                            coresTema.onPrimaryContainer
                                    ),
                                border = FilterChipDefaults
                                    .filterChipBorder(
                                        enabled = true,
                                        selected = selecionada,
                                        borderColor = coresTema.outline,
                                        selectedBorderColor =
                                            coresTema.primary
                                    )
                            )
                        }
                    }

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
                        colors = coresCampos,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (tituloInvalido) {
                        CaixaMensagemErro(
                            "Informe o título da movimentação."
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
                        minLines = 2,
                        maxLines = 3,
                        enabled = !salvando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = coresCampos,
                        shape = RoundedCornerShape(10.dp)
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
                            keyboardType = KeyboardType.Decimal,
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
                        colors = coresCampos,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (valorInvalido) {
                        CaixaMensagemErro(
                            "Informe um valor maior que zero."
                        )
                    }

                    if (erro.isNotBlank()) {
                        CaixaMensagemErro(erro)
                    }

                    Text(
                        text = "* Campos obrigatórios",
                        color = coresTema.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = !salvando,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp),
                        border = BorderStroke(
                            1.dp,
                            coresTema.outline
                        )
                    ) {
                        Text(
                            "Cancelar",
                            color = corTexto
                        )
                    }

                    Button(
                        onClick = {
                            validar()
                        },
                        enabled = !salvando,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CaixaAzul
                        )
                    ) {
                        if (salvando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Continuar")
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
                        color = coresTema.onSurfaceVariant
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
                            color = coresTema.onSurfaceVariant
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
                        containerColor = CaixaAzul
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
            containerColor = coresTema.surface
        )
    }
}

@Composable
private fun CaixaMensagemErro(mensagem: String) {
    val cores = MaterialTheme.colorScheme

    Text(
        text = mensagem,
        color = cores.error,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        }
    )
}

// Verifica o brilho do fundo para adaptar o modo escuro.
private fun Color.luminanceCaixa(): Float {
    return (
            0.2126f * red +
                    0.7152f * green +
                    0.0722f * blue
            )
}
