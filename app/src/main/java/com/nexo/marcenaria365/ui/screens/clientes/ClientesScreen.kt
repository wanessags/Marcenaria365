
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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

private val FundoClientes = Color(0xFFFAF6F0)
private val AzulClientes = Color(0xFF193447)
private val CobreClientes = Color(0xFF94613C)
private val CinzaClientes = Color(0xFF53616A)
private val BordaClientes = Color(0xFF89969E)
private val VerdeClientes = Color(0xFF237A58)

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
    var busca by rememberSaveable {
        mutableStateOf("")
    }

    var filtro by rememberSaveable {
        mutableStateOf("Todos")
    }

    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current

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
        containerColor = FundoClientes,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoClienteClick,
                modifier = Modifier.sizeIn(
                    minWidth = 56.dp,
                    minHeight = 56.dp
                ),
                containerColor = AzulClientes,
                contentColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.PersonAddAlt1,
                    contentDescription = "Adicionar cliente"
                )
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
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
                        Text("Clientes", fontSize = 12.sp)
                    },
                    colors = coresMenuClientes()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onServicosClick,
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
                        Text("Mais", fontSize = 12.sp)
                    },
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
                        tint = AzulClientes
                    )
                }

                Spacer(Modifier.width(5.dp))

                Column {
                    Text(
                        text = "Clientes",
                        color = AzulClientes,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Gerencie seus clientes e contatos.",
                        color = CinzaClientes,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            // PESQUISA
            OutlinedTextField(
                value = busca,
                onValueChange = {
                    busca = it
                },
                label = {
                    Text("Buscar clientes")
                },
                placeholder = {
                    Text(
                        "Nome ou telefone",
                        color = CinzaClientes
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = CinzaClientes
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
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = CobreClientes,
                    unfocusedBorderColor = BordaClientes,
                    focusedTextColor = AzulClientes,
                    unfocusedTextColor = AzulClientes,
                    focusedLabelColor = AzulClientes,
                    unfocusedLabelColor = CinzaClientes,
                    cursorColor = AzulClientes
                )
            )

            Spacer(Modifier.height(14.dp))

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
                                fontSize = 13.sp,
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
                                        2.dp,
                                        AzulClientes,
                                        RoundedCornerShape(16.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor =
                                    Color(0xFFF1E4D7),
                                selectedLabelColor = AzulClientes,
                                labelColor = AzulClientes
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filtro == nome,
                                borderColor = BordaClientes,
                                selectedBorderColor = CobreClientes
                            )
                    )
                }
            }

            Spacer(Modifier.height(11.dp))

            // QUANTIDADE DE RESULTADOS
            Text(
                text = if (clientesFiltrados.size == 1) {
                    "1 cliente encontrado"
                } else {
                    "${clientesFiltrados.size} clientes encontrados"
                },
                color = CinzaClientes,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )

            Spacer(Modifier.height(10.dp))

            // LISTA DE CLIENTES
            if (clientesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (busca.isNotBlank()) {
                            "Nenhum cliente encontrado para sua pesquisa."
                        } else if (filtro != "Todos") {
                            "Nenhum cliente neste filtro."
                        } else {
                            "Nenhum cliente cadastrado."
                        },
                        color = CinzaClientes,
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
                        items = clientesFiltrados,
                        key = { it.id }
                    ) { cliente ->

                        // Mantém a contagem de serviços em aberto.
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
                            color = BordaClientes,
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaCliente(
    cliente: ClienteUi,
    quantidadeServicos: Int,
    onClick: () -> Unit
) {
    var focado by remember {
        mutableStateOf(false)
    }

    val iniciais = cliente.nome
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }
        .ifBlank { "C" }

    val situacao = if (cliente.ativo) {
        "Ativo"
    } else {
        "Inativo"
    }

    val textoServicos = when (quantidadeServicos) {
        0 -> "Nenhum serviço em aberto"
        1 -> "1 serviço em aberto"
        else -> "$quantidadeServicos serviços em aberto"
    }

    val descricaoAcessivel =
        "Abrir detalhes de ${cliente.nome}. " +
                "Telefone: ${cliente.telefone}. " +
                "Situação: $situacao. $textoServicos."

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
                        color = AzulClientes,
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
        // AVATAR COM INICIAIS
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = CobreClientes,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            // NOME
            Text(
                text = cliente.nome,
                color = AzulClientes,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // TELEFONE
            Text(
                text = cliente.telefone,
                color = CinzaClientes,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // SITUAÇÃO
            Surface(
                modifier = Modifier.align(
                    Alignment.Start
                ),
                color = if (cliente.ativo) {
                    Color(0xFFE5F4EC)
                } else {
                    Color(0xFFF1EEEE)
                },
                shape = RoundedCornerShape(7.dp)
            ) {
                Text(
                    text = situacao,
                    color = if (cliente.ativo) {
                        VerdeClientes
                    } else {
                        AzulClientes
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    )
                )
            }

            // SERVIÇOS EM ABERTO
            Text(
                text = textoServicos,
                color = CobreClientes,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = CinzaClientes,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun coresMenuClientes() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = AzulClientes,
        selectedTextColor = AzulClientes,
        indicatorColor = Color(0xFFF1E8DE),
        unselectedIconColor = CinzaClientes,
        unselectedTextColor = CinzaClientes
    )
