
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
import androidx.compose.runtime.Composable
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoHome)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        // CONTEÚDO PRINCIPAL
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

            // SALDO EM CAIXA
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
                            text = "SALDO EM CAIXA",
                            color = CinzaHome,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = resumoInicial.saldo,
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
                            imageVector =
                                Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = CobreHome,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(11.dp))

            // INDICADORES CLICÁVEIS — SEM TEXTO EXTRA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                IndicadorDashboard(
                    modifier = Modifier.weight(1f),
                    icone = Icons.Outlined.PersonOutline,
                    numero = quantidadeClientes,
                    descricao = "Clientes\ncadastrados",
                    cor = CobreHome,
                    onClick = onClientesClick
                )

                IndicadorDashboard(
                    modifier = Modifier.weight(1f),
                    icone = Icons.AutoMirrored.Outlined.Assignment,
                    numero = resumoInicial.emAndamento,
                    descricao = "Serviços em\nandamento",
                    cor = CobreHome,
                    onClick = onServicosClick
                )

                IndicadorDashboard(
                    modifier = Modifier.weight(1f),
                    icone = Icons.Outlined.CheckCircleOutline,
                    numero = resumoInicial.concluidos,
                    descricao = "Serviços\nconcluídos",
                    cor = VerdeHome,
                    onClick = onServicosClick
                )
            }

            Spacer(Modifier.height(11.dp))

            // GRÁFICO DE SERVIÇOS POR STATUS
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
                            status = resumoInicial.status,
                            modifier = Modifier.size(132.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(13.dp)
                        ) {
                            resumoInicial.status.forEach { item ->
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

            // PRÓXIMAS ENTREGAS
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

                    resumoInicial.entregas.forEach { entrega ->
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
                                    text = "${entrega.cliente} · ${entrega.prazo}",
                                    color = CinzaHome,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = "● ${entrega.status}",
                                color = LaranjaHome,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
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

// CAIXINHA CLICÁVEL, SEM "VER" OU SETINHA
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

// GRÁFICO DE ROSCA
@Composable
private fun GraficoDashboard(
    status: List<StatusServico>,
    modifier: Modifier = Modifier
) {
    val total = status.sumOf {
        it.quantidade
    }

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
