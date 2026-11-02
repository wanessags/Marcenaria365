
package com.nexo.marcenaria365.ui.screens.materiais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.data.repository.MaterialDados
import com.nexo.marcenaria365.data.repository.MaterialItem
import com.nexo.marcenaria365.data.repository.MaterialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Marrom = Color(0xFFB88966)

private fun numero(valor: Double): String {
    return if (valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString().replace('.', ',')
    }
}

private fun moeda(valor: Double): String {
    return "R$ " + String.format(
        Locale.forLanguageTag("pt-BR"),
        "%.2f",
        valor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaisScreen(
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    val escopo = rememberCoroutineScope()

    var materiais by remember {
        mutableStateOf<List<MaterialItem>>(emptyList())
    }

    var pesquisa by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Todos") }
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var materialEditando by remember {
        mutableStateOf<MaterialItem?>(null)
    }

    var materialExcluindo by remember {
        mutableStateOf<MaterialItem?>(null)
    }

    fun carregarMateriais() {
        escopo.launch {
            carregando = true
            erro = null

            val resultado = withContext(Dispatchers.IO) {
                MaterialRepository.listar()
            }

            resultado.onSuccess {
                materiais = it
            }.onFailure {
                erro = it.message ?: "Não foi possível carregar materiais."
            }

            carregando = false
        }
    }

    LaunchedEffect(Unit) {
        carregarMateriais()
    }

    val categorias = listOf("Todos") +
            materiais.map { it.categoria }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

    val filtrados = materiais.filter { material ->
        val categoriaValida =
            categoria == "Todos" ||
                    material.categoria == categoria

        val pesquisaValida =
            pesquisa.isBlank() ||
                    material.nome.contains(
                        pesquisa,
                        ignoreCase = true
                    ) ||
                    material.categoria.contains(
                        pesquisa,
                        ignoreCase = true
                    )

        categoriaValida && pesquisaValida
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

                IconButton(
                    onClick = {
                        materialEditando = null
                        mostrarFormulario = true
                    }
                ) {
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
                placeholder = {
                    Text("Buscar materiais...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { item ->
                    FilterChip(
                        selected = categoria == item,
                        onClick = { categoria = item },
                        label = {
                            Text(item, fontSize = 12.sp)
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "${filtrados.size} materiais encontrados",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (carregando) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Azul)
                }
            }

            erro?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                TextButton(onClick = { carregarMateriais() }) {
                    Text("Tentar novamente")
                }
            }

            Spacer(Modifier.height(12.dp))

            if (!carregando && erro == null && filtrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (materiais.isEmpty()) {
                            "Nenhum material cadastrado.\nToque em + para adicionar."
                        } else {
                            "Nenhum material encontrado."
                        },
                        color = Color.Gray
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = filtrados,
                    key = { it.id }
                ) { material ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                materialEditando = material
                                mostrarFormulario = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = Marrom,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = material.nome,
                                    color = Azul,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = material.categoria,
                                    color = Marrom,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Estoque: " +
                                            numero(material.quantidadeEstoque) +
                                            " ${material.unidade}",
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Custo unitário: " +
                                            moeda(material.custoUnitario),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                if (
                                    material.quantidadeEstoque <=
                                    material.estoqueMinimo
                                ) {
                                    Text(
                                        text = "Estoque baixo",
                                        color = Color(0xFFB45309),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    materialExcluindo = material
                                }
                            ) {
                                Text(
                                    "Excluir",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioMaterial(
            material = materialEditando,
            salvando = salvando,
            onFechar = {
                if (!salvando) {
                    mostrarFormulario = false
                }
            },
            onSalvar = { dados ->
                escopo.launch {
                    salvando = true
                    erro = null

                    val resultado = withContext(Dispatchers.IO) {
                        val atual = materialEditando

                        if (atual == null) {
                            MaterialRepository.cadastrar(dados)
                        } else {
                            MaterialRepository.editar(
                                atual.id,
                                dados
                            )
                        }
                    }

                    resultado.onSuccess {
                        mostrarFormulario = false
                        materialEditando = null
                        carregarMateriais()
                    }.onFailure {
                        erro = it.message
                            ?: "Não foi possível salvar."
                    }

                    salvando = false
                }
            }
        )
    }

    materialExcluindo?.let { material ->
        AlertDialog(
            onDismissRequest = {
                if (!salvando) {
                    materialExcluindo = null
                }
            },
            title = { Text("Excluir material?") },
            text = {
                Text(
                    "Deseja realmente excluir '${material.nome}'?"
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !salvando,
                    onClick = {
                        escopo.launch {
                            salvando = true
                            erro = null

                            val resultado = withContext(
                                Dispatchers.IO
                            ) {
                                MaterialRepository.excluir(material.id)
                            }

                            resultado.onSuccess {
                                materialExcluindo = null
                                carregarMateriais()
                            }.onFailure {
                                erro = it.message
                                    ?: "Erro ao excluir material."
                            }

                            salvando = false
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !salvando,
                    onClick = { materialExcluindo = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FormularioMaterial(
    material: MaterialItem?,
    salvando: Boolean,
    onFechar: () -> Unit,
    onSalvar: (MaterialDados) -> Unit
) {
    var nome by remember(material?.id) {
        mutableStateOf(material?.nome ?: "")
    }

    var categoria by remember(material?.id) {
        mutableStateOf(material?.categoria ?: "")
    }

    var unidade by remember(material?.id) {
        mutableStateOf(material?.unidade ?: "UN")
    }

    var quantidade by remember(material?.id) {
        mutableStateOf(
            material?.quantidadeEstoque?.let(::numero) ?: "0"
        )
    }

    var minimo by remember(material?.id) {
        mutableStateOf(
            material?.estoqueMinimo?.let(::numero) ?: "0"
        )
    }

    var custo by remember(material?.id) {
        mutableStateOf(
            material?.custoUnitario?.let(::numero) ?: "0"
        )
    }

    var erroFormulario by remember {
        mutableStateOf<String?>(null)
    }

    fun converterNumero(texto: String): Double? {
        return texto.trim().replace(',', '.').toDoubleOrNull()
    }

    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Text(
                if (material == null) {
                    "Cadastrar material"
                } else {
                    "Editar material"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do material") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria") },
                    placeholder = { Text("Ex.: Madeiras") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = unidade,
                    onValueChange = { unidade = it },
                    label = { Text("Unidade") },
                    placeholder = { Text("UN, M, M², KG") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade em estoque") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = minimo,
                    onValueChange = { minimo = it },
                    label = { Text("Estoque mínimo") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = custo,
                    onValueChange = { custo = it },
                    label = { Text("Custo unitário (R$)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                erroFormulario?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                if (salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Azul
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !salvando,
                onClick = {
                    val qtd = converterNumero(quantidade)
                    val estoqueMinimo = converterNumero(minimo)
                    val valor = converterNumero(custo)

                    when {
                        nome.isBlank() -> {
                            erroFormulario = "Informe o nome."
                        }

                        nome.trim().length > 120 -> {
                            erroFormulario = "Nome muito longo."
                        }

                        categoria.length > 60 -> {
                            erroFormulario = "Categoria muito longa."
                        }

                        unidade.isBlank() ||
                                unidade.length > 20 -> {
                            erroFormulario = "Informe uma unidade válida."
                        }

                        qtd == null ||
                                !qtd.isFinite() ||
                                qtd < 0 -> {
                            erroFormulario =
                                "Informe uma quantidade válida."
                        }

                        estoqueMinimo == null ||
                                !estoqueMinimo.isFinite() ||
                                estoqueMinimo < 0 -> {
                            erroFormulario =
                                "Informe um estoque mínimo válido."
                        }

                        valor == null ||
                                !valor.isFinite() ||
                                valor < 0 -> {
                            erroFormulario =
                                "Informe um custo válido."
                        }

                        else -> {
                            erroFormulario = null

                            onSalvar(
                                MaterialDados(
                                    nome = nome,
                                    categoria = categoria,
                                    unidade = unidade,
                                    quantidadeEstoque = qtd,
                                    estoqueMinimo = estoqueMinimo,
                                    custoUnitario = valor
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !salvando,
                onClick = onFechar
            ) {
                Text("Cancelar")
            }
        }
    )
}
