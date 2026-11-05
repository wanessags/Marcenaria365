
package com.nexo.marcenaria365.ui.screens.mais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.perfil.FotoPerfil
import com.nexo.marcenaria365.ui.theme.ModoAparencia
import com.nexo.marcenaria365.ui.theme.PreferenciasAparencia

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
    val context = LocalContext.current
    val cores = MaterialTheme.colorScheme

    var mostrandoAparencia by remember {
        mutableStateOf(false)
    }

    var confirmarSaida by remember {
        mutableStateOf(false)
    }

    var mostrarAtendimento by remember {
        mutableStateOf(false)
    }

    val modoAtual = PreferenciasAparencia.modo

    BackHandler(
        enabled = mostrandoAparencia ||
                !confirmarSaida && !mostrarAtendimento
    ) {
        if (mostrandoAparencia) {
            mostrandoAparencia = false
        } else {
            onInicioClick()
        }
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
                    onClick = {
                        if (mostrandoAparencia) {
                            mostrandoAparencia = false
                        } else {
                            onInicioClick()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = if (
                            mostrandoAparencia
                        ) {
                            "Voltar para Mais"
                        } else {
                            "Voltar ao início"
                        },
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (mostrandoAparencia) {
                        "Aparência"
                    } else {
                        "Mais"
                    },
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
            if (!mostrandoAparencia) {
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
        }
    ) { paddingValues ->

        if (mostrandoAparencia) {

            // TELA APARÊNCIA
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Escolha o visual do aplicativo.",
                    color = cores.onBackground,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    OpcaoAparencia(
                        titulo = "Modo claro",
                        descricao = "Visual claro do aplicativo",
                        icone = Icons.Outlined.LightMode,
                        selecionado =
                            modoAtual == ModoAparencia.CLARO,
                        onClick = {
                            PreferenciasAparencia.alterar(
                                context,
                                ModoAparencia.CLARO
                            )
                        }
                    )

                    OpcaoAparencia(
                        titulo = "Modo escuro",
                        descricao = "Fundo escuro e textos claros",
                        icone = Icons.Outlined.DarkMode,
                        selecionado =
                            modoAtual == ModoAparencia.ESCURO,
                        onClick = {
                            PreferenciasAparencia.alterar(
                                context,
                                ModoAparencia.ESCURO
                            )
                        }
                    )

                    OpcaoAparencia(
                        titulo = "Automático",
                        descricao =
                            "Segue a configuração do celular",
                        icone = Icons.Outlined.BrightnessAuto,
                        selecionado =
                            modoAtual == ModoAparencia.AUTOMATICO,
                        onClick = {
                            PreferenciasAparencia.alterar(
                                context,
                                ModoAparencia.AUTOMATICO
                            )
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Você pode alterar essa opção quando quiser.",
                    color = cores.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

        } else {

            // TELA MAIS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // PERFIL
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
                        verticalAlignment =
                            Alignment.CenterVertically
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
                            imageVector =
                                Icons.Outlined.ChevronRight,
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

                OpcaoMais(
                    titulo = "Materiais",
                    icone = Icons.Outlined.Inventory2,
                    onClick = onMateriaisClick
                )

                OpcaoMais(
                    titulo = "Aparência",
                    subtitulo = when (modoAtual) {
                        ModoAparencia.CLARO -> "Modo claro"
                        ModoAparencia.ESCURO -> "Modo escuro"
                        ModoAparencia.AUTOMATICO -> "Automático"
                    },
                    icone = Icons.Outlined.Palette,
                    onClick = {
                        mostrandoAparencia = true
                    }
                )

                OpcaoMais(
                    titulo = "Central de atendimento",
                    icone = Icons.Outlined.Message,
                    onClick = {
                        mostrarAtendimento = true
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

                OutlinedButton(
                    onClick = {
                        confirmarSaida = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        cores.outline
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
    }

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
private fun OpcaoAparencia(
    titulo: String,
    descricao: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cores.surface,
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) {
                cores.primary
            } else {
                cores.outline
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selecionado,
                    onClick = onClick,
                    role = Role.RadioButton
                )
                .heightIn(min = 76.dp)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = cores.secondary,
                modifier = Modifier.size(25.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = cores.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = descricao,
                    color = cores.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            RadioButton(
                selected = selecionado,
                onClick = null
            )
        }
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
