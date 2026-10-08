
package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoClientes = Color(0xFFFAF6F0)
private val AzulClientes = Color(0xFF193447)
private val CobreClientes = Color(0xFFB98C69)
private val CinzaClientes = Color(0xFF929292)

data class ClienteUi(
    val id: Long,
    val nome: String,
    val telefone: String,
    val email: String = "",
    val ativo: Boolean = true
)

fun clientesIniciais(): List<ClienteUi> = listOf(
    ClienteUi(1, "Ana Silva", "(11) 91234-5678"),
    ClienteUi(2, "Bruno Souza", "(11) 98765-4321"),
    ClienteUi(3, "Carlos Fernandes", "(11) 99876-5432"),
    ClienteUi(4, "Débora Martins", "(11) 97654-3210"),
    ClienteUi(5, "Juliana Almeida", "(11) 96543-2109"),
    ClienteUi(6, "Ricardo Lima", "(11) 95432-1098")
)

@Composable
fun ClientesScreen(
    clientes: List<ClienteUi>,
    onAdicionarCliente: (ClienteUi) -> Unit,
    onAtualizarCliente: (ClienteUi) -> Unit,
    onInicioClick: () -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var tela by rememberSaveable { mutableStateOf("lista") }
    var clienteSelecionadoId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var mostrarAviso by remember { mutableStateOf(false) }

    val clienteSelecionado = clientes.find {
        it.id == clienteSelecionadoId
    }

    BackHandler(enabled = tela != "lista") {
        tela = "lista"
    }

    when (tela) {
        "cadastro" -> FormularioCliente(
            cliente = null,
            onVoltar = { tela = "lista" },
            onSalvar = { cliente ->
                onAdicionarCliente(cliente)
                tela = "lista"
            }
        )

        "edicao" -> FormularioCliente(
            cliente = clienteSelecionado,
            onVoltar = { tela = "detalhe" },
            onSalvar = { cliente ->
                onAtualizarCliente(cliente)
                tela = "detalhe"
            }
        )

        "detalhe" -> {
            if (clienteSelecionado != null) {
                DetalheCliente(
                    cliente = clienteSelecionado,
                    onVoltar = { tela = "lista" },
                    onEditar = { tela = "edicao" }
                )
            } else {
                LaunchedEffect(Unit) { tela = "lista" }
            }
        }

        else -> {
            val filtrados = clientes.filter { cliente ->
                val correspondeFiltro = when (filtro) {
                    "Ativos" -> cliente.ativo
                    "Inativos" -> !cliente.ativo
                    else -> true
                }
                val termo = busca.trim()
                correspondeFiltro &&
                        (cliente.nome.contains(termo, ignoreCase = true) ||
                                cliente.telefone.contains(termo, ignoreCase = true))
            }

            Scaffold(
                containerColor = FundoClientes,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { tela = "cadastro" },
                        containerColor = AzulClientes,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAddAlt1,
                            contentDescription = "Adicionar cliente",
                            modifier = Modifier.size(25.dp)
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
                            label = { Text("Início", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = true,
                            onClick = {},
                            icon = {
                                Icon(Icons.Outlined.PeopleOutline, "Clientes")
                            },
                            label = { Text("Clientes", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AzulClientes,
                                selectedTextColor = AzulClientes,
                                indicatorColor = FundoClientes
                            )
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
                            label = { Text("Serviços", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { mostrarAviso = true },
                            icon = { Icon(Icons.Outlined.Menu, "Mais") },
                            label = { Text("Mais", fontSize = 10.sp) }
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 18.dp)
                ) {
                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Clientes",
                        color = AzulClientes,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Gerencie seus clientes e contatos.",
                        color = CinzaClientes,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Buscar por nome ou telefone",
                                fontSize = 13.sp,
                                color = CinzaClientes
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = CinzaClientes
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = CobreClientes,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(15.dp))

                    val filtros = listOf(
                        "Todos" to clientes.size,
                        "Ativos" to clientes.count { it.ativo },
                        "Inativos" to clientes.count { !it.ativo }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filtros.forEach { (nome, quantidade) ->
                            val selecionado = filtro == nome

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selecionado)
                                            Color(0xFFF4E6D8)
                                        else Color.Transparent
                                    )
                                    .clickable { filtro = nome }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$nome ($quantidade)",
                                    fontSize = 11.sp,
                                    fontWeight = if (selecionado)
                                        FontWeight.SemiBold
                                    else FontWeight.Normal,
                                    color = AzulClientes,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(15.dp))

                    if (filtrados.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 55.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum cliente encontrado.",
                                fontSize = 13.sp,
                                color = CinzaClientes
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(
                                items = filtrados,
                                key = { it.id }
                            ) { cliente ->
                                LinhaCliente(
                                    cliente = cliente,
                                    onClick = {
                                        clienteSelecionadoId = cliente.id
                                        tela = "detalhe"
                                    }
                                )
                                HorizontalDivider(
                                    color = Color(0xFFE9E3DD),
                                    thickness = 0.7.dp
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
                        Text("Essa área será desenvolvida nas próximas etapas.")
                    },
                    confirmButton = {
                        TextButton(onClick = { mostrarAviso = false }) {
                            Text("Entendi", color = AzulClientes)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LinhaCliente(
    cliente: ClienteUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(49.dp)
                .clip(CircleShape)
                .background(CobreClientes),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cliente.nome.trim()
                    .split(Regex("\\s+"))
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .joinToString(""),
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cliente.nome,
                color = AzulClientes,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = cliente.telefone,
                color = CinzaClientes,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Ver cliente",
            tint = CinzaClientes
        )
    }
}

@Composable
private fun FormularioCliente(
    cliente: ClienteUi?,
    onVoltar: () -> Unit,
    onSalvar: (ClienteUi) -> Unit
) {
    var nome by rememberSaveable(cliente?.id) {
        mutableStateOf(cliente?.nome ?: "")
    }
    var telefone by rememberSaveable(cliente?.id) {
        mutableStateOf(cliente?.telefone ?: "")
    }
    var email by rememberSaveable(cliente?.id) {
        mutableStateOf(cliente?.email ?: "")
    }
    var mensagem by remember { mutableStateOf("") }

    val cores = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreClientes,
        unfocusedBorderColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoClientes)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        IconButton(onClick = onVoltar) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = AzulClientes
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (cliente == null) "Novo cliente" else "Editar cliente",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = AzulClientes
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Preencha as informações de contato.",
            fontSize = 13.sp,
            color = CinzaClientes
        )

        Spacer(Modifier.height(25.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it; mensagem = "" },
            label = { Text("Nome completo *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(11.dp),
            colors = cores
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = telefone,
            onValueChange = { telefone = it; mensagem = "" },
            label = { Text("Telefone *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(11.dp),
            colors = cores
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; mensagem = "" },
            label = { Text("E-mail (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(11.dp),
            colors = cores
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                val telefoneNumeros = telefone.filter { it.isDigit() }
                mensagem = when {
                    nome.isBlank() -> "Informe o nome do cliente."
                    telefoneNumeros.length !in 10..11 ->
                        "Informe um telefone com DDD válido."
                    email.isNotBlank() &&
                            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                        "Informe um e-mail válido."
                    else -> {
                        onSalvar(
                            ClienteUi(
                                id = cliente?.id ?: System.currentTimeMillis(),
                                nome = nome.trim(),
                                telefone = telefone.trim(),
                                email = email.trim(),
                                ativo = cliente?.ativo ?: true
                            )
                        )
                        ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulClientes
            )
        ) {
            Text(
                "Salvar cliente",
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (mensagem.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                mensagem,
                color = Color(0xFFB04444),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DetalheCliente(
    cliente: ClienteUi,
    onVoltar: () -> Unit,
    onEditar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoClientes)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        IconButton(onClick = onVoltar) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = AzulClientes
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = cliente.nome,
            color = AzulClientes,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Telefone", color = CinzaClientes, fontSize = 12.sp)
                Text(cliente.telefone, color = AzulClientes)
                HorizontalDivider()
                Text("E-mail", color = CinzaClientes, fontSize = 12.sp)
                Text(
                    cliente.email.ifBlank { "Não informado" },
                    color = AzulClientes
                )
                HorizontalDivider()
                Text("Situação", color = CinzaClientes, fontSize = 12.sp)
                Text(
                    if (cliente.ativo) "Ativo" else "Inativo",
                    color = AzulClientes
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onEditar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulClientes
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Editar cliente")
        }
    }
}
