
package com.nexo.marcenaria365.ui.screens.mais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.perfil.FotoPerfil

private val FundoMais = Color(0xFFFAF6F0)
private val AzulMais = Color(0xFF193447)
private val CobreMais = Color(0xFF94613C)
private val CinzaMais = Color(0xFF53616A)
private val BordaMais = Color(0xFF89969E)
private val VermelhoMais = Color(0xFFB3261E)

@Composable
fun MaisScreen(
    nomeUsuario: String,
    emailUsuario: String = "",
    fotoUsuario: String? = null,
    onPerfilClick: () -> Unit = {},
    onMateriaisClick: () -> Unit = {},
    onAjudaClick: () -> Unit = {},
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit,
    onServicosClick: () -> Unit,
    onPoliticaClick: () -> Unit,
    onSair: () -> Unit
) {
    var confirmarSaida by remember {
        mutableStateOf(false)
    }

    var mostrarConfiguracoes by remember {
        mutableStateOf(false)
    }

    var opcaoSelecionada by remember {
        mutableStateOf<String?>(null)
    }

    BackHandler(
        enabled = !confirmarSaida &&
                !mostrarConfiguracoes &&
                opcaoSelecionada == null,
        onBack = onInicioClick
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoMais,

        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp)
                    .background(AzulMais)
            ) {
                IconButton(
                    onClick = onInicioClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .sizeIn(
                            minWidth = 48.dp,
                            minHeight = 48.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Mais",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics {
                            heading()
                        }
                )
            }
        },

        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 75.dp),
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
                    icone =
                        Icons.AutoMirrored.Outlined.Assignment,
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

            // ACESSO AO PERFIL
            var perfilFocado by remember {
                mutableStateOf(false)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        perfilFocado = it.isFocused
                    }
                    .then(
                        if (perfilFocado) {
                            Modifier.border(
                                width = 2.dp,
                                color = AzulMais,
                                shape = RoundedCornerShape(15.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        role = Role.Button,
                        onClick = onPerfilClick
                    )
                    .semantics(mergeDescendants = true) {
                        contentDescription = buildString {
                            append("Abrir meu perfil. ")
                            append(
                                "Nome: ${
                                    nomeUsuario.ifBlank {
                                        "Não informado"
                                    }
                                }. "
                            )
                            append(
                                "E-mail: ${
                                    emailUsuario.ifBlank {
                                        "Não informado"
                                    }
                                }."
                            )
                        }
                    },
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = BordaMais
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FotoPerfil(
                        nome = nomeUsuario,
                        fotoUri = fotoUsuario,
                        modifier = Modifier.size(54.dp)
                    )

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
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = emailUsuario.ifBlank {
                                "Toque para acessar seu perfil"
                            },
                            color = CinzaMais,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector =
                            Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = CinzaMais,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Ferramentas e configurações",
                color = AzulMais,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(9.dp))

            OpcaoMais(
                titulo = "Materiais",
                icone = Icons.Outlined.Inventory2,
                onClick = onMateriaisClick
            )

            OpcaoMais(
                titulo = "Configurações",
                icone = Icons.Outlined.Settings,
                onClick = {
                    mostrarConfiguracoes = true
                }
            )

            OpcaoMais(
                titulo = "Central de atendimento",
                icone = Icons.Outlined.Message,
                onClick = {
                    opcaoSelecionada =
                        "Central de atendimento"
                }
            )

            OpcaoMais(
                titulo = "Ajuda e suporte",
                icone = Icons.Outlined.HelpOutline,
                onClick = onAjudaClick
            )

            OpcaoMais(
                titulo = "Política de Privacidade",
                icone = Icons.Outlined.Description,
                onClick = onPoliticaClick
            )

            Spacer(Modifier.height(25.dp))

            // SAIR DA CONTA
            var sairFocado by remember {
                mutableStateOf(false)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        sairFocado = it.isFocused
                    }
                    .then(
                        if (sairFocado) {
                            Modifier.border(
                                width = 2.dp,
                                color = AzulMais,
                                shape = RoundedCornerShape(11.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            confirmarSaida = true
                        }
                    ),
                shape = RoundedCornerShape(11.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = BordaMais
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = VermelhoMais,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        text = "Sair da conta",
                        color = VermelhoMais,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(25.dp))
        }
    }

    // CONFIGURAÇÕES
    if (mostrarConfiguracoes) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfiguracoes = false
            },
            title = {
                Text(
                    text = "Configurações",
                    color = AzulMais,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Minha conta",
                        fontWeight = FontWeight.SemiBold,
                        color = AzulMais,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Text(
                        text = "Nome: ${
                            nomeUsuario.ifBlank {
                                "Não informado"
                            }
                        }",
                        color = CinzaMais,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "E-mail: ${
                            emailUsuario.ifBlank {
                                "Não informado"
                            }
                        }",
                        color = CinzaMais,
                        fontSize = 14.sp
                    )

                    HorizontalDivider(
                        color = BordaMais
                    )

                    TextButton(
                        onClick = {
                            mostrarConfiguracoes = false
                            onPerfilClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Acessar meu perfil",
                            color = AzulMais
                        )
                    }

                    TextButton(
                        onClick = {
                            mostrarConfiguracoes = false
                            onPoliticaClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Política de Privacidade",
                            color = AzulMais
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfiguracoes = false
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Fechar",
                        color = AzulMais
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(17.dp)
        )
    }

    // CENTRAL DE ATENDIMENTO
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
                    text = "Esta funcionalidade ainda não está disponível.",
                    color = CinzaMais,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        opcaoSelecionada = null
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
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

    // CONFIRMAÇÃO DE SAÍDA
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
                    text = "Deseja encerrar sua sessão e voltar para a tela de login?",
                    color = CinzaMais,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                        onSair()
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Sair",
                        color = VermelhoMais,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
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
    var focado by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                focado = it.isFocused
            }
            .then(
                if (focado) {
                    Modifier.border(
                        width = 2.dp,
                        color = AzulMais,
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "Abrir $titulo"
            }
            .heightIn(min = 62.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = CobreMais,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(19.dp))

        Text(
            text = titulo,
            color = AzulMais,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = CinzaMais,
            modifier = Modifier.size(20.dp)
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
                contentDescription = null,
                modifier = Modifier.size(22.dp)
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
            selectedIconColor = AzulMais,
            selectedTextColor = AzulMais,
            indicatorColor = Color(0xFFF1E8DE),
            unselectedIconColor = CinzaMais,
            unselectedTextColor = CinzaMais
        )
    )
}
