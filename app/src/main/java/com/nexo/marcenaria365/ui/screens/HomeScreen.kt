
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
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import com.nexo.marcenaria365.ui.screens.servicos.rotuloSituacaoServico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val VerdeHome = Color(0xFF239B7A)
private val LaranjaHome = Color(0xFFF6A823)
private val VermelhoHome = Color(0xFFE47779)
private val CinzaHome = Color(0xFFC8C8C6)

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

private fun montarResumoDashboard(
    quantidadeClientes: Int,
    servicos: List<ServicoUi>
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

    val recebido = servicos.sumOf { it.valorRecebido }

    val entregas = servicos
        .filter {
            val situacao = classificarServico(it.status)
            situacao != "concluidos" &&
                    situacao != "cancelados" &&
                    it.entrega.isNotBlank()
        }
        .sortedBy { ordenarData(it.entrega) }
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
        StatusServico("Aguardando", aguardando, LaranjaHome),
        StatusServico("Produção", producao, CinzaHome),
        StatusServico("Concluídos", concluidos, VerdeHome),
        StatusServico("Cancelados", cancelados, VermelhoHome)
    )

    if (outros > 0) {
        status.add(
            StatusServico(
                "Outros status",
                outros,
                Color(0xFF9174AE)
            )
        )
    }

    return ResumoDashboard(
        saldo = formatarMoeda(recebido),
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

    var clientes by remember { mutableIntStateOf(0) }

    var servicos by remember {
        mutableStateOf<List<ServicoUi>>(emptyList())
    }

    var carregando by remember { mutableStateOf(true) }

    var mensagemErro by remember {
        mutableStateOf<String?>(null)
    }

    var revisaoDados by remember {
        mutableIntStateOf(0)
    }

    val tokenAtual = AuthRepository.sessaoAtual?.token
    val lifecycleOwner = LocalLifecycleOwner.current

    // Atualiza ao voltar para a Activity.
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

    // Busca dados reais, evitando indicadores antigos.
    LaunchedEffect(tokenAtual, revisaoDados) {
        carregando = true
        mensagemErro = null

        clientes = 0
        servicos = emptyList()

        if (tokenAtual == null) {
            mensagemErro = "Faça login para acessar seus dados."
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

                val recebidosPorServico = recebimentosApi
                    .filter {
                        it.status.equals(
                            "RECEBIDO",
                            ignoreCase = true
                        )
                    }
                    .groupBy { it.servicoId }
                    .mapValues { (_, pagamentos) ->
                        RecebimentoRepository.totalRecebido(
                            pagamentos
                        )
                    }

                val servicosAtualizados = servicosApi.map {
                        servico ->
                    servico.copy(
                        valorRecebido =
                            recebidosPorServico[servico.id] ?: 0.0
                    )
                }

                Pair(clientesApi.size, servicosAtualizados)
            }
        }

        if (AuthRepository.sessaoAtual?.token != tokenAtual) {
            return@LaunchedEffect
        }

        resultado.fold(
            onSuccess = { dados ->
                clientes = dados.first
                servicos = dados.second
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
        servicos = servicos
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { revisaoDados++ },
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
                        "Carregando seus dados...",
                        color = secundario
                    )
                }
            }

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

                TextButton(onClick = { revisaoDados++ }) {
                    Text("Tentar novamente")
                }
            }

            if (!carregando && mensagemErro == null) {

                // TOTAL RECEBIDO
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
                                Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = destaque,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(Modifier.width(9.dp))

                            Text(
                                "TOTAL RECEBIDO",
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
                                    "Total recebido: ${resumo.saldo}"
                            }
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
                                "Ver detalhes",
                                color = destaque,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = destaque,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(11.dp))

                // INDICADORES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
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
                        icone = Icons.AutoMirrored.Outlined.Assignment,
                        numero = resumo.emAndamento,
                        descricao = "Serviços em\nandamento",
                        cor = destaque,
                        onClick = onServicosClick
                    )

                    IndicadorDashboard(
                        modifier = Modifier.weight(1f),
                        icone = Icons.Outlined.CheckCircleOutline,
                        numero = resumo.concluidos,
                        descricao = "Serviços\nconcluídos",
                        cor = VerdeHome,
                        onClick = onServicosClick
                    )
                }

                Spacer(Modifier.height(11.dp))

                // GRÁFICO
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
                            "Serviços por status",
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
                                            overflow = TextOverflow.Ellipsis,
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
                            "Próximas entregas",
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
                                "Nenhuma entrega prevista.",
                                color = secundario,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(
                                    vertical = 12.dp
                                )
                            )
                        } else {
                            resumo.entregas.forEach { entrega ->
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
                                                        "Situação: " +
                                                        rotuloSituacaoServico(
                                                            entrega.status
                                                        )
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
                                            entrega.titulo,
                                            color = texto,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            "${entrega.cliente} · ${entrega.prazo}",
                                            color = secundario,
                                            fontSize = 12.sp,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(Modifier.width(5.dp))

                                    Text(
                                        rotuloSituacaoServico(entrega.status),
                                        color = destaque,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        modifier = Modifier.widthIn(
                                            max = 88.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        // NAVEGAÇÃO INFERIOR
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
                        Icons.Outlined.Home,
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
                        Icons.Outlined.Menu,
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
                numero.toString(),
                color = cores.onSurface,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                nome,
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

        status.forEach {
            append("${it.nome}: ${it.quantidade}. ")
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
                    val parte = 360f * item.quantidade / total

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
                total.toString(),
                color = cores.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp
            )

            Text(
                "Total",
                color = cores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
