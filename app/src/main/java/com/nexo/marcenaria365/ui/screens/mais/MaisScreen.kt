
package com.nexo.marcenaria365.ui.screens.mais

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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoMais = Color(0xFFFAF6F0)
private val AzulMais = Color(0xFF193447)
private val CobreMais = Color(0xFFBD906E)
private val CinzaMais = Color(0xFF929292)
private val VermelhoMais = Color(0xFFCF6666)

@Composable
fun MaisScreen(
    nomeUsuario: String,
    emailUsuario: String = "",
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit,
    onServicosClick: () -> Unit,
    onSair: () -> Unit
) {
    var confirmarSaida by remember {
        mutableStateOf(false)
    }

    var opcaoSelecionada by remember {
        mutableStateOf<String?>(null)
    }

    val iniciais = nomeUsuario
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "M" }

    BackHandler(onBack = onInicioClick)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),

        containerColor = FundoMais,

        // Cabeçalho com seta e título centralizado
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(AzulMais)
            ) {
                IconButton(
                    onClick = onInicioClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Text(
                    text = "Mais",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },

        // Barra inferior fixa
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp),
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                ItemMenuMais(
                    titulo = "Início",
                    icone = Icons.Outlined.Home,
                    selecionado = false,
                    onClick = onInicioClick
                )

                ItemMenuMais(
                    titulo = "Clientes",
                    icone = Icons.Outlined.PeopleOutline,
                    selecionado = false,
                    onClick = onClientesClick
                )

                ItemMenuMais(
                    titulo = "Serviços",
                    icone = Icons.AutoMirrored.Outlined.Assignment,
                    selecionado = false,
                    onClick = onServicosClick
                )

                ItemMenuMais(
                    titulo = "Mais",
                    icone = Icons.Outlined.Menu,
                    selecionado = true,
                    onClick = {}
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Cartão do usuário
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        opcaoSelecionada = "Meu perfil"
                    },
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                Color(0xFFEFE4D9),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciais,
                            color = CobreMais,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = nomeUsuario.ifBlank {
                                "Meu perfil"
                            },
                            color = AzulMais,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = emailUsuario.ifBlank {
                                "Dados da conta"
                            },
                            color = CinzaMais,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Abrir perfil",
                        tint = CinzaMais,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Opções do menu
            OpcaoMais(
                titulo = "Perfil",
                icone = Icons.Outlined.PersonOutline,
                onClick = {
                    opcaoSelecionada = "Meu perfil"
                }
            )

            OpcaoMais(
                titulo = "Materiais",
                icone = Icons.Outlined.Inventory2,
                onClick = {
                    opcaoSelecionada = "Materiais"
                }
            )

            OpcaoMais(
                titulo = "Configurações",
                icone = Icons.Outlined.Settings,
                onClick = {
                    opcaoSelecionada = "Configurações"
                }
            )

            OpcaoMais(
                titulo = "Central de atendimento",
                icone = Icons.Outlined.Message,
                onClick = {
                    opcaoSelecionada = "Central de atendimento"
                }
            )

            OpcaoMais(
                titulo = "Ajuda e suporte",
                icone = Icons.Outlined.HelpOutline,
                onClick = {
                    opcaoSelecionada = "Ajuda e suporte"
                }
            )

            OpcaoMais(
                titulo = "Política de Privacidade",
                icone = Icons.Outlined.Description,
                onClick = {
                    opcaoSelecionada = "Política de Privacidade"
                }
            )

            Spacer(Modifier.height(25.dp))

            // Botão sair da conta
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        confirmarSaida = true
                    },
                shape = RoundedCornerShape(11.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = VermelhoMais,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        text = "Sair da conta",
                        color = VermelhoMais,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(25.dp))
        }
    }

    // Opções ainda não implementadas
    if (opcaoSelecionada != null) {
        AlertDialog(
            onDismissRequest = {
                opcaoSelecionada = null
            },
            title = {
                Text(
                    text = opcaoSelecionada.orEmpty(),
                    color = AzulMais,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta funcionalidade ainda não está disponível."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        opcaoSelecionada = null
                    }
                ) {
                    Text(
                        text = "Entendi",
                        color = AzulMais
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // Confirmação para sair da conta
    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = {
                confirmarSaida = false
            },
            title = {
                Text(
                    text = "Sair da conta?",
                    color = AzulMais,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja voltar para a tela de login?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                        onSair()
                    }
                ) {
                    Text(
                        text = "Sair",
                        color = VermelhoMais
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulMais
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }
}

@Composable
private fun OpcaoMais(
    titulo: String,
    icone: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 62.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = CinzaMais,
            modifier = Modifier.size(21.dp)
        )

        Spacer(Modifier.width(19.dp))

        Text(
            text = titulo,
            color = AzulMais,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Abrir $titulo",
            tint = CinzaMais,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RowScope.ItemMenuMais(
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
                contentDescription = titulo,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = titulo,
                fontSize = 10.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AzulMais,
            selectedTextColor = AzulMais,
            indicatorColor = Color(0xFFF1E8DE),
            unselectedIconColor = CinzaMais,
            unselectedTextColor = CinzaMais
        )
    )
}
