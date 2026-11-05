
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi

private val FundoDetalhesCliente = Color(0xFFFAF6F0)
private val AzulDetalhesCliente = Color(0xFF193447)
private val CobreDetalhesCliente = Color(0xFF94613C)
private val CinzaDetalhesCliente = Color(0xFF53616A)
private val BordaDetalhesCliente = Color(0xFF89969E)
private val VerdeDetalhesCliente = Color(0xFF237A58)
private val VermelhoDetalhesCliente = Color(0xFFB3261E)

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

    val iniciais = cliente.nome
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "C" }

    BackHandler(
        enabled = !confirmarExclusao && !mostrarBloqueio,
        onBack = onVoltar
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDetalhesCliente)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // CABEÇALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.sizeIn(
                    minWidth = 48.dp,
                    minHeight = 48.dp
                )
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar para clientes",
                    tint = AzulDetalhesCliente
                )
            }

            Spacer(Modifier.width(6.dp))

            Text(
                text = "Detalhes do cliente",
                color = AzulDetalhesCliente,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // IDENTIFICAÇÃO
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            CobreDetalhesCliente,
                            CircleShape
                        )
                        .semantics {
                            contentDescription =
                                "Avatar de ${cliente.nome}"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciais,
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = cliente.nome,
                    color = AzulDetalhesCliente,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        heading()
                    }
                )

                Spacer(Modifier.height(10.dp))

                Surface(
                    color = if (cliente.ativo) {
                        Color(0xFFE5F4EC)
                    } else {
                        Color(0xFFF1EEEE)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (cliente.ativo) {
                            "Situação: Ativo"
                        } else {
                            "Situação: Inativo"
                        },
                        color = if (cliente.ativo) {
                            VerdeDetalhesCliente
                        } else {
                            AzulDetalhesCliente
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // INFORMAÇÕES DE CONTATO
            TituloSecaoClienteDetalhes(
                "Informações de contato"
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
                    CampoClienteDetalhesAcessivel(
                        titulo = "Nome completo",
                        valor = cliente.nome
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 16.dp
                        ),
                        color = BordaDetalhesCliente
                    )

                    CampoClienteDetalhesAcessivel(
                        titulo = "Telefone",
                        valor = cliente.telefone.ifBlank {
                            "Não informado"
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 16.dp
                        ),
                        color = BordaDetalhesCliente
                    )

                    CampoClienteDetalhesAcessivel(
                        titulo = "E-mail",
                        valor = cliente.email.ifBlank {
                            "Não informado"
                        }
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            // SERVIÇOS VINCULADOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TituloSecaoClienteDetalhes(
                        "Serviços vinculados"
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = if (servicosVinculados.size == 1) {
                        "1 serviço"
                    } else {
                        "${servicosVinculados.size} serviços"
                    },
                    color = CinzaDetalhesCliente,
                    fontSize = 13.sp
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
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                ) {
                    if (servicosVinculados.isEmpty()) {
                        Text(
                            text = "Nenhum serviço vinculado a este cliente.",
                            color = CinzaDetalhesCliente,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(
                                vertical = 14.dp
                            )
                        )
                    } else {
                        servicosVinculados.forEachIndexed {
                                indice, servico ->

                            val descricaoAcessivel =
                                "Abrir serviço ${servico.titulo}. " +
                                        "Entrega: ${servico.entrega}. " +
                                        "Situação: ${servico.status}."

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        role = Role.Button,
                                        onClick = {
                                            onAbrirServico(servico)
                                        }
                                    )
                                    .semantics(
                                        mergeDescendants = true
                                    ) {
                                        contentDescription =
                                            descricaoAcessivel
                                    }
                                    .heightIn(min = 80.dp)
                                    .padding(vertical = 12.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement =
                                        Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = servico.titulo,
                                        color = AzulDetalhesCliente,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "Entrega: ${servico.entrega}",
                                        color = CinzaDetalhesCliente,
                                        fontSize = 13.sp
                                    )

                                    Text(
                                        text = "Situação: ${servico.status}",
                                        color = when (
                                            servico.status
                                        ) {
                                            "Concluído" ->
                                                VerdeDetalhesCliente

                                            "Em produção" ->
                                                CobreDetalhesCliente

                                            else ->
                                                AzulDetalhesCliente
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Icon(
                                    imageVector =
                                        Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = CinzaDetalhesCliente,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            if (
                                indice != servicosVinculados.lastIndex
                            ) {
                                HorizontalDivider(
                                    color = BordaDetalhesCliente
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            // EDITAR
            Button(
                onClick = onEditar,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulDetalhesCliente,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Editar cliente",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            // EXCLUIR
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
                    contentColor = VermelhoDetalhesCliente
                )
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Excluir cliente",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(26.dp))
        }
    }

    // CONFIRMAÇÃO DE EXCLUSÃO
    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },
            title = {
                Text(
                    text = "Excluir cliente?",
                    color = AzulDetalhesCliente,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Cliente: ${cliente.nome}",
                        color = AzulDetalhesCliente,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Deseja excluir este cliente? " +
                                "Esta ação não poderá ser desfeita.",
                        color = CinzaDetalhesCliente,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluir(cliente.id)
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Excluir definitivamente",
                        color = VermelhoDetalhesCliente,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulDetalhesCliente
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // PROTEÇÃO DE CLIENTES COM SERVIÇOS
    if (mostrarBloqueio) {
        AlertDialog(
            onDismissRequest = {
                mostrarBloqueio = false
            },
            title = {
                Text(
                    text = "Cliente com serviços vinculados",
                    color = AzulDetalhesCliente,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Este cliente possui ${
                            servicosVinculados.size
                        } serviço(s) cadastrado(s).",
                        color = AzulDetalhesCliente
                    )

                    Text(
                        text = "Para preservar o histórico, " +
                                "ele não pode ser excluído. " +
                                "Você pode acessar a edição do cliente.",
                        color = CinzaDetalhesCliente,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarBloqueio = false
                        onEditar()
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Editar cliente",
                        color = AzulDetalhesCliente,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarBloqueio = false
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Fechar",
                        color = CinzaDetalhesCliente
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }
}

@Composable
private fun TituloSecaoClienteDetalhes(
    titulo: String
) {
    Text(
        text = titulo,
        color = AzulDetalhesCliente,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

@Composable
private fun CampoClienteDetalhesAcessivel(
    titulo: String,
    valor: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo: $valor"
            }
    ) {
        Text(
            text = titulo,
            color = AzulDetalhesCliente,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(7.dp))

        Text(
            text = valor,
            color = CinzaDetalhesCliente,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}
