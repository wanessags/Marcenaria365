
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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.MoreVert
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
private val BordaMais = Color(0xFFEDE8E2)

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

    var opcaoEmDesenvolvimento by remember {
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

        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(AzulMais)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )

                Text(
                    text = "Mais",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                IconButton(
                    onClick = {
                        opcaoEmDesenvolvimento = "Configurações"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Opções",
                        tint = Color.White
                    )
                }
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.height(75.dp)
            ) {
                itemMenuMais(
                    titulo = "Início",
                    icone = Icons.Outlined.Home,
                    selecionado = false,
                    onClick = onInicioClick
                )

                itemMenuMais(
                    titulo = "Clientes",
                    icone = Icons.Outlined.PeopleOutline,
                    selecionado = false,
                    onClick = onClientesClick
                )

                itemMenuMais(
                    titulo = "Serviços",
                    icone = Icons.AutoMirrored.Outlined.Assignment,
                    selecionado = false,
                    onClick = onServicosClick
                )

                itemMenuMais(
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

            // Cartão superior do usuário
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        opcaoEmDesenvolvimento = "Meu perfil"
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
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulMais,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = emailUsuario.ifBlank {
                                "Dados da conta"
                            },
                            fontSize = 11.sp,
                            color = CinzaMais,
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

            Spacer(Modifier.height(23.dp))

            // Opções da tela Mais
            OpcaoMais(
                titulo = "Perfil",
                icone = Icons.Outlined.PersonOutline,
                onClick = {
                    opcaoEmDesenvolvimento = "Meu perfil"
                }
            )

            OpcaoMais(
                titulo = "Materiais",
                icone = Icons.Outlined.Inventory2,
                onClick = {
                    opcaoEmDesenvolvimento = "Materiais"
                }
            )

            OpcaoMais(
                titulo = "Configurações",
                icone = Icons.Outlined.Settings,
                onClick = {
                    opcaoEmDesenvolvimento = "Configurações"
                }
            )

            OpcaoMais(
                titulo = "Central de atendimento",
                icone = Icons.Outlined.Message,
                onClick = {
                    opcaoEmDesenvolvimento =
                        "Central de atendimento"
                }
            )

            OpcaoMais(
                titulo = "Ajuda e suporte",
                icone = Icons.Outlined.HelpOutline,
                onClick = {
                    opcaoEmDesenvolvimento = "Ajuda e suporte"
                }
            )

            OpcaoMais(
                titulo = "Política de Privacidade",
                icone = Icons.Outlined.Description,
                onClick = {
                    opcaoEmDesenvolvimento =
                        "Política de Privacidade"
                }
            )

            Spacer(Modifier.height(25.dp))

            // Sair da conta
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
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
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

            Spacer(Modifier.height(24.dp))
        }
    }

    // Aviso temporário para opções futuras
    if (opcaoEmDesenvolvimento != null) {
        AlertDialog(
            onDismissRequest = {
                opcaoEmDesenvolvimento = null
            },
            title = {
                Text(
                    text = opcaoEmDesenvolvimento.orEmpty(),
                    color = AzulMais,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta funcionalidade será adicionada " +
                            "nas próximas etapas do Marcenaria 365.",
                    color = AzulMais
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        opcaoEmDesenvolvimento = null
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
                        color = VermelhoMais,
                        fontWeight = FontWeight.SemiBold
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
private fun RowScope.itemMenuMais(
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
                contentDescription = titulo
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
