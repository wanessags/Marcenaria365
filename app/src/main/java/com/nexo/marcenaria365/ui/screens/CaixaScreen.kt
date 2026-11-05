
package com.nexo.marcenaria365.ui.screens.caixa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

private val Azul = Color(0xFF193447)
private val Fundo = Color(0xFFFAF6F0)
private val Verde = Color(0xFF239B7A)
private val Laranja = Color(0xFFC08B60)
private val Cinza = Color(0xFF66727B)
private val Borda = Color(0xFFE9E3DD)
private val Branco = Color.White

@Composable
fun CaixaScreen(
    onVoltar: () -> Unit,
    servicos: List<ServicoUi>
) {
    val movimentacoes = remember {
        mutableStateListOf<MovimentacaoUi>()
    }

    var erroCaixa by remember {
        mutableStateOf<String?>(null)
    }

    var carregandoCaixa by remember {
        mutableStateOf(true)
    }

    var atualizacaoCaixa by remember {
        mutableIntStateOf(0)
    }

    var salvandoMovimentacao by remember {
        mutableStateOf(false)
    }

    var mostrarCadastro by remember {
        mutableStateOf(false)
    }

    var periodo by rememberSaveable {
        mutableIntStateOf(0)
    }

    var menuPeriodoAberto by remember {
        mutableStateOf(false)
    }

    var mostrarTodas by rememberSaveable {
        mutableStateOf(false)
    }

    var visualizacao by rememberSaveable {
        mutableStateOf("Todas")
    }

    val scope = rememberCoroutineScope()

    // CARREGA AS MOVIMENTACOES DO BACKEND
    LaunchedEffect(atualizacaoCaixa) {
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

                val entradasRecebidas =
                    recebimentos.mapNotNull { recebimento ->

                        if (
                            !recebimento.status.equals(
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
                            }
                            ?: return@mapNotNull null

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

                val movimentosAvulsos =
                    avulsas.mapNotNull { movimento ->

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
                                movimento.tipo == "ENTRADA"
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
            onSuccess = { lista ->
                movimentacoes.clear()
                movimentacoes.addAll(lista)
                erroCaixa = null
            },
            onFailure = { erro ->
                erroCaixa = erro.message
                    ?: "Não foi possível carregar o caixa."
            }
        )

        carregandoCaixa = false
    }

    // PERIODOS
    val periodos = remember {
        (0..5).map { deslocamento ->
            val calendario = Calendar.getInstance()

            calendario.add(
                Calendar.MONTH,
                -deslocamento
            )

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
                        .replaceFirstChar {
                            it.uppercase()
                        }
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
            compareByDescending<MovimentacaoUi> {
                it.ano
            }
                .thenByDescending { it.mes }
                .thenByDescending { it.dia }
                .thenByDescending { it.id }
        )

    val entradas = filtradas
        .filter {
            it.tipo == TipoMovimentacao.ENTRADA
        }
        .sumOf {
            it.valor
        }

    val saidas = filtradas
        .filter {
            it.tipo == TipoMovimentacao.SAIDA
        }
        .sumOf {
            it.valor
        }

    val saldo = entradas - saidas

    val movimentacoesSelecionadas = when (visualizacao) {
        "Entradas" -> filtradas.filter {
            it.tipo == TipoMovimentacao.ENTRADA
        }

        "Saídas" -> filtradas.filter {
            it.tipo == TipoMovimentacao.SAIDA
        }

        else -> filtradas
    }

    val exibidas = when {
        visualizacao != "Todas" ->
            movimentacoesSelecionadas

        mostrarTodas ->
            movimentacoesSelecionadas

        else ->
            movimentacoesSelecionadas.take(4)
    }

    fun voltarTela() {
        if (visualizacao != "Todas" || mostrarTodas) {
            visualizacao = "Todas"
            mostrarTodas = false
        } else {
            onVoltar()
        }
    }

    BackHandler(
        enabled = !mostrarCadastro
    ) {
        voltarTela()
    }

    // MENSAGENS DE ERRO
    if (erroCaixa != null) {
        AlertDialog(
            onDismissRequest = {
                erroCaixa = null
            },
            title = {
                Text("Caixa")
            },
            text = {
                Text(
                    erroCaixa
                        ?: "Erro ao carregar movimentações."
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
            containerColor = Branco
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Fundo,

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    mostrarCadastro = true
                },
                containerColor = Azul,
                contentColor = Branco,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription =
                        "Registrar movimentação"
                )
            }
        },

        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(Azul)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        voltarTela()
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Branco
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
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Branco,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(48.dp)
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = 22.dp,
                bottom = 95.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(17.dp)
        ) {

            // RESUMO FINANCEIRO
            if (
                visualizacao == "Todas" &&
                !mostrarTodas
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumo financeiro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Azul,
                            modifier = Modifier.weight(1f)
                        )

                        SeletorPeriodoCaixa(
                            titulo = periodos[periodo].third,
                            aberto = menuPeriodoAberto,
                            periodos = periodos.map {
                                it.third
                            },
                            selecionado = periodo,
                            onAbrir = {
                                menuPeriodoAberto = true
                            },
                            onFechar = {
                                menuPeriodoAberto = false
                            },
                            onSelecionar = { indice ->
                                periodo = indice
                                menuPeriodoAberto = false
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        ResumoCaixaCard(
                            titulo = "Entradas",
                            valor = entradas,
                            cor = Verde,
                            fundo = Color(0xFFE2F3EC),
                            entrada = true,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                visualizacao = "Entradas"
                            }
                        )

                        ResumoCaixaCard(
                            titulo = "Saídas",
                            valor = saidas,
                            cor = Laranja,
                            fundo = Color(0xFFF8E8D7),
                            entrada = false,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                visualizacao = "Saídas"
                            }
                        )

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(142.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFFEFEFED)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 5.dp),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Center
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.Wallet,
                                    contentDescription = null,
                                    tint = Azul,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = "Saldo",
                                    fontSize = 12.sp,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    color = Azul
                                )

                                Spacer(Modifier.height(9.dp))

                                Text(
                                    text = moedaCaixa(saldo),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Azul,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // TITULO DAS MOVIMENTACOES
            item {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
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
                            color = Azul,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (
                            visualizacao == "Todas" &&
                            !mostrarTodas &&
                            filtradas.size > 4
                        ) {
                            TextButton(
                                onClick = {
                                    mostrarTodas = true
                                }
                            ) {
                                Text(
                                    text = "Ver todas",
                                    color = Azul,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (visualizacao != "Todas") {
                        Text(
                            text = if (
                                visualizacao == "Entradas"
                            ) {
                                "Total recebido: ${
                                    moedaCaixa(entradas)
                                }"
                            } else {
                                "Total gasto: ${
                                    moedaCaixa(saidas)
                                }"
                            },
                            color = if (
                                visualizacao == "Entradas"
                            ) {
                                Verde
                            } else {
                                Laranja
                            },
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    if (
                        visualizacao != "Todas" ||
                        mostrarTodas
                    ) {
                        SeletorPeriodoCaixa(
                            titulo = periodos[periodo].third,
                            aberto = menuPeriodoAberto,
                            periodos = periodos.map {
                                it.third
                            },
                            selecionado = periodo,
                            onAbrir = {
                                menuPeriodoAberto = true
                            },
                            onFechar = {
                                menuPeriodoAberto = false
                            },
                            onSelecionar = { indice ->
                                periodo = indice
                                menuPeriodoAberto = false
                            }
                        )
                    }
                }
            }

            // LISTA
            if (carregandoCaixa) {
                item {
                    Row(
                        modifier = Modifier.padding(
                            vertical = 12.dp
                        ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Azul,
                            strokeWidth = 2.dp
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = "Carregando movimentações...",
                            color = Cinza,
                            fontSize = 12.sp
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
                        color = Cinza,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            vertical = 25.dp
                        )
                    )
                }
            } else {
                items(
                    items = exibidas,
                    key = { it.id }
                ) { movimentacao ->
                    LinhaMovimentacao(movimentacao)
                }
            }
        }
    }

    // FORMULARIO NOVO E MAIS LARGO
    if (mostrarCadastro) {
        CadastroMovimentacaoDialog(
            salvando = salvandoMovimentacao,
            onCancelar = {
                if (!salvandoMovimentacao) {
                    mostrarCadastro = false
                }
            },
            onSalvar = { titulo, descricao, valor, tipo ->
                if (!salvandoMovimentacao) {
                    salvandoMovimentacao = true

                    scope.launch {
                        val resultado =
                            withContext(Dispatchers.IO) {
                                MovimentacaoCaixaRepository
                                    .cadastrar(
                                        tipo = tipo.name,
                                        categoria = titulo,
                                        descricao =
                                            descricao.ifBlank {
                                                titulo
                                            },
                                        valor = valor
                                    )
                            }

                        salvandoMovimentacao = false

                        resultado.fold(
                            onSuccess = {
                                mostrarCadastro = false
                                periodo = 0

                                visualizacao = if (
                                    tipo ==
                                    TipoMovimentacao.ENTRADA
                                ) {
                                    "Entradas"
                                } else {
                                    "Saídas"
                                }

                                atualizacaoCaixa++
                            },
                            onFailure = { erro ->
                                erroCaixa = erro.message
                                    ?: "Não foi possível salvar."
                            }
                        )
                    }
                }
            }
        )
    }
}

// SELETOR DE MES
@Composable
private fun SeletorPeriodoCaixa(
    titulo: String,
    aberto: Boolean,
    periodos: List<String>,
    selecionado: Int,
    onAbrir: () -> Unit,
    onFechar: () -> Unit,
    onSelecionar: (Int) -> Unit
) {
    Box {
        Surface(
            onClick = onAbrir,
            shape = RoundedCornerShape(12.dp),
            color = Branco,
            border = BorderStroke(1.dp, Borda)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 10.dp
                ),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Laranja,
                    modifier = Modifier.size(17.dp)
                )

                Text(
                    text = titulo,
                    color = Azul,
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    maxLines = 1
                )

                Icon(
                    imageVector =
                        Icons.Outlined.KeyboardArrowDown,
                    contentDescription =
                        "Selecionar período",
                    tint = Cinza,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        DropdownMenu(
            expanded = aberto,
            onDismissRequest = onFechar,
            modifier = Modifier.background(Branco)
        ) {
            periodos.forEachIndexed { indice, nome ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = nome,
                            color = Azul,
                            fontSize = 13.sp,
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
                                text = "✓",
                                color = Verde
                            )
                        }
                    }
                )
            }
        }
    }
}

// CARTOES DO RESUMO
@Composable
private fun ResumoCaixaCard(
    titulo: String,
    valor: Double,
    cor: Color,
    fundo: Color,
    entrada: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(142.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = fundo
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 6.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {
            Icon(
                imageVector = if (entrada) {
                    Icons.Outlined.ArrowDownward
                } else {
                    Icons.Outlined.ArrowUpward
                },
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = titulo,
                color = Azul,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = moedaCaixa(valor),
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ITEM DA LISTA
@Composable
private fun LinhaMovimentacao(
    movimentacao: MovimentacaoUi
) {
    val entrada =
        movimentacao.tipo == TipoMovimentacao.ENTRADA

    val cor = if (entrada) {
        Verde
    } else {
        Laranja
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(cor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (entrada) {
                    Icons.Outlined.ArrowDownward
                } else {
                    Icons.Outlined.ShoppingBag
                },
                contentDescription = null,
                tint = Branco,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = movimentacao.titulo,
                color = Azul,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = movimentacao.descricao,
                color = Cinza,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = moedaCaixa(movimentacao.valor),
                color = cor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(5.dp))

        Text(
            text = dataCaixa(movimentacao),
            color = Cinza,
            fontSize = 10.sp
        )
    }
}

// NOVO FORMULARIO DE MOVIMENTACAO
@Composable
private fun CadastroMovimentacaoDialog(
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

    var erro by remember {
        mutableStateOf("")
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Branco,
        unfocusedContainerColor = Branco,
        focusedBorderColor = Laranja,
        unfocusedBorderColor = Borda,
        focusedTextColor = Azul,
        unfocusedTextColor = Azul
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
            shape = RoundedCornerShape(22.dp),
            color = Fundo,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // TITULO
                Text(
                    text = "Nova movimentação",
                    color = Azul,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "Registre uma entrada ou saída.",
                    color = Cinza,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(19.dp))

                // CONTEUDO ROLAVEL
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    // SELETOR DE ENTRADA OU SAIDA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(9.dp)
                    ) {
                        TipoCaixaBotao(
                            titulo = "Entrada",
                            selecionado =
                                tipo == TipoMovimentacao.ENTRADA,
                            cor = Verde,
                            iconeEntrada = true,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                tipo = TipoMovimentacao.ENTRADA
                                erro = ""
                            }
                        )

                        TipoCaixaBotao(
                            titulo = "Saída",
                            selecionado =
                                tipo == TipoMovimentacao.SAIDA,
                            cor = Laranja,
                            iconeEntrada = false,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                tipo = TipoMovimentacao.SAIDA
                                erro = ""
                            }
                        )
                    }

                    // TITULO
                    Text(
                        text = "Título",
                        color = Azul,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = {
                            titulo = it
                            erro = ""
                        },
                        placeholder = {
                            Text(
                                text = if (
                                    tipo == TipoMovimentacao.ENTRADA
                                ) {
                                    "Ex.: Pagamento recebido"
                                } else {
                                    "Ex.: Compra de materiais"
                                },
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        enabled = !salvando,
                        shape = RoundedCornerShape(11.dp),
                        colors = coresCampo,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // DESCRICAO
                    Text(
                        text = "Descrição",
                        color = Azul,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = descricao,
                        onValueChange = {
                            descricao = it
                        },
                        placeholder = {
                            Text(
                                text = "Detalhes da movimentação",
                                fontSize = 12.sp
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        enabled = !salvando,
                        shape = RoundedCornerShape(11.dp),
                        colors = coresCampo,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // VALOR
                    Text(
                        text = "Valor (R$)",
                        color = Azul,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = valor,
                        onValueChange = {
                            valor = it
                            erro = ""
                        },
                        placeholder = {
                            Text(
                                "0,00",
                                fontSize = 12.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        enabled = !salvando,
                        shape = RoundedCornerShape(11.dp),
                        colors = coresCampo,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (erro.isNotBlank()) {
                        Text(
                            text = erro,
                            color = Color(0xFFB34D4D),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(
                    color = Borda
                )

                Spacer(Modifier.height(16.dp))

                // BOTOES INFERIORES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = !salvando,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(11.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Branco,
                                contentColor = Azul
                            ),
                        border = BorderStroke(1.dp, Borda)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val numero = valor
                                .replace("R$", "")
                                .trim()
                                .let { texto ->
                                    if (texto.contains(",")) {
                                        texto
                                            .replace(".", "")
                                            .replace(",", ".")
                                    } else {
                                        texto
                                    }
                                }
                                .toDoubleOrNull()

                            when {
                                titulo.isBlank() -> {
                                    erro = "Informe o título."
                                }

                                numero == null ||
                                        !numero.isFinite() ||
                                        numero <= 0.0 -> {
                                    erro =
                                        "Informe um valor válido."
                                }

                                else -> {
                                    onSalvar(
                                        titulo.trim(),
                                        descricao.trim(),
                                        numero,
                                        tipo
                                    )
                                }
                            }
                        },
                        enabled = !salvando,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(11.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Azul,
                            contentColor = Branco
                        )
                    ) {
                        if (salvando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(19.dp),
                                color = Branco,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Salvar",
                                fontSize = 13.sp,
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// BOTOES PARA SELECIONAR O TIPO
@Composable
private fun TipoCaixaBotao(
    titulo: String,
    selecionado: Boolean,
    cor: Color,
    iconeEntrada: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(47.dp),
        shape = RoundedCornerShape(11.dp),
        color = if (selecionado) {
            cor
        } else {
            Branco
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selecionado) {
                cor
            } else {
                Borda
            }
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (iconeEntrada) {
                    Icons.Outlined.ArrowDownward
                } else {
                    Icons.Outlined.ArrowUpward
                },
                contentDescription = null,
                tint = if (selecionado) {
                    Branco
                } else {
                    cor
                },
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selecionado) {
                    Branco
                } else {
                    Azul
                }
            )
        }
    }
}
