package com.nexo.marcenaria365.ui.screens.materiais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Marrom = Color(0xFFB88966)

private data class MaterialExemplo(
    val nome: String,
    val categoria: String,
    val descricao: String
)

@Composable
fun MateriaisScreen(
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    var pesquisa by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Todos") }
    var mostrarAviso by remember { mutableStateOf(false) }

    val categorias = listOf("Todos", "Madeiras", "Ferragens", "Acabamentos")

    val materiais = remember {
        listOf(
            MaterialExemplo("MDF Branco", "Madeiras", "Chapa para móveis planejados"),
            MaterialExemplo("MDF Amadeirado", "Madeiras", "Acabamento com textura de madeira"),
            MaterialExemplo("Compensado", "Madeiras", "Chapa de madeira laminada"),
            MaterialExemplo("Dobradiça", "Ferragens", "Ferragem para portas de armários"),
            MaterialExemplo("Corrediça telescópica", "Ferragens", "Sistema para gavetas"),
            MaterialExemplo("Puxador", "Ferragens", "Acessório para portas e gavetas"),
            MaterialExemplo("Fita de borda", "Acabamentos", "Acabamento das bordas do MDF"),
            MaterialExemplo("Verniz", "Acabamentos", "Proteção e acabamento da madeira")
        )
    }

    val filtrados = materiais.filter {
        (categoria == "Todos" || it.categoria == categoria) &&
            (pesquisa.isBlank() ||
                it.nome.contains(pesquisa, ignoreCase = true) ||
                it.descricao.contains(pesquisa, ignoreCase = true))
    }

    Scaffold(
        containerColor = Fundo,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Azul)
                    .statusBarsPadding()
                    .height(62.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Materiais",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { mostrarAviso = true }) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Adicionar material",
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                placeholder = { Text("Buscar materiais...") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { item ->
                    FilterChip(
                        selected = categoria == item,
                        onClick = { categoria = item },
                        label = { Text(item, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "${filtrados.size} materiais encontrados",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtrados) { material ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = Marrom,
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(Modifier.width(14.dp))

                            Column {
                                Text(
                                    material.nome,
                                    color = Azul,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    material.categoria,
                                    color = Marrom,
                                    fontSize = 11.sp
                                )

                                Text(
                                    material.descricao,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarAviso) {
        AlertDialog(
            onDismissRequest = { mostrarAviso = false },
            title = { Text("Cadastrar material") },
            text = {
                Text(
                    "O cadastro de materiais será implementado " +
                        "na próxima etapa."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarAviso = false }) {
                    Text("Entendi")
                }
            }
        )
    }
}