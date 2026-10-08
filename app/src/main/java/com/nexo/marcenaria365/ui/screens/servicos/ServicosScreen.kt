
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Chair
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Borda = Color(0xFFE9E3DD)

@Composable
fun ServicosScreen(
    servicos: List<ServicoUi>,
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit,
    onNovoServicoClick: () -> Unit,
    onServicoClick: (ServicoUi) -> Unit
) {
    var pesquisa by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var aviso by remember { mutableStateOf(false) }

    BackHandler(onBack = onInicioClick)

    val filtrados = servicos.filter { servico ->
        val corresponde = when (filtro) {
            "Em andamento" -> servico.status == "Em produção"
            "Orçamentos" -> servico.status == "Orçamento"
            "Concluídos" -> servico.status == "Concluído"
            else -> true
        }

        corresponde && (
                servico.titulo.contains(pesquisa, true) ||
                        servico.clienteNome.contains(pesquisa, true)
                )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Fundo,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoServicoClick,
                containerColor = Azul,
                contentColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Novo serviço"
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
                    icon = { Icon(Icons.Outlined.Home, "Início") },
                    label = { Text("Início", fontSize = 10.sp) },
                    colors = coresNavegacao()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onClientesClick,
                    icon = {
                        Icon(Icons.Outlined.PeopleOutline, "Clientes")
                    },
                    label = { Text("Clientes", fontSize = 10.sp) },
                    colors = coresNavegacao()
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Assignment,
                            "Serviços"
                        )
                    },
                    label = { Text("Serviços", fontSize = 10.sp) },
                    colors = coresNavegacao()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { aviso = true },
                    icon = { Icon(Icons.Outlined.Menu, "Mais") },
                    label = { Text("Mais", fontSize = 10.sp) },
                    colors = coresNavegacao()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(Modifier.height(15.dp))

            Text(
                text = "Serviços",
                color = Azul,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(15.dp))

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = {
                    Text(
                        "Buscar serviço...",
                        fontSize = 13.sp,
                        color = Cinza
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Cinza
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0ECE7),
                    unfocusedContainerColor = Color(0xFFF0ECE7),
                    focusedBorderColor = Cobre,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(14.dp))

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

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { (nome, quantidade) ->
                    FilterChip(
                        selected = filtro == nome,
                        onClick = { filtro = nome },
                        label = {
                            Text(
                                "$nome ($quantidade)",
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF6E8D8),
                            selectedLabelColor = Azul
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtro == nome,
                            borderColor = Borda,
                            selectedBorderColor = Cobre
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (filtrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum serviço encontrado.",
                        color = Cinza,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 95.dp
                    )
                ) {
                    items(filtrados, key = { it.id }) { servico ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onServicoClick(servico) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(
                                        Color(0xFFEDE4DA),
                                        RoundedCornerShape(9.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Chair,
                                    contentDescription = null,
                                    tint = Cobre,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = servico.titulo,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Azul,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = servico.clienteNome,
                                    fontSize = 12.sp,
                                    color = Cinza
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Entrega: ${servico.entrega}",
                                    fontSize = 11.sp,
                                    color = Cinza
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = servico.status,
                                    fontSize = 11.sp,
                                    color = when (servico.status) {
                                        "Concluído" -> Color(0xFF239B7A)
                                        "Em produção" -> Cobre
                                        else -> Color(0xFFD49A4C)
                                    }
                                )
                            }

                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = "Ver detalhes",
                                tint = Cinza,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        HorizontalDivider(
                            color = Borda,
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        }
    }

    if (aviso) {
        AlertDialog(
            onDismissRequest = { aviso = false },
            title = { Text("Mais") },
            text = { Text("Essa área ainda não está disponível.") },
            confirmButton = {
                TextButton(onClick = { aviso = false }) {
                    Text("Entendi", color = Azul)
                }
            }
        )
    }
}

@Composable
private fun coresNavegacao() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = Azul,
        selectedTextColor = Azul,
        indicatorColor = Fundo,
        unselectedIconColor = Cinza,
        unselectedTextColor = Cinza
    )
