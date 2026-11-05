
package com.nexo.marcenaria365.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoHome = Color(0xFFFAF6F0)
private val AzulHome = Color(0xFF193447)
private val CobreHome = Color(0xFF94613C)
private val CinzaHome = Color(0xFF53616A)
private val VerdeHome = Color(0xFF237A58)
private val LaranjaHome = Color(0xFF825B12)
private val VermelhoHome = Color(0xFFB3261E)
private val CinzaGraficoHome = Color(0xFF89969E)

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

// Dados demonstrativos preservados do projeto atual.
private val resumoInicial = ResumoDashboard(
    saldo = "R$ 4.850,00",
    clientes = 6,
    emAndamento = 8,
    concluidos = 4,
    status = listOf(
        StatusServico(
            nome = "Orçamentos",
            quantidade = 4,
            cor = LaranjaHome
        ),
        StatusServico(
            nome = "Produção",
            quantidade = 4,
            cor = CinzaGraficoHome
        ),
        StatusServico(
            nome = "Concluídos",
            quantidade = 7,
            cor = VerdeHome
        ),
        StatusServico(
            nome = "Pendentes",
            quantidade = 1,
            cor = VermelhoHome
        )
    ),
    entregas = listOf(
        EntregaResumo(
            titulo = "Cozinha planejada",
            cliente = "Ana Silva",
            prazo = "15/10",
            status = "Em produção"
        ),
        EntregaResumo(
            titulo = "Rack para sala",
            cliente = "Bruno Souza",
            prazo = "18/10",
            status = "Em produção"
        )
    )
)

@Composable
fun HomeScreen(
    nomeUsuario: String,
    onSair: () -> Unit,
    onClientesClick: () -> Unit,
    onServicosClick: () -> Unit,
    onCaixaClick: () -> Unit,
    onMaisClick: () -> Unit,
    quantidadeClientes: Int = resumoInicial.clientes
) {
    val primeiroNome = nomeUsuario
        .trim()
        .substringBefore(" ")
        .ifBlank { "Usuário" }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoHome,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                ItemMenuHome(
                    titulo = "Início",
                    icone = Icons.Outlined.Home,
                    selecionado = true,
                    onClick = {}
                )

                ItemMenuHome(
                    titulo = "Clientes",
                    icone = Icons.Outlined.PeopleOutline,
                    selecionado = false,
                    onClick = onClientesClick
                )

                ItemMenuHome(
                    titulo = "Serviços",
                    icone =
                        Icons.AutoMirrored.Outlined.Assignment,
                    selecionado = false,
                    onClick = onServicosClick
                )

                ItemMenuHome(
                    titulo = "Mais",
                    icone = Icons.Outlined.Menu,
                    selecionado = false,
                    onClick = onMaisClick
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            Text(
                text = "Olá, $primeiroNome!",
                color = AzulHome,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Confira o resumo da sua marcenaria.",
                color = CinzaHome,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(18.dp))

            // SALDO EM CAIXA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 17.dp
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
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(Modifier.width(9.dp))

                        Text(
                            text = "Saldo em caixa",
                            color = AzulHome,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = resumoInicial.saldo,
                        color = AzulHome,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            contentDescription =
                                "Saldo em caixa: ${resumoInicial.saldo}"
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = onCaixaClick,
                        modifier = Modifier.heightIn(
                            min = 48.dp
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Text(
                            text = "Ver detalhes do caixa",
                            color = CobreHome,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.width(5.dp))

                        Icon(
                            imageVector =
                                Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = CobreHome
                        )
                    }
                }
            }

            Spacer(Modifier.height(13.dp))

            // INDICADORES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {
                IndicadorDashboard(
                    modifier = Modifier.weight(1f),
                    icone = Icons.Outlined.PersonOutline,
                    numero = quantidadeClientes,
                    descricao = "Clientes cadastrados",
                    cor = CobreHome,
                    onClick = onClientesClick
                )

                IndicadorDashboard(
                    modifier = Modifier.weight(1f),
                    icone =
                        Icons.AutoMirrored.Outlined.Assignment,
                    numero = resumoInicial.emAndamento,
                    descricao = "Serviços em andamento",
                    cor = CobreHome,
                    onClick = onServicosClick
                )

                IndicadorDashboard(
                    modifier = Modifier.weight(1f),
                    icone =
                        Icons.Outlined.CheckCircleOutline,
                    numero = resumoInicial.concluidos,
                    descricao = "Serviços concluídos",
                    cor = VerdeHome,
                    onClick = onServicosClick
                )
            }

            Spacer(Modifier.height(16.dp))

            // GRÁFICO DE SERVIÇOS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Serviços por status",
                        color = AzulHome,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(15.dp))

                    GraficoDashboard(
                        status = resumoInicial.status,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(140.dp)
                    )

                    Spacer(Modifier.height(15.dp))

                    // Legenda textual: não depende apenas da cor.
                    resumoInicial.status.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .semantics(
                                    mergeDescendants = true
                                ) {
                                    contentDescription =
                                        "${item.nome}: ${item.quantidade} serviços"
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(
                                        color = item.cor,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = item.nome,
                                color = AzulHome,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = item.quantidade.toString(),
                                color = AzulHome,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // PRÓXIMAS ENTREGAS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Próximas entregas",
                        color = AzulHome,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    if (resumoInicial.entregas.isEmpty()) {
                        Text(
                            text = "Nenhuma entrega cadastrada.",
                            color = CinzaHome,
                            fontSize = 14.sp
                        )
                    } else {
                        resumoInicial.entregas.forEachIndexed {
                                indice, entrega ->

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(
                                        mergeDescendants = true
                                    ) {
                                        contentDescription =
                                            "Serviço: ${entrega.titulo}. " +
                                                    "Cliente: ${entrega.cliente}. " +
                                                    "Prazo: ${entrega.prazo}. " +
                                                    "Situação: ${entrega.status}."
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = entrega.titulo,
                                    color = AzulHome,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = "Cliente: ${entrega.cliente}",
                                    color = CinzaHome,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = "Prazo: ${entrega.prazo}",
                                    color = CinzaHome,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = "Situação: ${entrega.status}",
                                    color = CobreHome,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (
                                indice != resumoInicial.entregas.lastIndex
                            ) {
                                HorizontalDivider(
                                    color = Color(0xFF89969E)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
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
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 124.dp)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Ver $descricao. Quantidade: $numero."
            },
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
            modifier = Modifier.padding(10.dp),
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(23.dp)
            )

            Text(
                text = numero.toString(),
                color = AzulHome,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = descricao,
                color = CinzaHome,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GraficoDashboard(
    status: List<StatusServico>,
    modifier: Modifier = Modifier
) {
    val total = status.sumOf {
        it.quantidade
    }

    val descricaoGrafico = buildString {
        append("Gráfico de serviços por status. ")
        append("Total: $total serviços. ")

        status.forEach { item ->
            append("${item.nome}: ${item.quantidade}. ")
        }
    }

    Box(
        modifier = modifier.semantics(
            mergeDescendants = true
        ) {
            contentDescription = descricaoGrafico
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val espessura = 19.dp.toPx()
            val margem = espessura / 2f
            val diametro = (
                    size.minDimension - espessura
                    ).coerceAtLeast(0f)

            var angulo = -90f

            status.forEach { item ->
                val parte = if (total > 0) {
                    360f * item.quantidade / total
                } else {
                    0f
                }

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
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = total.toString(),
                color = AzulHome,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Text(
                text = "Total",
                color = CinzaHome,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RowScope.ItemMenuHome(
    titulo: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selecionado,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icone,
                contentDescription = null,
                modifier = Modifier.size(23.dp)
            )
        },
        label = {
            Text(
                text = titulo,
                fontSize = 12.sp
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AzulHome,
            selectedTextColor = AzulHome,
            indicatorColor = Color(0xFFF1E8DE),
            unselectedIconColor = CinzaHome,
            unselectedTextColor = CinzaHome
        )
    )
}
