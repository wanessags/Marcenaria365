
package com.nexo.marcenaria365.ui.screens.mais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.perfil.FotoPerfil

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
    val cores = MaterialTheme.colorScheme

    var confirmarSaida by remember {
        mutableStateOf(false)
    }

    var mostrarAtendimento by remember {
        mutableStateOf(false)
    }

    BackHandler(
        enabled = !confirmarSaida && !mostrarAtendimento
    ) {
        onInicioClick()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),

        containerColor = cores.background,

        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF193447))
                    .heightIn(min = 62.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onInicioClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Mais",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.semantics {
                        heading()
                    }
                )
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = cores.surface,
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

            // PERFIL DO USUÁRIO
            Card(
                onClick = onPerfilClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cores.surface
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
                            color = cores.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = emailUsuario.ifBlank {
                                "Acessar meu perfil"
                            },
                            color = cores.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = cores.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Ferramentas e preferências",
                color = cores.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(12.dp))

            // MATERIAIS
            OpcaoMais(
                titulo = "Materiais",
                icone = Icons.Outlined.Inventory2,
                onClick = onMateriaisClick
            )

            // A opção Aparência foi ocultada no MVP.
            // O tema claro é aplicado na MainActivity.
            // O histórico Git mantém a implementação anterior.

            // CENTRAL DE ATENDIMENTO
            OpcaoMais(
                titulo = "Central de atendimento",
                icone = Icons.Outlined.Message,
                onClick = {
                    mostrarAtendimento = true
                }
            )

            // AJUDA E SUPORTE
            OpcaoMais(
                titulo = "Ajuda e suporte",
                icone = Icons.Outlined.HelpOutline,
                onClick = onAjudaClick
            )

            // POLÍTICA DE PRIVACIDADE
            OpcaoMais(
                titulo = "Política de Privacidade",
                icone = Icons.Outlined.Description,
                onClick = onPoliticaClick
            )

            Spacer(Modifier.height(25.dp))

            // SAIR DA CONTA
            OutlinedButton(
                onClick = {
                    confirmarSaida = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = cores.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = cores.surface,
                    contentColor = cores.error
                )
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Sair da conta",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // MENSAGEM DA CENTRAL DE ATENDIMENTO
    if (mostrarAtendimento) {
        AlertDialog(
            onDismissRequest = {
                mostrarAtendimento = false
            },
            title = {
                Text("Central de atendimento")
            },
            text = {
                Text(
                    "Esta funcionalidade ainda não está disponível."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarAtendimento = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Entendi")
                }
            }
        )
    }

    // CONFIRMAÇÃO DE SAÍDA
    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = {
                confirmarSaida = false
            },
            title = {
                Text("Sair da conta?")
            },
            text = {
                Text(
                    "Deseja encerrar a sessão e voltar para o login?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                        onSair()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Sair",
                        color = cores.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarSaida = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OpcaoMais(
    titulo: String,
    icone: ImageVector,
    subtitulo: String? = null,
    onClick: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .heightIn(min = 64.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = cores.secondary,
            modifier = Modifier.size(23.dp)
        )

        Spacer(Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                color = cores.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (subtitulo != null) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = subtitulo,
                    color = cores.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = cores.onSurfaceVariant,
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
    val cores = MaterialTheme.colorScheme

    NavigationBarItem(
        selected = selecionado,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icone,
                contentDescription = null
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
            selectedIconColor = cores.primary,
            selectedTextColor = cores.onSurface,
            indicatorColor = cores.primaryContainer,
            unselectedIconColor = cores.onSurfaceVariant,
            unselectedTextColor = cores.onSurfaceVariant
        )
    )
}
