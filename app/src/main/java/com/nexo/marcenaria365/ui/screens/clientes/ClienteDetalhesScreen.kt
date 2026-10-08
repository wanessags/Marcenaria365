
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi

private val FundoDetalhes = Color(0xFFFAF6F0)
private val AzulDetalhes = Color(0xFF193447)
private val CobreDetalhes = Color(0xFFBD906E)
private val CinzaDetalhes = Color(0xFF929292)
private val BordaDetalhes = Color(0xFFE9E3DD)
private val VerdeDetalhes = Color(0xFF27845D)
private val VermelhoDetalhes = Color(0xFFB34D4D)

@Composable
fun ClienteDetalhesScreen(
    cliente: ClienteUi,
    servicos: List<ServicoUi> = emptyList(),
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: (Long) -> Unit,
    onAbrirServico: (ServicoUi) -> Unit = {}
) {
    var confirmarExclusao by remember {
        mutableStateOf(false)
    }

    var mostrarBloqueio by remember {
        mutableStateOf(false)
    }

    val servicosVinculados = servicos.filter {
        it.clienteId == cliente.id
    }

    BackHandler(
        enabled = !confirmarExclusao && !mostrarBloqueio,
        onBack = onVoltar
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDetalhes)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = AzulDetalhes
                )
            }

            Text(
                text = "Detalhes do cliente",
                color = AzulDetalhes,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Identificação do cliente
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val iniciais = cliente.nome
                    .trim()
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .mapNotNull { it.firstOrNull() }
                    .joinToString("")
                    .uppercase()

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Color(0xFFB98C69),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciais,
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = cliente.nome,
                    color = AzulDetalhes,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // Badge pequeno
                Surface(
                    color = if (cliente.ativo) {
                        Color(0xFFE5F4EC)
                    } else {
                        Color(0xFFF1EEEE)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (cliente.ativo) {
                            "Ativo"
                        } else {
                            "Inativo"
                        },
                        color = if (cliente.ativo) {
                            VerdeDetalhes
                        } else {
                            CinzaDetalhes
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = 7.dp,
                            vertical = 3.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Informações de contato",
                color = AzulDetalhes,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    CampoCliente(
                        titulo = "Nome completo",
                        valor = cliente.nome
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 16.dp
                        ),
                        color = BordaDetalhes
                    )

                    CampoCliente(
                        titulo = "Telefone",
                        valor = cliente.telefone
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 16.dp
                        ),
                        color = BordaDetalhes
                    )

                    CampoCliente(
                        titulo = "E-mail",
                        valor = cliente.email.ifBlank {
                            "Não informado"
                        }
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            // Serviços relacionados ao cliente
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Serviços vinculados",
                    color = AzulDetalhes,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (servicosVinculados.size == 1) {
                        "1 serviço"
                    } else {
                        "${servicosVinculados.size} serviços"
                    },
                    color = CinzaDetalhes,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 17.dp,
                        vertical = 12.dp
                    )
                ) {
                    if (servicosVinculados.isEmpty()) {
                        Text(
                            text = "Nenhum serviço vinculado a este cliente.",
                            color = CinzaDetalhes,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(
                                vertical = 14.dp
                            )
                        )
                    } else {
                        servicosVinculados.forEachIndexed {
                                indice, servico ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAbrirServico(servico)
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = servico.titulo,
                                        color = AzulDetalhes,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = "Entrega: ${servico.entrega}",
                                        color = CinzaDetalhes,
                                        fontSize = 11.sp
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = servico.status,
                                        color = when (
                                            servico.status
                                        ) {
                                            "Concluído" -> VerdeDetalhes
                                            "Em produção" -> CobreDetalhes
                                            else -> CinzaDetalhes
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Icon(
                                    imageVector =
                                        Icons.Outlined.ChevronRight,
                                    contentDescription = "Abrir serviço",
                                    tint = CinzaDetalhes,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            if (
                                indice != servicosVinculados.lastIndex
                            ) {
                                HorizontalDivider(
                                    color = BordaDetalhes
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            // Editar cliente
            Button(
                onClick = onEditar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulDetalhes,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Editar cliente",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Excluir cliente
            TextButton(
                onClick = {
                    if (servicosVinculados.isNotEmpty()) {
                        mostrarBloqueio = true
                    } else {
                        confirmarExclusao = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VermelhoDetalhes
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = VermelhoDetalhes,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir cliente",
                    color = VermelhoDetalhes,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Confirmação de exclusão
    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },
            title = {
                Text(
                    text = "Excluir cliente?",
                    color = AzulDetalhes,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja excluir ${cliente.nome}? " +
                            "Essa ação não poderá ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluir(cliente.id)
                    }
                ) {
                    Text(
                        text = "Excluir",
                        color = VermelhoDetalhes
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulDetalhes
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // Proteção para clientes com serviços
    if (mostrarBloqueio) {
        AlertDialog(
            onDismissRequest = {
                mostrarBloqueio = false
            },
            title = {
                Text(
                    text = "Cliente com serviços vinculados",
                    color = AzulDetalhes,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Este cliente possui serviços cadastrados. " +
                            "Para preservar o histórico, ele não pode " +
                            "ser excluído. Você pode marcá-lo como " +
                            "inativo na edição."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarBloqueio = false
                        onEditar()
                    }
                ) {
                    Text(
                        text = "Editar cliente",
                        color = AzulDetalhes
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarBloqueio = false
                    }
                ) {
                    Text(
                        text = "Fechar",
                        color = CinzaDetalhes
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }
}

@Composable
private fun CampoCliente(
    titulo: String,
    valor: String
) {
    Column {
        Text(
            text = titulo,
            color = AzulDetalhes,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(7.dp))

        Text(
            text = valor,
            color = CinzaDetalhes,
            fontSize = 13.sp
        )
    }
}
