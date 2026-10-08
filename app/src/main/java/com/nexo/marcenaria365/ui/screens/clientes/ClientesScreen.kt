
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
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

@Composable
fun ClientesScreen(
    clientes: List<ClienteUi>,
    onInicioClick: () -> Unit,
    onNovoClienteClick: () -> Unit,
    onClienteClick: (ClienteUi) -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var mostrarAviso by remember { mutableStateOf(false) }

    BackHandler(onBack = onInicioClick)

    val filtrados = clientes.filter { cliente ->
        val situacaoValida = when (filtro) {
            "Ativos" -> cliente.ativo
            "Inativos" -> !cliente.ativo
            else -> true
        }

        val termo = busca.trim()

        situacaoValida && (
                cliente.nome.contains(termo, ignoreCase = true) ||
                        cliente.telefone.contains(termo, ignoreCase = true)
                )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = ClienteCores.Fundo,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoClienteClick,
                containerColor = ClienteCores.Azul,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAddAlt1,
                    contentDescription = "Adicionar cliente"
                )
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onInicioClick,
                    icon = {
                        Icon(Icons.Outlined.Home, "Início")
                    },
                    label = {
                        Text("Início", fontSize = 10.sp)
                    },
                    colors = coresNavegacaoClientes()
                )

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            Icons.Outlined.PeopleOutline,
                            "Clientes"
                        )
                    },
                    label = {
                        Text("Clientes", fontSize = 10.sp)
                    },
                    colors = coresNavegacaoClientes()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { mostrarAviso = true },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Assignment,
                            "Serviços"
                        )
                    },
                    label = {
                        Text("Serviços", fontSize = 10.sp)
                    },
                    colors = coresNavegacaoClientes()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { mostrarAviso = true },
                    icon = {
                        Icon(Icons.Outlined.Menu, "Mais")
                    },
                    label = {
                        Text("Mais", fontSize = 10.sp)
                    },
                    colors = coresNavegacaoClientes()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            ClienteCabecalho(
                titulo = "Clientes",
                subtitulo = "Gerencie seus clientes e contatos.",
                onVoltar = onInicioClick
            )

            Spacer(Modifier.height(22.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        "Buscar por nome ou telefone",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = ClienteCores.Cinza
                    )
                },
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = ClienteCores.Cobre,
                    unfocusedBorderColor = ClienteCores.Borda
                )
            )

            Spacer(Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Todos" to clientes.size,
                    "Ativos" to clientes.count { it.ativo },
                    "Inativos" to clientes.count { !it.ativo }
                ).forEach { (nome, total) ->
                    FilterChip(
                        selected = filtro == nome,
                        onClick = { filtro = nome },
                        label = {
                            Text(
                                "$nome ($total)",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor =
                                Color(0xFFF4E6D8),
                            selectedLabelColor = ClienteCores.Azul
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtro == nome,
                            borderColor = ClienteCores.Borda,
                            selectedBorderColor = ClienteCores.Cobre
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (filtrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum cliente encontrado.",
                        color = ClienteCores.Cinza,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(
                        items = filtrados,
                        key = { it.id }
                    ) { cliente ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClienteClick(cliente)
                                }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ClienteAvatar(
                                nome = cliente.nome,
                                tamanho = 48.dp
                            )

                            Spacer(Modifier.width(14.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = cliente.nome,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ClienteCores.Azul,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = cliente.telefone,
                                    fontSize = 12.sp,
                                    color = ClienteCores.Cinza
                                )
                            }

                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = "Ver detalhes",
                                tint = ClienteCores.Cinza
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.7.dp,
                            color = ClienteCores.Borda
                        )
                    }
                }
            }
        }
    }

    if (mostrarAviso) {
        AlertDialog(
            onDismissRequest = { mostrarAviso = false },
            title = { Text("Em desenvolvimento") },
            text = {
                Text("Essa área será adicionada em breve.")
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarAviso = false }
                ) {
                    Text(
                        "Entendi",
                        color = ClienteCores.Azul
                    )
                }
            }
        )
    }
}

@Composable
private fun coresNavegacaoClientes() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = ClienteCores.Azul,
        selectedTextColor = ClienteCores.Azul,
        indicatorColor = ClienteCores.Fundo,
        unselectedIconColor = ClienteCores.Cinza,
        unselectedTextColor = ClienteCores.Cinza
    )
