
package com.nexo.marcenaria365.ui.screens.caixa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val Azul = Color(0xFF193447)
private val Fundo = Color(0xFFFAF6F0)
private val Verde = Color(0xFF239B7A)
private val Laranja = Color(0xFFC08B60)
private val Cinza = Color(0xFF929292)
private val Borda = Color(0xFFE9E3DD)

@Composable
fun CaixaScreen(
    onVoltar: () -> Unit
) {
    val movimentacoes = remember {
        mutableStateListOf<MovimentacaoUi>().apply {
            addAll(movimentacoesIniciais())
        }
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

    var mostrarCadastro by remember {
        mutableStateOf(false)
    }

    // "Todas", "Entradas" ou "Saídas"
    var visualizacao by rememberSaveable {
        mutableStateOf("Todas")
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
        .filter { it.tipo == TipoMovimentacao.ENTRADA }
        .sumOf { it.valor }

    val saidas = filtradas
        .filter { it.tipo == TipoMovimentacao.SAIDA }
        .sumOf { it.valor }

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
        visualizacao != "Todas" -> movimentacoesSelecionadas
        mostrarTodas -> movimentacoesSelecionadas
        else -> movimentacoesSelecionadas.take(4)
    }

    fun voltarTela() {
        if (visualizacao != "Todas" || mostrarTodas) {
            visualizacao = "Todas"
            mostrarTodas = false
        } else {
            onVoltar()
        }
    }

    BackHandler {
        voltarTela()
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
                contentColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Registrar movimentação"
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
                IconButton(onClick = { voltarTela() }) {
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
                    modifier = Modifier.weight(1f),
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
                top = 22.dp,
                bottom = 95.dp
            ),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {

            // RESUMO FINANCEIRO: VISÍVEL NA TELA PRINCIPAL
            if (visualizacao == "Todas" && !mostrarTodas) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumo financeiro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Azul
                        )

                        SeletorPeriodoCaixa(
                            titulo = periodos[periodo].third,
                            aberto = menuPeriodoAberto,
                            periodos = periodos.map { it.third },
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
                            shape = RoundedCornerShape(11.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEFEFED)
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
                                    imageVector = Icons.Outlined.Wallet,
                                    contentDescription = null,
                                    tint = Azul,
                                    modifier = Modifier.size(25.dp)
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Saldo",
                                    fontSize = 11.sp,
                                    color = Azul
                                )

                                Spacer(Modifier.height(9.dp))

                                Text(
                                    text = moedaCaixa(saldo),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Azul,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF8E8D7),
                                RoundedCornerShape(11.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Laranja,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = "Toque em Entradas ou Saídas " +
                                    "para consultar as movimentações " +
                                    "separadamente.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF988A80)
                        )
                    }
                }
            }

            // CABEÇALHO DAS MOVIMENTAÇÕES
            item {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
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
                                    color = Color(0xFF428DBD),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // MOSTRA TOTAL QUANDO ESTIVER EM UMA CATEGORIA
                    if (visualizacao != "Todas") {
                        Text(
                            text = if (visualizacao == "Entradas") {
                                "Total recebido: ${moedaCaixa(entradas)}"
                            } else {
                                "Total gasto: ${moedaCaixa(saidas)}"
                            },
                            color = if (visualizacao == "Entradas") {
                                Verde
                            } else {
                                Laranja
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    // NAS LISTAS FILTRADAS, O PERÍODO CONTINUA EDITÁVEL
                    if (visualizacao != "Todas" || mostrarTodas) {
                        SeletorPeriodoCaixa(
                            titulo = periodos[periodo].third,
                            aberto = menuPeriodoAberto,
                            periodos = periodos.map { it.third },
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

            // MOVIMENTAÇÕES FILTRADAS
            if (exibidas.isEmpty()) {
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

    // CADASTRAR ENTRADA OU SAÍDA
    if (mostrarCadastro) {
        CadastroMovimentacaoDialog(
            onCancelar = {
                mostrarCadastro = false
            },
            onSalvar = { titulo, descricao, valor, tipo ->
                val hoje = Calendar.getInstance()

                movimentacoes.add(
                    MovimentacaoUi(
                        id = (movimentacoes.maxOfOrNull {
                            it.id
                        } ?: 0L) + 1L,
                        titulo = titulo,
                        descricao = descricao,
                        valor = valor,
                        tipo = tipo,
                        dia = hoje.get(Calendar.DAY_OF_MONTH),
                        mes = hoje.get(Calendar.MONTH) + 1,
                        ano = hoje.get(Calendar.YEAR)
                    )
                )

                periodo = 0
                mostrarCadastro = false

                // MOSTRA A CATEGORIA DO REGISTRO ADICIONADO
                visualizacao = if (
                    tipo == TipoMovimentacao.ENTRADA
                ) {
                    "Entradas"
                } else {
                    "Saídas"
                }
            }
        )
    }
}

@Composable
private fun SeletorPeriodoCaixa(
    titulo: String,
    aberto: Boolean,
    periodos: List<String>,
    onAbrir: () -> Unit,
    onFechar: () -> Unit,
    onSelecionar: (Int) -> Unit
) {
    Box {
        TextButton(
            onClick = onAbrir
        ) {
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = Cinza
            )

            Spacer(Modifier.width(3.dp))

            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "Selecionar período",
                tint = Cinza,
                modifier = Modifier.size(15.dp)
            )
        }

        DropdownMenu(
            expanded = aberto,
            onDismissRequest = onFechar
        ) {
            periodos.forEachIndexed { indice, nome ->
                DropdownMenuItem(
                    text = {
                        Text(nome)
                    },
                    onClick = {
                        onSelecionar(indice)
                    }
                )
            }
        }
    }
}

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
        modifier = modifier
            .height(142.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(
            containerColor = fundo
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (entrada) {
                    Icons.Outlined.ArrowDownward
                } else {
                    Icons.Outlined.ArrowUpward
                },
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(25.dp)
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = titulo,
                color = cor,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = moedaCaixa(valor),
                color = cor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(5.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Ver $titulo",
                tint = cor,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun LinhaMovimentacao(
    movimentacao: MovimentacaoUi
) {
    val entrada =
        movimentacao.tipo == TipoMovimentacao.ENTRADA

    val cor = if (entrada) {
        Verde
    } else {
        Color(0xFFF0A72C)
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
                tint = Color.White,
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
                fontSize = 10.sp,
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
            fontSize = 9.sp
        )
    }
}

@Composable
private fun CadastroMovimentacaoDialog(
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

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = "Registrar movimentação",
                color = Azul,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        TipoMovimentacao.ENTRADA,
                        TipoMovimentacao.SAIDA
                    ).forEachIndexed { indice, opcao ->

                        SegmentedButton(
                            selected = tipo == opcao,
                            onClick = {
                                tipo = opcao
                            },
                            shape =
                                SegmentedButtonDefaults.itemShape(
                                    indice,
                                    2
                                ),
                            label = {
                                Text(
                                    if (
                                        opcao ==
                                        TipoMovimentacao.ENTRADA
                                    ) {
                                        "Entrada"
                                    } else {
                                        "Saída"
                                    }
                                )
                            }
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
                        Text("Título")
                    },
                    placeholder = {
                        Text(
                            if (tipo == TipoMovimentacao.ENTRADA) {
                                "Pagamento recebido"
                            } else {
                                "Compra de materiais"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descricao,
                    onValueChange = {
                        descricao = it
                    },
                    label = {
                        Text("Descrição / Serviço")
                    },
                    placeholder = {
                        Text(
                            "Ex.: Ana Silva · Cozinha planejada"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valor,
                    onValueChange = {
                        valor = it
                        erro = ""
                    },
                    label = {
                        Text("Valor (R$)")
                    },
                    placeholder = {
                        Text("Ex.: 1500,00")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
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
        },
        confirmButton = {
            TextButton(
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
                            erro = "Informe um valor válido."
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
                }
            ) {
                Text(
                    text = "Salvar",
                    color = Azul,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(
                    text = "Cancelar",
                    color = Cinza
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
