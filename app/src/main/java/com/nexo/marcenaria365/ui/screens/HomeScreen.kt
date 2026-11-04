
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.data.repository.AuthRepository
import com.nexo.marcenaria365.data.repository.ClienteRepository
import com.nexo.marcenaria365.data.repository.ServicoRepository
import com.nexo.marcenaria365.data.repository.RecebimentoRepository
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val FundoHome = Color(0xFFFAF6F0)
private val AzulHome = Color(0xFF193447)
private val CobreHome = Color(0xFFBD906E)
private val CinzaHome = Color(0xFF929292)
private val VerdeHome = Color(0xFF239B7A)
private val LaranjaHome = Color(0xFFF6A823)
private val VermelhoHome = Color(0xFFE47779)
private val CinzaGraficoHome = Color(0xFFC8C8C6)

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

private fun formatarMoeda(valor: Double): String {
    return NumberFormat
        .getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        .format(valor)
}

private fun classificarServico(status: String): String {
    val texto = status.trim().lowercase(Locale.ROOT)

    return when {
        texto.contains("conclu") ||
                texto.contains("finaliz") ||
                texto.contains("entregue") -> "concluidos"

        texto.contains("produ") ||
                texto.contains("andamento") ||
                texto.contains("execu") -> "producao"

        texto.contains("orç") ||
                texto.contains("orc") -> "orcamentos"

        else -> "pendentes"
    }
}

private fun ordenarData(prazo: String): Long {
    return try {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("pt-BR")
        )
        formato.isLenient = false
        formato.parse(prazo.trim())?.time ?: Long.MAX_VALUE
    } catch (_: Exception) {
        Long.MAX_VALUE
    }
}

private fun montarResumoDashboard(
    quantidadeClientes: Int,
    servicos: List<ServicoUi>
): ResumoDashboard {

    val emAndamento = servicos.count {
        classificarServico(it.status) == "producao"
    }

    val concluidos = servicos.count {
        classificarServico(it.status) == "concluidos"
    }

    val orcamentos = servicos.count {
        classificarServico(it.status) == "orcamentos"
    }

    val pendentes = servicos.count {
        classificarServico(it.status) == "pendentes"
    }

    val totalRecebido = servicos.sumOf {
        it.valorRecebido
    }

    val proximasEntregas = servicos
        .filter {
            classificarServico(it.status) != "concluidos" &&
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

    return ResumoDashboard(
        saldo = formatarMoeda(totalRecebido),
        clientes = quantidadeClientes,
        emAndamento = emAndamento,
        concluidos = concluidos,
        status = listOf(
            StatusServico(
                nome = "Orçamentos",
                quantidade = orcamentos,
                cor = LaranjaHome
            ),
            StatusServico(
                nome = "Produção",
                quantidade = emAndamento,
                cor = CinzaGraficoHome
            ),
            StatusServico(
                nome = "Concluídos",
                quantidade = concluidos,
                cor = VerdeHome
            ),
            StatusServico(
                nome = "Pendentes",
                quantidade = pendentes,
                cor = VermelhoHome
            )
        ),
        entregas = proximasEntregas
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
    val primeiroNome = nomeUsuario
        .trim()
        .substringBefore(" ")
        .ifBlank { "Usuário" }

    var totalClientes by remember { mutableIntStateOf(0) }

    var listaServicos by remember {
        mutableStateOf<List<ServicoUi>>(emptyList())
    }

    var carregando by remember {
        mutableStateOf(true)
    }

    var mensagemErro by remember {
        mutableStateOf<String?>(null)
    }

    val tokenAtual = AuthRepository.sessaoAtual?.token

    LaunchedEffect(tokenAtual) {
        carregando = true
        mensagemErro = null
        totalClientes = 0
        listaServicos = emptyList()

        if (tokenAtual == null) {
            carregando = false
            mensagemErro = "Faça login para acessar seus dados."
            return@LaunchedEffect
        }

        val resultado = withContext(Dispatchers.IO) {
            runCatching {
                val clientesApi =
                    ClienteRepository.listar().getOrThrow()

                val servicosApi =
                    ServicoRepository.listar(clientesApi).getOrThrow()

                val recebimentosApi =
                    RecebimentoRepository.listar().getOrThrow()

                val totaisRecebidos = recebimentosApi
                    .filter {
                        it.status.equals(
                            "RECEBIDO",
                            ignoreCase = true
                        )
                    }
                    .groupBy { it.servicoId }
                    .mapValues { (_, pagamentos) ->
                        RecebimentoRepository.totalRecebido(pagamentos)
                    }

                val servicosAtualizados = servicosApi.map { servico ->
                    servico.copy(
                        valorRecebido =
                            totaisRecebidos[servico.id] ?: 0.0
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
                totalClientes = dados.first
                listaServicos = dados.second
                mensagemErro = null
            },
            onFailure = { erro ->
                mensagemErro =
                    erro.message ?: "Não foi possível carregar os dados."
            }
        )

        carregando = false
    }

    val resumo = montarResumoDashboard(
        quantidadeClientes = totalClientes,
        servicos = listaServicos
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoHome)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(15.dp))

            Text(
                text = "Olá, $primeiroNome!",
                color = AzulHome,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Confira o resumo da sua marcenaria hoje.",
                color = CinzaHome,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(13.dp))

            if (carregando) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CobreHome,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Carregando seus dados...",
                        color = CinzaHome,
                        fontSize = 12.sp
                    )
                }
            }

            if (mensagemErro != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = mensagemErro ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = VermelhoHome,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            if (!carregando && mensagemErro == null) {

                // TOTAL RECEBIDO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
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
                                tint = CobreHome,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(Modifier.width(9.dp))

                            Text(
                                text = "TOTAL RECEBIDO",
                                color = CinzaHome,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = resumo.saldo,
                            color = AzulHome,
                            fontSize = 29.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .clickable(onClick = onCaixaClick)
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ver detalhes",
                                color = CobreHome,
                                fontSize = 12.sp
                            )

                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = CobreHome,
                                modifier = Modifier.size(17.dp)
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
                        cor = CobreHome,
                        onClick = onClientesClick
                    )

                    IndicadorDashboard(
                        modifier = Modifier.weight(1f),
                        icone = Icons.AutoMirrored.Outlined.Assignment,
                        numero = resumo.emAndamento,
                        descricao = "Serviços em\nandamento",
                        cor = CobreHome,
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

                // GRAFICO DE SERVICOS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Serviços por status",
                            color = AzulHome,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
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
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(item.cor)
                                        )

                                        Spacer(Modifier.width(6.dp))

                                        Text(
                                            text = item.nome,
                                            color = AzulHome,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = item.quantidade.toString(),
                                            color = AzulHome,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(11.dp))

                // PROXIMAS ENTREGAS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp)
                    ) {
                        Text(
                            text = "Próximas entregas",
                            color = AzulHome,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        if (resumo.entregas.isEmpty()) {
                            Text(
                                text = "Nenhuma entrega prevista.",
                                color = CinzaHome,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            resumo.entregas.forEach { entrega ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF7EBDC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.AutoMirrored.Outlined.Assignment,
                                            contentDescription = null,
                                            tint = CobreHome,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(9.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = entrega.titulo,
                                            color = AzulHome,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text =
                                                "${entrega.cliente} · ${entrega.prazo}",
                                            color = CinzaHome,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Text(
                                        text = entrega.status,
                                        color = LaranjaHome,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        // MENU INFERIOR
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(75.dp)
        ) {
            NavigationBarItem(
                selected = true,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Início"
                    )
                },
                label = {
                    Text("Início", fontSize = 10.sp)
                },
                colors = coresDashboard()
            )

            NavigationBarItem(
                selected = false,
                onClick = onClientesClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.PeopleOutline,
                        contentDescription = "Clientes"
                    )
                },
                label = {
                    Text("Clientes", fontSize = 10.sp)
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
                        contentDescription = "Serviços"
                    )
                },
                label = {
                    Text("Serviços", fontSize = 10.sp)
                },
                colors = coresDashboard()
            )

            NavigationBarItem(
                selected = false,
                onClick = onMaisClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Mais"
                    )
                },
                label = {
                    Text("Mais", fontSize = 10.sp)
                },
                colors = coresDashboard()
            )
        }
    }
}

@Composable
private fun coresDashboard() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = AzulHome,
        selectedTextColor = AzulHome,
        indicatorColor = Color(0xFFF1E8DE),
        unselectedIconColor = CinzaHome,
        unselectedTextColor = CinzaHome
    )

@Composable
private fun IndicadorDashboard(
    modifier: Modifier,
    icone: ImageVector,
    numero: Int,
    descricao: String,
    cor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                color = AzulHome,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = descricao,
                color = CinzaHome,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun GraficoDashboard(
    status: List<StatusServico>,
    modifier: Modifier = Modifier
) {
    val total = status.sumOf { it.quantidade }

    Box(
        modifier = modifier,
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
                    color = CinzaGraficoHome,
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
                color = AzulHome,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp
            )

            Text(
                text = "Total",
                color = CinzaHome,
                fontSize = 10.sp
            )
        }
    }
}
