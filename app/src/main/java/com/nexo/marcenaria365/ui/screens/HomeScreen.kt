
package com.nexo.marcenaria365.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircleOutline
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

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Verde = Color(0xFF239B7A)
private val Laranja = Color(0xFFF6A823)
private val Vermelho = Color(0xFFE47779)
private val CinzaGrafico = Color(0xFFC8C8C6)

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

// Dados temporários até a integração com a API.
private val resumoInicial = ResumoDashboard(
    saldo = "R$ 4.850,00",
    clientes = 6,
    emAndamento = 8,
    concluidos = 4,
    status = listOf(
        StatusServico("Orçamentos", 4, Laranja),
        StatusServico("Produção", 4, CinzaGrafico),
        StatusServico("Concluídos", 7, Verde),
        StatusServico("Pendentes", 1, Vermelho)
    ),
    entregas = listOf(
        EntregaResumo(
            "Cozinha planejada",
            "Ana Silva",
            "15/10",
            "Em produção"
        ),
        EntregaResumo(
            "Rack para sala",
            "Bruno Souza",
            "18/10",
            "Em produção"
        )
    )
)

@Composable
fun HomeScreen(
    nomeUsuario: String,
    onSair: () -> Unit
) {
    var abaAtual by remember {
        mutableStateOf("Início")
    }

    var mostrarSaida by remember {
        mutableStateOf(false)
    }

    val primeiroNome = nomeUsuario
        .trim()
        .substringBefore(" ")
        .ifBlank { "Usuário" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(15.dp))

            Text(
                text = "Olá, $primeiroNome!",
                color = Azul,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Confira o resumo da sua marcenaria hoje.",
                color = Cinza,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(13.dp))

            CartaoSaldo(resumoInicial.saldo)

            Spacer(Modifier.height(11.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Indicador(
                    modifier = Modifier.weight(1f),
                    icone = Icons.Outlined.PersonOutline,
                    numero = resumoInicial.clientes,
                    descricao = "Clientes\ncadastrados",
                    cor = Cobre
                )

                Indicador(
                    modifier = Modifier.weight(1f),
                    icone = Icons.AutoMirrored.Outlined.Assignment,
                    numero = resumoInicial.emAndamento,
                    descricao = "Serviços em\nandamento",
                    cor = Cobre
                )

                Indicador(
                    modifier = Modifier.weight(1f),
                    icone = Icons.Outlined.CheckCircleOutline,
                    numero = resumoInicial.concluidos,
                    descricao = "Serviços\nconcluídos",
                    cor = Verde
                )
            }

            Spacer(Modifier.height(11.dp))

            CartaoGrafico(resumoInicial.status)

            Spacer(Modifier.height(11.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(13.dp)
                ) {
                    Text(
                        text = "Próximas entregas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Azul
                    )

                    Spacer(Modifier.height(12.dp))

                    resumoInicial.entregas.forEach { entrega ->
                        LinhaEntrega(entrega)
                        Spacer(Modifier.height(11.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(75.dp)
        ) {
            NavigationBarItem(
                selected = abaAtual == "Início",
                onClick = { abaAtual = "Início" },
                icon = {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = "Início"
                    )
                },
                label = { Text("Início", fontSize = 10.sp) },
                colors = coresMenu()
            )

            NavigationBarItem(
                selected = abaAtual == "Clientes",
                onClick = { abaAtual = "Clientes" },
                icon = {
                    Icon(
                        Icons.Outlined.PeopleOutline,
                        contentDescription = "Clientes"
                    )
                },
                label = { Text("Clientes", fontSize = 10.sp) },
                colors = coresMenu()
            )

            NavigationBarItem(
                selected = abaAtual == "Serviços",
                onClick = { abaAtual = "Serviços" },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = "Serviços"
                    )
                },
                label = { Text("Serviços", fontSize = 10.sp) },
                colors = coresMenu()
            )

            NavigationBarItem(
                selected = abaAtual == "Mais",
                onClick = {
                    abaAtual = "Mais"
                    mostrarSaida = true
                },
                icon = {
                    Icon(
                        Icons.Outlined.Menu,
                        contentDescription = "Mais"
                    )
                },
                label = { Text("Mais", fontSize = 10.sp) },
                colors = coresMenu()
            )
        }
    }

    if (abaAtual == "Clientes" || abaAtual == "Serviços") {
        AlertDialog(
            onDismissRequest = {
                abaAtual = "Início"
            },
            title = { Text(abaAtual) },
            text = {
                Text("Esta área será implementada em breve.")
            },
            confirmButton = {
                TextButton(
                    onClick = { abaAtual = "Início" }
                ) {
                    Text("Entendi", color = Azul)
                }
            }
        )
    }

    if (mostrarSaida) {
        AlertDialog(
            onDismissRequest = {
                mostrarSaida = false
                abaAtual = "Início"
            },
            title = { Text("Sair da conta") },
            text = {
                Text("Deseja voltar para a tela de login?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarSaida = false
                        onSair()
                    }
                ) {
                    Text("Sair", color = Azul)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarSaida = false
                        abaAtual = "Início"
                    }
                ) {
                    Text("Cancelar", color = Cinza)
                }
            }
        )
    }
}

@Composable
private fun coresMenu() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = Azul,
        selectedTextColor = Azul,
        indicatorColor = Fundo,
        unselectedIconColor = Cinza,
        unselectedTextColor = Cinza
    )

@Composable
private fun CartaoSaldo(saldo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Cobre),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(33.dp)
                )
            }

            Spacer(Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "SALDO EM CAIXA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cinza
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = saldo,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Azul,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Ver detalhes ›",
                    fontSize = 11.sp,
                    color = Cinza
                )
            }
        }
    }
}

@Composable
private fun Indicador(
    modifier: Modifier,
    icone: ImageVector,
    numero: Int,
    descricao: String,
    cor: Color
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                color = Azul,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = descricao,
                color = Cinza,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun CartaoGrafico(status: List<StatusServico>) {
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Azul
            )

            Spacer(Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GraficoCircular(
                    status = status,
                    modifier = Modifier.size(132.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    status.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
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
                                fontSize = 10.sp,
                                color = Azul,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(Modifier.width(4.dp))

                            Text(
                                text = item.quantidade.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Azul
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraficoCircular(
    status: List<StatusServico>,
    modifier: Modifier
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
                color = Azul,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Total",
                color = Cinza,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun LinhaEntrega(entrega: EntregaResumo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                imageVector = Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null,
                tint = Cobre,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(9.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entrega.titulo,
                color = Azul,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${entrega.cliente} · ${entrega.prazo}",
                color = Cinza,
                fontSize = 10.sp
            )
        }

        Text(
            text = "● ${entrega.status}",
            fontSize = 9.sp,
            color = Laranja,
            maxLines = 1
        )
    }
}
