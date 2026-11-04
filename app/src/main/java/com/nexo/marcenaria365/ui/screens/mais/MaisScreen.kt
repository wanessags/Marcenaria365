
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexo.marcenaria365.ui.screens.perfil.FotoPerfil

private val FundoMais = Color(0xFFFAF6F0)
private val AzulMais = Color(0xFF193447)
private val CinzaMais = Color(0xFF77828A)
private val VermelhoMais = Color(0xFFCF6666)
private val CobreMais = Color(0xFFB88966)
private val BordaMais = Color(0xFFEAE5DF)

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
        enabled = !mostrarConfiguracoes &&
                !confirmarSaida &&
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

            // CARTAO DO PERFIL
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPerfilClick),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = emailUsuario.ifBlank {
                                "Toque para acessar seu perfil"
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

            Spacer(Modifier.height(20.dp))

            // OPCOES DO MENU MAIS
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
                    opcaoSelecionada = "Central de atendimento"
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        confirmarSaida = true
                    },
                shape = RoundedCornerShape(11.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
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

    // NOVA JANELA DE CONFIGURACOES
    if (mostrarConfiguracoes) {
        Dialog(
            onDismissRequest = {
                mostrarConfiguracoes = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = true
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = FundoMais,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Color(0xFFF1E7DC),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = CobreMais,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Configurações",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulMais
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Gerencie sua conta e informações",
                        color = CinzaMais,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // INFORMACOES DA CONTA
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            BordaMais
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp)
                        ) {
                            Text(
                                text = "MINHA CONTA",
                                color = CobreMais,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FotoPerfil(
                                    nome = nomeUsuario,
                                    fotoUri = fotoUsuario,
                                    modifier = Modifier.size(44.dp)
                                )

                                Spacer(Modifier.width(11.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = nomeUsuario.ifBlank {
                                            "Nome não informado"
                                        },
                                        color = AzulMais,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(Modifier.height(3.dp))

                                    Text(
                                        text = emailUsuario.ifBlank {
                                            "E-mail não informado"
                                        },
                                        color = CinzaMais,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ACESSAR PERFIL
                    OpcaoConfiguracaoMais(
                        titulo = "Meu perfil",
                        descricao = "Visualizar e editar meus dados",
                        icone = Icons.Outlined.PersonOutline,
                        onClick = {
                            mostrarConfiguracoes = false
                            onPerfilClick()
                        }
                    )

                    Spacer(Modifier.height(9.dp))

                    // PRIVACIDADE
                    OpcaoConfiguracaoMais(
                        titulo = "Privacidade",
                        descricao = "Consultar a política de dados",
                        icone = Icons.Outlined.Description,
                        onClick = {
                            mostrarConfiguracoes = false
                            onPoliticaClick()
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    // FECHAR JANELA
                    Button(
                        onClick = {
                            mostrarConfiguracoes = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulMais,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Fechar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
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
                    "Esta funcionalidade ainda não está disponível."
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

    // CONFIRMACAO DE SAIDA
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
                    "Deseja voltar para a tela de login?"
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

// OPCAO DENTRO DAS CONFIGURACOES
@Composable
private fun OpcaoConfiguracaoMais(
    titulo: String,
    descricao: String,
    icone: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BordaMais
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 13.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(37.dp)
                    .background(
                        Color(0xFFF5EFE8),
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = CobreMais,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = AzulMais,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = descricao,
                    color = CinzaMais,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(5.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Abrir $titulo",
                tint = CinzaMais,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// OPCAO DO MENU MAIS
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

// MENU INFERIOR
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
