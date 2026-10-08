
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi

private val FundoClientes = Color(0xFFFAF6F0)
private val AzulClientes = Color(0xFF193447)
private val CobreClientes = Color(0xFFBD906E)
private val CinzaClientes = Color(0xFF929292)
private val BordaClientes = Color(0xFFE9E3DD)
private val VerdeClientes = Color(0xFF27845D)

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

    BackHandler(onBack = onInicioClick)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoClientes,

        // BOTÃO FIXO DE ADICIONAR CLIENTE
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoClienteClick,
                containerColor = AzulClientes,
                contentColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAddAlt1,
                    contentDescription = "Adicionar cliente"
                )
            }
        },

        // MENU INFERIOR
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.height(75.dp)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onInicioClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Início"
                        )
                    },
                    label = {
                        Text("Início", fontSize = 10.sp)
                    },
                    colors = coresMenuClientes()
                )

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.PeopleOutline,
                            contentDescription = "Clientes"
                        )
                    },
                    label = {
                        Text("Clientes", fontSize = 10.sp)
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
                            contentDescription = "Serviços"
                        )
                    },
                    label = {
                        Text("Serviços", fontSize = 10.sp)
                    },
                    colors = coresMenuClientes()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onMaisClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Mais"
                        )
                    },
                    label = {
                        Text("Mais", fontSize = 10.sp)
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
                    onClick = onInicioClick
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = AzulClientes
                    )
                }

                Column {
                    Text(
                        text = "Clientes",
                        color = AzulClientes,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Gerencie seus clientes e contatos.",
                        color = CinzaClientes,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            // CAMPO DE PESQUISA
            OutlinedTextField(
                value = busca,
                onValueChange = {
                    busca = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = {
                    Text(
                        text = "Buscar por nome ou telefone",
                        color = CinzaClientes,
                        fontSize = 13.sp
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = CobreClientes,
                    unfocusedBorderColor = BordaClientes
                )
            )

            Spacer(Modifier.height(12.dp))

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
                    listOf(
                        "Todos" to clientes.size,
                        "Ativos" to clientes.count {
                            it.ativo
                        },
                        "Inativos" to clientes.count {
                            !it.ativo
                        }
                    )
                ) { (nome, quantidade) ->

                    FilterChip(
                        selected = filtro == nome,
                        onClick = {
                            filtro = nome
                        },
                        label = {
                            Text(
                                text = "$nome ($quantidade)",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            selectedContainerColor =
                                Color(0xFFF4E6D8),
                            selectedLabelColor = AzulClientes,
                            labelColor = CinzaClientes
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtro == nome,
                            borderColor = BordaClientes,
                            selectedBorderColor = CobreClientes
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // LISTA DE CLIENTES
            if (clientesFiltrados.isEmpty()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum cliente encontrado.",
                        color = CinzaClientes,
                        fontSize = 13.sp
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

                        // CONTA APENAS SERVIÇOS EM ABERTO
                        val quantidadeServicos = servicos.count {
                            it.clienteId == cliente.id &&
                                    it.status != "Concluído" &&
                                    it.status != "Cancelado"
                        }

                        LinhaCliente(
                            cliente = cliente,
                            quantidadeServicos = quantidadeServicos,
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

// LINHA DE CLIENTE MAIS COMPACTA
@Composable
private fun LinhaCliente(
    cliente: ClienteUi,
    quantidadeServicos: Int,
    onClick: () -> Unit
) {
    val iniciais = cliente.nome
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "C" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // AVATAR COM INICIAIS
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(
                    color = Color(0xFFB98C69),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            // NOME E STATUS NA MESMA LINHA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                Text(
                    text = cliente.nome,
                    color = AzulClientes,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(
                        1f,
                        fill = false
                    )
                )

                Surface(
                    color = if (cliente.ativo) {
                        Color(0xFFE5F4EC)
                    } else {
                        Color(0xFFF1EEEE)
                    },
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(
                        text = if (cliente.ativo) {
                            "Ativo"
                        } else {
                            "Inativo"
                        },
                        color = if (cliente.ativo) {
                            VerdeClientes
                        } else {
                            CinzaClientes
                        },
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 3.dp
                        )
                    )
                }
            }

            // TELEFONE
            Text(
                text = cliente.telefone,
                color = CinzaClientes,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // QUANTIDADE DE SERVIÇOS
            if (quantidadeServicos > 0) {
                Text(
                    text = if (quantidadeServicos == 1) {
                        "1 serviço em aberto"
                    } else {
                        "$quantidadeServicos serviços em aberto"
                    },
                    color = CobreClientes,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(7.dp))

        // SETA DE DETALHES
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Abrir detalhes do cliente",
            tint = CinzaClientes,
            modifier = Modifier.size(18.dp)
        )
    }
}

// CORES DA NAVEGAÇÃO INFERIOR
@Composable
private fun coresMenuClientes() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = AzulClientes,
        selectedTextColor = AzulClientes,
        indicatorColor = Color(0xFFF1E8DE),
        unselectedIconColor = CinzaClientes,
        unselectedTextColor = CinzaClientes
    )
