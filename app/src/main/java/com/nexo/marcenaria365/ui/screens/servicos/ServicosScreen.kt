
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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

@Composable
fun ServicosScreen(
    servicos: List<ServicoUi>,
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit,
    onMaisClick: () -> Unit,
    onNovoServicoClick: () -> Unit,
    onServicoClick: (ServicoUi) -> Unit
) {
    val cores = MaterialTheme.colorScheme

    var pesquisa by rememberSaveable {
        mutableStateOf("")
    }

    var filtro by rememberSaveable {
        mutableStateOf("Todos")
    }

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    BackHandler(onBack = onInicioClick)

    // O status interno continua sendo "Orçamento".
    // Apenas o nome apresentado ao usuário muda.
    val servicosFiltrados = servicos.filter { servico ->

        val correspondeFiltro = when (filtro) {

            "Em andamento" ->
                servico.status == "Em produção"

            "Aguardando" ->
                servico.status == "Orçamento" ||
                        servico.status == "AGUARDANDO"

            "Concluídos" ->
                servico.status == "Concluído"

            else -> true
        }

        val termo = pesquisa.trim()

        val correspondePesquisa =
            termo.isBlank() ||
                    servico.titulo.contains(
                        termo,
                        ignoreCase = true
                    ) ||
                    servico.clienteNome.contains(
                        termo,
                        ignoreCase = true
                    )

        correspondeFiltro && correspondePesquisa
    }

    // CONTADORES COM DADOS REAIS
    val filtros = listOf(
        "Todos" to servicos.size,

        "Em andamento" to servicos.count {
            it.status == "Em produção"
        },

        "Aguardando" to servicos.count {
            it.status == "Orçamento" ||
                    it.status == "AGUARDANDO"
        },

        "Concluídos" to servicos.count {
            it.status == "Concluído"
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),

        containerColor = cores.background,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoServicoClick,
                modifier = Modifier.sizeIn(
                    minWidth = 56.dp,
                    minHeight = 56.dp
                ),
                containerColor = cores.primary,
                contentColor = cores.onPrimary,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Adicionar serviço",
                    modifier = Modifier.size(25.dp)
                )
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = cores.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.heightIn(min = 75.dp)
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
                    colors = coresMenuServicos()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onClientesClick,
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
                    colors = coresMenuServicos()
                )

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = "Serviços",
                            fontSize = 12.sp
                        )
                    },
                    colors = coresMenuServicos()
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
                    colors = coresMenuServicos()
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // CABEÇALHO
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
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = cores.onBackground
                    )
                }

                Spacer(Modifier.width(3.dp))

                Column {
                    Text(
                        text = "Serviços",
                        color = cores.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Acompanhe seus projetos e entregas.",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            // PESQUISA
            OutlinedTextField(
                value = pesquisa,

                onValueChange = {
                    pesquisa = it
                },

                label = {
                    Text(
                        text = "Buscar serviços",
                        fontSize = 13.sp
                    )
                },

                placeholder = {
                    Text(
                        text = "Nome do serviço ou cliente",
                        fontSize = 13.sp
                    )
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
                    focusedBorderColor = cores.secondary,
                    unfocusedBorderColor = cores.outline,
                    focusedLabelColor = cores.onSurface,
                    unfocusedLabelColor = cores.onSurfaceVariant,
                    focusedTextColor = cores.onSurface,
                    unfocusedTextColor = cores.onSurface,
                    focusedPlaceholderColor =
                        cores.onSurfaceVariant,
                    unfocusedPlaceholderColor =
                        cores.onSurfaceVariant,
                    cursorColor = cores.secondary
                )
            )

            Spacer(Modifier.height(15.dp))

            // FILTROS
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

                    var focado by remember {
                        mutableStateOf(false)
                    }

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
                                fontSize = 12.sp,
                                fontWeight =
                                    if (filtro == nome) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    }
                            )
                        },

                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .onFocusChanged {
                                focado = it.isFocused
                            }
                            .then(
                                if (focado) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = cores.primary,
                                        shape = RoundedCornerShape(
                                            16.dp
                                        )
                                    )
                                } else {
                                    Modifier
                                }
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
                                    cores.secondary
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // QUANTIDADE DE RESULTADOS
            Text(
                text = if (servicosFiltrados.size == 1) {
                    "1 serviço encontrado"
                } else {
                    "${servicosFiltrados.size} serviços encontrados"
                },
                color = cores.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )

            Spacer(Modifier.height(12.dp))

            // LISTA
            if (servicosFiltrados.isEmpty()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = if (pesquisa.isNotBlank()) {
                            "Nenhum serviço encontrado para sua pesquisa."
                        } else if (filtro != "Todos") {
                            "Nenhum serviço encontrado neste filtro."
                        } else {
                            "Nenhum serviço cadastrado."
                        },

                        color = cores.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,

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
                        items = servicosFiltrados,
                        key = { it.id }
                    ) { servico ->

                        LinhaServico(
                            servico = servico,
                            onClick = {
                                onServicoClick(servico)
                            }
                        )

                        HorizontalDivider(
                            color = cores.outlineVariant,
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaServico(
    servico: ServicoUi,
    onClick: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

    var focado by remember {
        mutableStateOf(false)
    }

    // Apenas muda o texto apresentado.
    // O status original permanece intacto.
    val situacaoVisivel = rotuloSituacaoServico(
        servico.status
    )

    val descricaoAcessivel =
        "Abrir serviço ${servico.titulo}. " +
                "Cliente: ${servico.clienteNome}. " +
                "Entrega: ${servico.entrega}. " +
                "Situação: $situacaoVisivel."

    val corSituacaoFundo = when (servico.status) {
        "Concluído" ->
            cores.tertiaryContainer

        "Em produção" ->
            cores.primaryContainer

        "Orçamento", "AGUARDANDO" ->
            cores.secondaryContainer

        "Cancelado" ->
            cores.errorContainer

        else ->
            cores.surfaceVariant
    }

    val corSituacaoTexto = when (servico.status) {
        "Concluído" ->
            cores.onTertiaryContainer

        "Em produção" ->
            cores.onPrimaryContainer

        "Orçamento", "AGUARDANDO" ->
            cores.onSecondaryContainer

        "Cancelado" ->
            cores.onErrorContainer

        else ->
            cores.onSurfaceVariant
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
                        shape = RoundedCornerShape(12.dp)
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
            .heightIn(min = 80.dp)
            .padding(
                horizontal = 6.dp,
                vertical = 12.dp
            ),

        verticalAlignment = Alignment.CenterVertically
    ) {

        // FOTO ORIGINAL DO SERVIÇO
        FotoServico(
            fotoUri = servico.fotoUri,
            modifier = Modifier.size(52.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = servico.titulo,
                color = cores.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = servico.clienteNome,
                color = cores.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Entrega: ${servico.entrega}",
                color = cores.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // SITUAÇÃO COM NOVO RÓTULO
            Surface(
                modifier = Modifier.align(
                    Alignment.Start
                ),
                color = corSituacaoFundo,
                shape = RoundedCornerShape(7.dp)
            ) {

                Text(
                    text = situacaoVisivel,
                    color = corSituacaoTexto,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    )
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

@Composable
private fun coresMenuServicos(): NavigationBarItemColors {
    val cores = MaterialTheme.colorScheme

    return NavigationBarItemDefaults.colors(
        selectedIconColor = cores.primary,
        selectedTextColor = cores.onSurface,
        indicatorColor = cores.primaryContainer,
        unselectedIconColor = cores.onSurfaceVariant,
        unselectedTextColor = cores.onSurfaceVariant
    )
}
