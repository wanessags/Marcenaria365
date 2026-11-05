
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoServicos = Color(0xFFFAF6F0)
private val AzulServicos = Color(0xFF193447)
private val CobreServicos = Color(0xFF94613C)
private val CinzaServicos = Color(0xFF53616A)
private val BordaServicos = Color(0xFFB5B5B0)
private val VerdeServicos = Color(0xFF237A58)
private val BrancoServicos = Color.White

@Composable
fun ServicosScreen(
    servicos: List<ServicoUi>,
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit,
    onMaisClick: () -> Unit,
    onNovoServicoClick: () -> Unit,
    onServicoClick: (ServicoUi) -> Unit
) {
    var pesquisa by rememberSaveable {
        mutableStateOf("")
    }

    var filtro by rememberSaveable {
        mutableStateOf("Todos")
    }

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    BackHandler(onBack = onInicioClick)

    val servicosFiltrados = servicos.filter { servico ->
        val correspondeFiltro = when (filtro) {
            "Em andamento" ->
                servico.status == "Em produção"

            "Orçamentos" ->
                servico.status == "Orçamento"

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

    val filtros = listOf(
        "Todos" to servicos.size,
        "Em andamento" to servicos.count {
            it.status == "Em produção"
        },
        "Orçamentos" to servicos.count {
            it.status == "Orçamento"
        },
        "Concluídos" to servicos.count {
            it.status == "Concluído"
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoServicos,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoServicoClick,
                modifier = Modifier.sizeIn(
                    minWidth = 56.dp,
                    minHeight = 56.dp
                ),
                containerColor = AzulServicos,
                contentColor = BrancoServicos,
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
                containerColor = BrancoServicos,
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
                        Text("Início", fontSize = 12.sp)
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
                        Text("Clientes", fontSize = 12.sp)
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
                        Text("Serviços", fontSize = 12.sp)
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
                        Text("Mais", fontSize = 12.sp)
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
                        tint = AzulServicos
                    )
                }

                Spacer(Modifier.width(3.dp))

                Column {
                    Text(
                        text = "Serviços",
                        color = AzulServicos,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Acompanhe seus projetos e entregas.",
                        color = CinzaServicos,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

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
                        color = CinzaServicos,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = CinzaServicos
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
                    focusedContainerColor = BrancoServicos,
                    unfocusedContainerColor = BrancoServicos,
                    focusedBorderColor = CobreServicos,
                    unfocusedBorderColor = BordaServicos,
                    focusedLabelColor = AzulServicos,
                    unfocusedLabelColor = CinzaServicos,
                    focusedTextColor = AzulServicos,
                    unfocusedTextColor = AzulServicos,
                    cursorColor = AzulServicos
                )
            )

            Spacer(Modifier.height(15.dp))

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
                                        color = AzulServicos,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = BrancoServicos,
                                selectedContainerColor =
                                    Color(0xFFF1E4D7),
                                selectedLabelColor = AzulServicos,
                                labelColor = AzulServicos
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filtro == nome,
                                borderColor = BordaServicos,
                                selectedBorderColor = CobreServicos
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (servicosFiltrados.size == 1) {
                    "1 serviço encontrado"
                } else {
                    "${servicosFiltrados.size} serviços encontrados"
                },
                color = CinzaServicos,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )

            Spacer(Modifier.height(12.dp))

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
                        color = CinzaServicos,
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
                            color = BordaServicos,
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
    var focado by remember {
        mutableStateOf(false)
    }

    val descricaoAcessivel =
        "Abrir serviço ${servico.titulo}. " +
                "Cliente: ${servico.clienteNome}. " +
                "Entrega: ${servico.entrega}. " +
                "Situação: ${servico.status}."

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
                        color = AzulServicos,
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
                color = AzulServicos,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = servico.clienteNome,
                color = CinzaServicos,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Entrega: ${servico.entrega}",
                color = CinzaServicos,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                modifier = Modifier.align(Alignment.Start),
                color = when (servico.status) {
                    "Concluído" -> Color(0xFFE5F4EC)
                    "Em produção" -> Color(0xFFF6E8D8)
                    "Orçamento" -> Color(0xFFFFF3E2)
                    else -> Color(0xFFF1EEEE)
                },
                shape = RoundedCornerShape(7.dp)
            ) {
                Text(
                    text = servico.status,
                    color = when (servico.status) {
                        "Concluído" -> VerdeServicos
                        "Em produção" -> CobreServicos
                        "Orçamento" -> Color(0xFF825B12)
                        else -> AzulServicos
                    },
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
            tint = CinzaServicos,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun coresMenuServicos() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = AzulServicos,
        selectedTextColor = AzulServicos,
        indicatorColor = Color(0xFFF1E8DE),
        unselectedIconColor = CinzaServicos,
        unselectedTextColor = CinzaServicos
    )
