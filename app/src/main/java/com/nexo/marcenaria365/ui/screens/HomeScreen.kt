
package com.nexo.marcenaria365.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nexo.marcenaria365.data.repository.AuthRepository
import com.nexo.marcenaria365.data.repository.ClienteRepository
import com.nexo.marcenaria365.data.repository.ServicoRepository
import com.nexo.marcenaria365.data.repository.RecebimentoRepository
import com.nexo.marcenaria365.data.repository.MovimentacaoCaixaRepository
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import com.nexo.marcenaria365.ui.screens.servicos.rotuloSituacaoServico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

private val VerdeHome = Color(0xFF239B7A)
private val LaranjaHome = Color(0xFFF6A823)
private val VermelhoHome = Color(0xFFE47779)
private val CinzaHome = Color(0xFFC8C8C6)

// Cores das etiquetas de status.
// Correspondem às cores utilizadas no gráfico.

private val FundoAguardandoHome = Color(0xFFFFF0D6)
private val FundoProducaoHome = Color(0xFFECECEB)
private val FundoConcluidoHome = Color(0xFFDDF3E7)
private val FundoCanceladoHome = Color(0xFFFBE6E7)

private val TextoAguardandoHome = Color(0xFF895500)
private val TextoProducaoHome = Color(0xFF48545C)
private val TextoConcluidoHome = Color(0xFF176B54)
private val TextoCanceladoHome = Color(0xFF9E3339)

data class StatusServico(
    val nome: String,
    val quantidade: Int,
    val cor: Color
)

data class EntregaResumo(
    val titulo: String,
    val cliente: String,
    val prazo: String,
    val status: String
)

data class ResumoDashboard(
    val saldo: String,
    val clientes: Int,
    val emAndamento: Int,
    val concluidos: Int,
    val status: List<StatusServico>,
    val entregas: List<EntregaResumo>
)

private fun formatarMoeda(valor: Double): String =
    NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR")
    ).format(valor)

// CLASSIFICAÇÃO DOS STATUS
// Preserva a compatibilidade com os valores
// que já existem no backend e no aplicativo.

private fun classificarServico(status: String): String {
    val texto = status.trim().lowercase(Locale.ROOT)

    return when {
        texto.contains("conclu") ||
                texto.contains("finaliz") ||
                texto.contains("entregue") -> "concluidos"

        texto.contains("produ") ||
                texto.contains("andamento") ||
                texto.contains("execu") -> "producao"

        texto.contains("cancel") -> "cancelados"

        texto.contains("aguard") ||
                texto.contains("orç") ||
                texto.contains("orc") -> "aguardando"

        else -> "pendentes"
    }
}

// RÓTULO VISUAL DAS PRÓXIMAS ENTREGAS
// Altera somente o texto mostrado ao usuário.
// Não modifica os status salvos.

private fun rotuloEntregaHome(status: String): String {
    return when (classificarServico(status)) {
        "aguardando" -> "Aguardando"
        "producao" -> "Produção"
        "concluidos" -> "Concluído"
        "cancelados" -> "Cancelado"
        else -> rotuloSituacaoServico(status)
    }
}

// FUNDO COLORIDO DA ETIQUETA

private fun fundoEntregaHome(
    status: String,
    corPadrao: Color
): Color {
    return when (classificarServico(status)) {
        "aguardando" -> FundoAguardandoHome
        "producao" -> FundoProducaoHome
        "concluidos" -> FundoConcluidoHome
        "cancelados" -> FundoCanceladoHome
        else -> corPadrao
    }
}

// COR DO TEXTO DA ETIQUETA

private fun corEntregaHome(
    status: String,
    corPadrao: Color
): Color {
    return when (classificarServico(status)) {
        "aguardando" -> TextoAguardandoHome
        "producao" -> TextoProducaoHome
        "concluidos" -> TextoConcluidoHome
        "cancelados" -> TextoCanceladoHome
        else -> corPadrao
    }
}

private fun ordenarData(prazo: String): Long =
    try {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("pt-BR")
        ).apply {
            isLenient = false
        }.parse(prazo.trim())?.time ?: Long.MAX_VALUE
    } catch (_: Exception) {
        Long.MAX_VALUE
    }

/*
 * REGRA FINANCEIRA PRESERVADA
 *
 * A tela Início utiliza a mesma origem de dados
 * da tela Caixa:
 *
 * 1. Recebimentos com status RECEBIDO.
 * 2. Movimentações avulsas de entrada.
 * 3. Movimentações avulsas de saída.
 * 4. Movimentações vinculadas a recebimentos
 *    não são somadas novamente.
 *
 * O saldo total considera todas as datas
 * registradas, sem o filtro mensal do Caixa.
 *
 * Nenhuma movimentação é criada, editada
 * ou excluída por esta função.
 */

private fun calcularSaldoTotal(
    totalRecebimentos: Double,
    entradasAvulsas: Double,
    saidasAvulsas: Double
): Double {
    val totalEntradas =
        totalRecebimentos + entradasAvulsas

    return totalEntradas - saidasAvulsas
}

/*
 * Os indicadores operacionais continuam
 * independentes do saldo financeiro.
 *
 * Serviços cancelados permanecem no gráfico.
 * As próximas entregas ignoram serviços
 * concluídos e cancelados.
 */

private fun montarResumoDashboard(
    quantidadeClientes: Int,
    servicos: List<ServicoUi>,
    saldoTotal: Double
): ResumoDashboard {

    val aguardando = servicos.count {
        classificarServico(it.status) == "aguardando"
    }

    val producao = servicos.count {
        classificarServico(it.status) == "producao"
    }

    val concluidos = servicos.count {
        classificarServico(it.status) == "concluidos"
    }

    val cancelados = servicos.count {
        classificarServico(it.status) == "cancelados"
    }

    val outros = servicos.count {
        classificarServico(it.status) == "pendentes"
    }

    val entregas = servicos
        .filter {
            val situacao = classificarServico(it.status)

            situacao != "concluidos" &&
                    situacao != "cancelados" &&
                    it.entrega.isNotBlank()
        }
        .sortedBy {
            ordenarData(it.entrega)
        }
        .take(3)
        .map {
            EntregaResumo(
                titulo = it.titulo,
                cliente = it.clienteNome,
                prazo = it.entrega,
                status = it.status
            )
        }

    val status = mutableListOf(
        StatusServico(
            nome = "Aguardando",
            quantidade = aguardando,
            cor = LaranjaHome
        ),
        StatusServico(
            nome = "Produção",
            quantidade = producao,
            cor = CinzaHome
        ),
        StatusServico(
            nome = "Concluídos",
            quantidade = concluidos,
            cor = VerdeHome
        ),
        StatusServico(
            nome = "Cancelados",
            quantidade = cancelados,
            cor = VermelhoHome
        )
    )

    if (outros > 0) {
        status.add(
            StatusServico(
                nome = "Outros status",
                quantidade = outros,
                cor = Color(0xFF9174AE)
            )
        )
    }

    return ResumoDashboard(
        saldo = formatarMoeda(saldoTotal),
        clientes = quantidadeClientes,
        emAndamento = producao,
        concluidos = concluidos,
        status = status,
        entregas = entregas
    )
}

@Composable
fun HomeScreen(
    nomeUsuario: String,
    onSair: () -> Unit,
    onClientesClick: () -> Unit,
    onServicosClick: () -> Unit,
    onCaixaClick: () -> Unit,
    onMaisClick: () -> Unit,
    quantidadeClientes: Int = 0
) {
    val cores = MaterialTheme.colorScheme
    val destaque = cores.secondary
    val texto = cores.onSurface
    val secundario = cores.onSurfaceVariant

    val primeiroNome = nomeUsuario
        .trim()
        .substringBefore(" ")
        .ifBlank { "Usuário" }

    var clientes by remember {
        mutableIntStateOf(0)
    }

    var servicos by remember {
        mutableStateOf<List<ServicoUi>>(emptyList())
    }

    var saldoTotal by remember {
        mutableDoubleStateOf(0.0)
    }

    var carregando by remember {
        mutableStateOf(true)
    }

    var mensagemErro by remember {
        mutableStateOf<String?>(null)
    }

    var revisaoDados by remember {
        mutableIntStateOf(0)
    }

    val tokenAtual = AuthRepository.sessaoAtual?.token
    val lifecycleOwner = LocalLifecycleOwner.current

    // Atualiza os indicadores ao voltar para o aplicativo.

    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                revisaoDados++
            }
        }

        lifecycleOwner.lifecycle.addObserver(observador)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador)
        }
    }

    /*
     * CARREGAMENTO DOS DADOS REAIS
     *
     * A Home e o Caixa consultam os mesmos
     * repositórios financeiros.
     *
     * O cálculo financeiro é independente
     * do status operacional dos serviços.
     */

    LaunchedEffect(tokenAtual, revisaoDados) {
        carregando = true
        mensagemErro = null

        clientes = 0
        servicos = emptyList()
        saldoTotal = 0.0

        if (tokenAtual == null) {
            mensagemErro =
                "Faça login para acessar seus dados."

            carregando = false
            return@LaunchedEffect
        }

        val resultado = withContext(Dispatchers.IO) {
            runCatching {
                val clientesApi =
                    ClienteRepository.listar().getOrThrow()

                val servicosApi =
                    ServicoRepository.listar(clientesApi)
                        .getOrThrow()

                val recebimentosApi =
                    RecebimentoRepository.listar().getOrThrow()

                val movimentacoesApi =
                    MovimentacaoCaixaRepository.listar()
                        .getOrThrow()

                /*
                 * Recebimentos efetivamente pagos.
                 *
                 * A data precisa ser válida, assim
                 * como já ocorre na tela Caixa.
                 */

                val recebimentosValidos =
                    recebimentosApi.filter { recebimento ->
                        recebimento.status.equals(
                            "RECEBIDO",
                            ignoreCase = true
                        ) &&
                                recebimento.dataRecebimento?.let {
                                    runCatching {
                                        LocalDate.parse(it)
                                    }.isSuccess
                                } == true
                    }

                val totalRecebimentos =
                    recebimentosValidos.sumOf {
                        it.valor
                    }

                /*
                 * Movimentações avulsas.
                 *
                 * Se o lançamento estiver vinculado
                 * a um recebimento, o valor não deve
                 * ser contado duas vezes.
                 */

                val movimentacoesAvulsas =
                    movimentacoesApi.filter { movimento ->
                        movimento.recebimentoId == null &&
                                runCatching {
                                    LocalDate.parse(
                                        movimento.dataMovimentacao
                                    )
                                }.isSuccess
                    }

                val entradasAvulsas =
                    movimentacoesAvulsas
                        .filter {
                            it.tipo.equals(
                                "ENTRADA",
                                ignoreCase = true
                            )
                        }
                        .sumOf {
                            it.valor
                        }

                val saidasAvulsas =
                    movimentacoesAvulsas
                        .filter {
                            it.tipo.equals(
                                "SAIDA",
                                ignoreCase = true
                            )
                        }
                        .sumOf {
                            it.valor
                        }

                val saldoCalculado =
                    calcularSaldoTotal(
                        totalRecebimentos = totalRecebimentos,
                        entradasAvulsas = entradasAvulsas,
                        saidasAvulsas = saidasAvulsas
                    )

                /*
                 * Preserva a associação dos
                 * recebimentos aos serviços.
                 */

                val recebidosPorServico =
                    recebimentosApi
                        .filter {
                            it.status.equals(
                                "RECEBIDO",
                                ignoreCase = true
                            )
                        }
                        .groupBy {
                            it.servicoId
                        }
                        .mapValues { (_, pagamentos) ->
                            RecebimentoRepository.totalRecebido(
                                pagamentos
                            )
                        }

                val servicosAtualizados =
                    servicosApi.map { servico ->
                        servico.copy(
                            valorRecebido =
                                recebidosPorServico[servico.id]
                                    ?: 0.0
                        )
                    }

                Triple(
                    clientesApi.size,
                    servicosAtualizados,
                    saldoCalculado
                )
            }
        }

        // Não utiliza dados de outra sessão.

        if (AuthRepository.sessaoAtual?.token != tokenAtual) {
            return@LaunchedEffect
        }

        resultado.fold(
            onSuccess = { dados ->
                clientes = dados.first
                servicos = dados.second
                saldoTotal = dados.third
                mensagemErro = null
            },
            onFailure = { erro ->
                mensagemErro = erro.message
                    ?: "Não foi possível atualizar o resumo."
            }
        )

        carregando = false
    }

    val resumo = montarResumoDashboard(
        quantidadeClientes = clientes,
        servicos = servicos,
        saldoTotal = saldoTotal
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cores.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // SAUDAÇÃO

            Text(
                text = "Olá, $primeiroNome!",
                color = cores.onBackground,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Confira o resumo da sua marcenaria hoje.",
                color = secundario,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(Modifier.height(8.dp))

            // ATUALIZAÇÃO MANUAL

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        revisaoDados++
                    },
                    enabled = !carregando
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Atualizar resumo")
                }
            }

            Spacer(Modifier.height(5.dp))

            // CARREGAMENTO

            if (carregando) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = destaque,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Carregando seus dados...",
                        color = secundario
                    )
                }
            }

            // MENSAGEM DE ERRO

            if (mensagemErro != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion = LiveRegionMode.Assertive
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = cores.surface
                    )
                ) {
                    Text(
                        text = mensagemErro.orEmpty(),
                        color = cores.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        revisaoDados++
                    }
                ) {
                    Text("Tentar novamente")
                }
            }

            if (!carregando && mensagemErro == null) {

                // CARTÃO FINANCEIRO
                // Saldo total: entradas - saídas.

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cores.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 16.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = destaque,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(Modifier.width(9.dp))

                            Text(
                                text = "SALDO TOTAL",
                                color = secundario,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = resumo.saldo,
                            color = texto,
                            fontSize = 29.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics {
                                contentDescription =
                                    "Saldo total acumulado: ${resumo.saldo}"
                            }
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Entradas menos saídas registradas",
                            color = secundario,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .clickable(
                                    role = Role.Button,
                                    onClick = onCaixaClick
                                )
                                .heightIn(min = 48.dp)
                                .padding(
                                    horizontal = 4.dp,
                                    vertical = 9.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ver detalhes",
                                color = destaque,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                imageVector =
                                    Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = destaque,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(11.dp))

                // INDICADORES OPERACIONAIS

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {
                    IndicadorDashboard(
                        modifier = Modifier.weight(1f),
                        icone = Icons.Outlined.PersonOutline,
                        numero = resumo.clientes,
                        descricao = "Clientes\ncadastrados",
                        cor = destaque,
                        onClick = onClientesClick
                    )

                    IndicadorDashboard(
                        modifier = Modifier.weight(1f),
                        icone =
                            Icons.AutoMirrored.Outlined.Assignment,
                        numero = resumo.emAndamento,
                        descricao = "Serviços em\nandamento",
                        cor = destaque,
                        onClick = onServicosClick
                    )

                    IndicadorDashboard(
                        modifier = Modifier.weight(1f),
                        icone =
                            Icons.Outlined.CheckCircleOutline,
                        numero = resumo.concluidos,
                        descricao = "Serviços\nconcluídos",
                        cor = VerdeHome,
                        onClick = onServicosClick
                    )
                }

                Spacer(Modifier.height(11.dp))

                // GRÁFICO DE SERVIÇOS

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cores.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Serviços por status",
                            color = texto,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                heading()
                            }
                        )

                        Spacer(Modifier.height(13.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            GraficoDashboard(
                                status = resumo.status,
                                modifier = Modifier.size(132.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(13.dp)
                            ) {
                                resumo.status.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics(
                                                mergeDescendants = true
                                            ) {
                                                contentDescription =
                                                    "${item.nome}: " +
                                                            "${item.quantidade} serviços"
                                            },
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(item.cor)
                                        )

                                        Spacer(Modifier.width(6.dp))

                                        Text(
                                            text = item.nome,
                                            color = texto,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow =
                                                TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = item.quantidade.toString(),
                                            color = texto,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(11.dp))

                // PRÓXIMAS ENTREGAS
                // Etiquetas coloridas com o mesmo padrão
                // visual do gráfico e da lista de serviços.

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cores.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp)
                    ) {
                        Text(
                            text = "Próximas entregas",
                            color = texto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.semantics {
                                heading()
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        if (resumo.entregas.isEmpty()) {
                            Text(
                                text = "Nenhuma entrega prevista.",
                                color = secundario,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(
                                    vertical = 12.dp
                                )
                            )
                        } else {
                            resumo.entregas.forEach { entrega ->

                                val situacaoVisivel =
                                    rotuloEntregaHome(entrega.status)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics(
                                            mergeDescendants = true
                                        ) {
                                            contentDescription =
                                                "Serviço: ${entrega.titulo}. " +
                                                        "Cliente: ${entrega.cliente}. " +
                                                        "Prazo: ${entrega.prazo}. " +
                                                        "Situação: $situacaoVisivel"
                                        }
                                        .padding(bottom = 12.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(
                                                RoundedCornerShape(8.dp)
                                            )
                                            .background(
                                                cores.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.AutoMirrored.Outlined.Assignment,
                                            contentDescription = null,
                                            tint = cores.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(9.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = entrega.titulo,
                                            color = texto,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow =
                                                TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text =
                                                "${entrega.cliente} · ${entrega.prazo}",
                                            color = secundario,
                                            fontSize = 12.sp,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(Modifier.width(6.dp))

                                    // ETIQUETA COLORIDA
                                    // Exibe "Produção" em vez de
                                    // "Em produção", sem modificar
                                    // o valor interno do serviço.

                                    Surface(
                                        color = fundoEntregaHome(
                                            entrega.status,
                                            cores.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = situacaoVisivel,
                                            color = corEntregaHome(
                                                entrega.status,
                                                cores.onSurfaceVariant
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight =
                                                FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow =
                                                TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 7.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        // NAVEGAÇÃO INFERIOR ORIGINAL

        NavigationBar(
            containerColor = cores.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.heightIn(min = 75.dp)
        ) {
            NavigationBarItem(
                selected = true,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = null
                    )
                },
                label = {
                    Text("Início", fontSize = 12.sp)
                },
                colors = coresDashboard()
            )

            NavigationBarItem(
                selected = false,
                onClick = onClientesClick,
                icon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.PeopleOutline,
                        contentDescription = null
                    )
                },
                label = {
                    Text("Clientes", fontSize = 12.sp)
                },
                colors = coresDashboard()
            )

            NavigationBarItem(
                selected = false,
                onClick = onServicosClick,
                icon = {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null
                    )
                },
                label = {
                    Text("Serviços", fontSize = 12.sp)
                },
                colors = coresDashboard()
            )

            NavigationBarItem(
                selected = false,
                onClick = onMaisClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = null
                    )
                },
                label = {
                    Text("Mais", fontSize = 12.sp)
                },
                colors = coresDashboard()
            )
        }
    }
}

@Composable
private fun coresDashboard(): NavigationBarItemColors {
    val cores = MaterialTheme.colorScheme

    return NavigationBarItemDefaults.colors(
        selectedIconColor = cores.primary,
        selectedTextColor = cores.onSurface,
        indicatorColor = cores.primaryContainer,
        unselectedIconColor = cores.onSurfaceVariant,
        unselectedTextColor = cores.onSurfaceVariant
    )
}

@Composable
private fun IndicadorDashboard(
    modifier: Modifier,
    icone: ImageVector,
    numero: Int,
    descricao: String,
    cor: Color,
    onClick: () -> Unit
) {
    val cores = MaterialTheme.colorScheme
    val nome = descricao.replace("\n", " ")

    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 112.dp)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "$nome: $numero. Abrir detalhes."
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cores.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(21.dp)
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = numero.toString(),
                color = cores.onSurface,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = nome,
                color = cores.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun GraficoDashboard(
    status: List<StatusServico>,
    modifier: Modifier = Modifier
) {
    val cores = MaterialTheme.colorScheme
    val total = status.sumOf { it.quantidade }

    val descricao = buildString {
        append("Gráfico de serviços por status. ")
        append("Total: $total serviços. ")

        status.forEach { item ->
            append("${item.nome}: ${item.quantidade}. ")
        }
    }

    Box(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = descricao
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val espessura = 19.dp.toPx()
            val margem = espessura / 2f
            val diametro = size.minDimension - espessura

            var angulo = -90f

            if (total > 0) {
                status.forEach { item ->
                    val parte =
                        360f * item.quantidade / total

                    drawArc(
                        color = item.cor,
                        startAngle = angulo,
                        sweepAngle = parte,
                        useCenter = false,
                        topLeft = Offset(margem, margem),
                        size = Size(diametro, diametro),
                        style = Stroke(width = espessura)
                    )

                    angulo += parte
                }
            } else {
                drawArc(
                    color = cores.outline,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(margem, margem),
                    size = Size(diametro, diametro),
                    style = Stroke(width = espessura)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = total.toString(),
                color = cores.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp
            )

            Text(
                text = "Total",
                color = cores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
