
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi

@Composable
fun ClientesScreen(
    clientes: List<ClienteUi>,
    servicos: List<ServicoUi> = emptyList(),
    onInicioClick: () -> Unit,
    onNovoClienteClick: () -> Unit,
    onClienteClick: (ClienteUi) -> Unit,
    onServicosClick: () -> Unit,
    onMaisClick: () -> Unit
) {
    // As cores acompanham o tema selecionado
    // em Mais > Aparência.
    val cores = MaterialTheme.colorScheme

    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current

    // Estados originais da pesquisa e dos filtros.
    var busca by rememberSaveable {
        mutableStateOf("")
    }

    var filtro by rememberSaveable {
        mutableStateOf("Todos")
    }

    // REGRA ORIGINAL: filtrar por situação,
    // nome ou telefone.
    val clientesFiltrados = clientes.filter { cliente ->
        val correspondeFiltro = when (filtro) {
            "Ativos" -> cliente.ativo
            "Inativos" -> !cliente.ativo
            else -> true
        }

        val termo = busca.trim()

        correspondeFiltro && (
                cliente.nome.contains(
                    termo,
                    ignoreCase = true
                ) ||
                        cliente.telefone.contains(
                            termo,
                            ignoreCase = true
                        )
                )
    }

    // Mesmas categorias da versão funcional.
    val filtros = listOf(
        "Todos" to clientes.size,
        "Ativos" to clientes.count { it.ativo },
        "Inativos" to clientes.count { !it.ativo }
    )

    BackHandler(onBack = onInicioClick)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),

        containerColor = cores.background,

        // BOTÃO ORIGINAL DE CADASTRAR CLIENTE
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoClienteClick,
                modifier = Modifier.sizeIn(
                    minWidth = 56.dp,
                    minHeight = 56.dp
                ),
                containerColor = cores.primary,
                contentColor = cores.onPrimary,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.PersonAddAlt1,
                    contentDescription = "Adicionar cliente",
                    modifier = Modifier.size(25.dp)
                )
            }
        },

        // MENU INFERIOR ORIGINAL
        bottomBar = {
            NavigationBar(
                containerColor = cores.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.heightIn(
                    min = 75.dp
                )
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onInicioClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = "Início",
                            fontSize = 12.sp
                        )
                    },
                    alwaysShowLabel = true,
                    colors = coresMenuClientes()
                )

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.PeopleOutline,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = "Clientes",
                            fontSize = 12.sp
                        )
                    },
                    alwaysShowLabel = true,
                    colors = coresMenuClientes()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onServicosClick,
                    icon = {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored
                                    .Outlined.Assignment,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = "Serviços",
                            fontSize = 12.sp
                        )
                    },
                    alwaysShowLabel = true,
                    colors = coresMenuClientes()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onMaisClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = "Mais",
                            fontSize = 12.sp
                        )
                    },
                    alwaysShowLabel = true,
                    colors = coresMenuClientes()
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // CABEÇALHO ORIGINAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 20.dp,
                        top = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onInicioClick,
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored
                                .Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = cores.onBackground
                    )
                }

                Spacer(Modifier.width(5.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Clientes",
                        color = cores.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Gerencie seus clientes e contatos.",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            // CAMPO DE PESQUISA ORIGINAL
            // Agora com rótulo permanente.
            OutlinedTextField(
                value = busca,
                onValueChange = {
                    busca = it
                },
                label = {
                    Text("Buscar clientes")
                },
                placeholder = {
                    Text("Nome ou telefone")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = cores.onSurfaceVariant
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        teclado?.hide()
                        foco.clearFocus()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cores.surface,
                    unfocusedContainerColor = cores.surface,
                    focusedTextColor = cores.onSurface,
                    unfocusedTextColor = cores.onSurface,
                    focusedBorderColor = cores.primary,
                    unfocusedBorderColor = cores.outline,
                    focusedLabelColor = cores.primary,
                    unfocusedLabelColor = cores.onSurfaceVariant,
                    cursorColor = cores.primary
                )
            )

            Spacer(Modifier.height(12.dp))

            // FILTROS ORIGINAIS
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 20.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filtros,
                    key = { it.first }
                ) { (nome, quantidade) ->

                    FilterChip(
                        selected = filtro == nome,
                        onClick = {
                            filtro = nome

                            teclado?.hide()
                            foco.clearFocus()
                        },
                        label = {
                            Text(
                                text = "$nome ($quantidade)",
                                fontSize = 13.sp,
                                fontWeight = if (filtro == nome) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Medium
                                }
                            )
                        },
                        modifier = Modifier.heightIn(
                            min = 48.dp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = cores.surface,
                                selectedContainerColor =
                                    cores.primaryContainer,
                                selectedLabelColor =
                                    cores.onPrimaryContainer,
                                labelColor = cores.onSurface
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filtro == nome,
                                borderColor = cores.outline,
                                selectedBorderColor =
                                    cores.primary
                            )
                    )
                }
            }

            Spacer(Modifier.height(9.dp))

            // CONTAGEM DOS RESULTADOS:
            // apenas informação visual e para TalkBack.
            Text(
                text = if (clientesFiltrados.size == 1) {
                    "1 cliente encontrado"
                } else {
                    "${clientesFiltrados.size} clientes encontrados"
                },
                fontSize = 12.sp,
                color = cores.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )

            Spacer(Modifier.height(9.dp))

            // LISTA ORIGINAL DE CLIENTES
            if (clientesFiltrados.isEmpty()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum cliente encontrado.",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 85.dp
                    )
                ) {
                    items(
                        items = clientesFiltrados,
                        key = { it.id }
                    ) { cliente ->

                        // REGRA ORIGINAL:
                        // contar serviços em aberto
                        // vinculados ao cliente.
                        val quantidadeServicos = servicos.count {
                            it.clienteId == cliente.id &&
                                    it.status != "Concluído" &&
                                    it.status != "Cancelado"
                        }

                        LinhaCliente(
                            cliente = cliente,
                            quantidadeServicos =
                                quantidadeServicos,
                            onClick = {
                                onClienteClick(cliente)
                            }
                        )

                        HorizontalDivider(
                            color = cores.outline.copy(
                                alpha = 0.55f
                            ),
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        }
    }
}

// LINHA DO CLIENTE
// Mantém nome, telefone, situação, serviços
// e navegação ao tocar.
@Composable
private fun LinhaCliente(
    cliente: ClienteUi,
    quantidadeServicos: Int,
    onClick: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

    // Verifica o tema aplicado pelo aplicativo,
    // e não apenas a configuração do celular.
    val modoEscuro =
        cores.background.luminance() < 0.5f

    val corVerde = if (modoEscuro) {
        Color(0xFF83DDB5)
    } else {
        Color(0xFF237A58)
    }

    val corSituacao = if (cliente.ativo) {
        corVerde
    } else {
        cores.onSurfaceVariant
    }

    val fundoSituacao = if (cliente.ativo) {
        if (modoEscuro) {
            Color(0xFF1D4134)
        } else {
            Color(0xFFE5F4EC)
        }
    } else {
        cores.surfaceVariant
    }

    val situacao = if (cliente.ativo) {
        "Ativo"
    } else {
        "Inativo"
    }

    val iniciais = cliente.nome
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "C" }

    var focado by remember {
        mutableStateOf(false)
    }

    // O TalkBack anuncia todas as informações
    // antes de abrir os detalhes do cliente.
    val descricaoAcessivel = buildString {
        append("Abrir detalhes de ${cliente.nome}. ")
        append("Telefone: ${cliente.telefone}. ")
        append("Situação: $situacao. ")

        if (quantidadeServicos == 1) {
            append("1 serviço em aberto.")
        } else {
            append("$quantidadeServicos serviços em aberto.")
        }
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
                        color = cores.primary,
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
                contentDescription = descricaoAcessivel
            }
            .heightIn(min = 76.dp)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AVATAR ORIGINAL COM INICIAIS
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = cores.secondary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                color = cores.onSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            // NOME E SITUAÇÃO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = cliente.nome,
                    color = cores.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false
                    )
                )

                Surface(
                    color = fundoSituacao,
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(
                        text = situacao,
                        color = corSituacao,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 7.dp,
                            vertical = 5.dp
                        )
                    )
                }
            }

            // TELEFONE
            Text(
                text = cliente.telefone,
                color = cores.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // SERVIÇOS EM ABERTO
            // Mantém a regra original:
            // a informação só aparece se houver serviços.
            if (quantidadeServicos > 0) {
                Text(
                    text = if (quantidadeServicos == 1) {
                        "1 serviço em aberto"
                    } else {
                        "$quantidadeServicos serviços em aberto"
                    },
                    color = cores.secondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(7.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = cores.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// CORES DINÂMICAS DA NAVEGAÇÃO INFERIOR
@Composable
private fun coresMenuClientes(): NavigationBarItemColors {
    val cores = MaterialTheme.colorScheme

    return NavigationBarItemDefaults.colors(
        selectedIconColor = cores.primary,
        selectedTextColor = cores.onSurface,
        indicatorColor = cores.primaryContainer,
        unselectedIconColor = cores.onSurfaceVariant,
        unselectedTextColor = cores.onSurfaceVariant
    )
}
