
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoServicos = Color(0xFFFAF6F0)
private val AzulServicos = Color(0xFF193447)
private val CobreServicos = Color(0xFFBD906E)
private val CinzaServicos = Color(0xFF929292)
private val BordaServicos = Color(0xFFE9E3DD)
private val VerdeServicos = Color(0xFF27845D)

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

    BackHandler(onBack = onInicioClick)

    val servicosFiltrados = servicos.filter { servico ->
        val correspondeFiltro = when (filtro) {
            "Em andamento" -> servico.status == "Em produção"
            "Orçamentos" -> servico.status == "Orçamento"
            "Concluídos" -> servico.status == "Concluído"
            else -> true
        }

        val termo = pesquisa.trim()

        correspondeFiltro && (
                servico.titulo.contains(
                    termo,
                    ignoreCase = true
                ) ||
                        servico.clienteNome.contains(
                            termo,
                            ignoreCase = true
                        )
                )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoServicos,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onNovoServicoClick,
                containerColor = AzulServicos,
                contentColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Adicionar serviço"
                )
            }
        },

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
                    colors = coresMenuServicos()
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onClientesClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.PeopleOutline,
                            contentDescription = "Clientes"
                        )
                    },
                    label = {
                        Text("Clientes", fontSize = 10.sp)
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
                            contentDescription = "Serviços"
                        )
                    },
                    label = {
                        Text("Serviços", fontSize = 10.sp)
                    },
                    colors = coresMenuServicos()
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

            // CABEÇALHO NO MESMO PADRÃO DE CLIENTES
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
                IconButton(onClick = onInicioClick) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar ao início",
                        tint = AzulServicos
                    )
                }

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
                        fontSize = 12.sp
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = {
                    Text(
                        text = "Buscar por serviço ou cliente",
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = CobreServicos,
                    unfocusedBorderColor = BordaServicos
                )
            )

            Spacer(Modifier.height(12.dp))

            // FILTROS
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
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 20.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { (nome, quantidade) ->
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
                            selectedLabelColor = AzulServicos,
                            labelColor = CinzaServicos
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtro == nome,
                            borderColor = BordaServicos,
                            selectedBorderColor = CobreServicos
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // LISTA DE SERVIÇOS
            if (servicosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum serviço encontrado.",
                        color = CinzaServicos,
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

// LINHA COMPACTA, SEM PERDER A FOTO
@Composable
private fun LinhaServico(
    servico: ServicoUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // FOTO CADASTRADA OU ÍCONE PADRÃO
        FotoServico(
            fotoUri = servico.fotoUri,
            modifier = Modifier.size(52.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = servico.titulo,
                color = AzulServicos,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${servico.clienteNome} · ${servico.entrega}",
                color = CinzaServicos,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // SELO DISCRETO DE STATUS
            Surface(
                modifier = Modifier.align(Alignment.Start),
                color = when (servico.status) {
                    "Concluído" -> Color(0xFFE5F4EC)
                    "Em produção" -> Color(0xFFF6E8D8)
                    "Orçamento" -> Color(0xFFFFF3E2)
                    else -> Color(0xFFF1EEEE)
                },
                shape = RoundedCornerShape(5.dp)
            ) {
                Text(
                    text = servico.status,
                    color = when (servico.status) {
                        "Concluído" -> VerdeServicos
                        "Em produção" -> CobreServicos
                        "Orçamento" -> Color(0xFFAF782C)
                        else -> CinzaServicos
                    },
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(
                        horizontal = 7.dp,
                        vertical = 3.dp
                    )
                )
            }
        }

        Spacer(Modifier.width(7.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Abrir detalhes do serviço",
            tint = CinzaServicos,
            modifier = Modifier.size(18.dp)
        )
    }
}

// CORES DO MENU INFERIOR
@Composable
private fun coresMenuServicos() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = AzulServicos,
        selectedTextColor = AzulServicos,
        indicatorColor = Color(0xFFF1E8DE),
        unselectedIconColor = CinzaServicos,
        unselectedTextColor = CinzaServicos
    )
