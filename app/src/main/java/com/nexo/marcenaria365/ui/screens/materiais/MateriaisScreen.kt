
package com.nexo.marcenaria365.ui.screens.materiais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
private val Cinza = Color(0xFF66727B)
private val Borda = Color(0xFFE9E3DD)
private val Branco = Color.White

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

@Composable
fun MateriaisScreen(
    onVoltar: () -> Unit
) {
    BackHandler(onBack = onVoltar)

    val escopo = rememberCoroutineScope()

    var materiais by remember {
        mutableStateOf<List<MaterialItem>>(emptyList())
    }

    var pesquisa by remember {
        mutableStateOf("")
    }

    var categoria by remember {
        mutableStateOf("Todos")
    }

    var carregando by remember {
        mutableStateOf(true)
    }

    var salvando by remember {
        mutableStateOf(false)
    }

    var erro by remember {
        mutableStateOf<String?>(null)
    }

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
                erro = it.message
                    ?: "Não foi possível carregar materiais."
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
                IconButton(
                    onClick = onVoltar
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Branco
                    )
                }

                Text(
                    text = "Materiais",
                    color = Branco,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(48.dp))
            }
        },

        // BOTAO + NA PARTE INFERIOR
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    materialEditando = null
                    mostrarFormulario = true
                },
                containerColor = Azul,
                contentColor = Branco,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Adicionar material",
                    modifier = Modifier.size(26.dp)
                )
            }
        },

        floatingActionButtonPosition = FabPosition.End

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            // BUSCAR MATERIAIS
            OutlinedTextField(
                value = pesquisa,
                onValueChange = {
                    pesquisa = it
                },
                placeholder = {
                    Text("Buscar materiais...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Cinza
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Branco,
                    unfocusedContainerColor = Branco,
                    focusedBorderColor = Azul,
                    unfocusedBorderColor = Borda
                )
            )

            Spacer(Modifier.height(12.dp))

            // FILTROS POR CATEGORIA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { item ->
                    FilterChip(
                        selected = categoria == item,
                        onClick = {
                            categoria = item
                        },
                        label = {
                            Text(
                                text = item,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Branco,
                            selectedContainerColor = Azul,
                            selectedLabelColor = Branco,
                            labelColor = Azul
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "${filtrados.size} materiais encontrados",
                fontSize = 12.sp,
                color = Cinza
            )

            if (carregando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Azul
                    )
                }
            }

            erro?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                TextButton(
                    onClick = {
                        carregarMateriais()
                    }
                ) {
                    Text("Tentar novamente")
                }
            }

            Spacer(Modifier.height(12.dp))

            if (
                !carregando &&
                erro == null &&
                filtrados.isEmpty()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFFB8B4AE),
                            modifier = Modifier.size(44.dp)
                        )

                        Text(
                            text = if (materiais.isEmpty()) {
                                "Nenhum material cadastrado.\n" +
                                        "Toque no + abaixo para adicionar."
                            } else {
                                "Nenhum material encontrado."
                            },
                            color = Cinza,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // LISTA DE MATERIAIS
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 95.dp
                )
            ) {
                items(
                    items = filtrados,
                    key = { it.id }
                ) { material ->

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Branco
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
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
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
                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Text(
                                    text = material.categoria,
                                    color = Marrom,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Estoque: " +
                                            numero(
                                                material.quantidadeEstoque
                                            ) +
                                            " ${material.unidade}",
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Custo unitário: " +
                                            moeda(material.custoUnitario),
                                    fontSize = 12.sp,
                                    color = Cinza
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
                                    text = "Excluir",
                                    color = MaterialTheme
                                        .colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // FORMULARIO DE CADASTRO E EDICAO
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

                    val atual = materialEditando

                    val resultado = withContext(
                        Dispatchers.IO
                    ) {
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

    // CONFIRMACAO DE EXCLUSAO
    materialExcluindo?.let { material ->
        AlertDialog(
            onDismissRequest = {
                if (!salvando) {
                    materialExcluindo = null
                }
            },
            title = {
                Text("Excluir material?")
            },
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
                                MaterialRepository.excluir(
                                    material.id
                                )
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
                    onClick = {
                        materialExcluindo = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// FORMULARIO COMPLETO COM TECLADO AJUSTADO
@Composable
private fun FormularioMaterial(
    material: MaterialItem?,
    salvando: Boolean,
    onFechar: () -> Unit,
    onSalvar: (MaterialDados) -> Unit
) {
    val teclado = LocalSoftwareKeyboardController.current

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

    var erroFormulario by remember(material?.id) {
        mutableStateOf<String?>(null)
    }

    fun converterNumero(texto: String): Double? {
        return texto
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()
    }

    fun salvarMaterial() {
        val qtd = converterNumero(quantidade)
        val estoqueMinimo = converterNumero(minimo)
        val valor = converterNumero(custo)

        when {
            nome.isBlank() -> {
                erroFormulario = "Informe o nome do material."
            }

            nome.trim().length > 120 -> {
                erroFormulario = "Nome muito longo."
            }

            categoria.length > 60 -> {
                erroFormulario = "Categoria muito longa."
            }

            unidade.isBlank() ||
                    unidade.length > 20 -> {
                erroFormulario =
                    "Informe uma unidade válida."
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
                teclado?.hide()
                erroFormulario = null

                onSalvar(
                    MaterialDados(
                        nome = nome.trim(),
                        categoria = categoria.trim(),
                        unidade = unidade.trim(),
                        quantidadeEstoque = qtd,
                        estoqueMinimo = estoqueMinimo,
                        custoUnitario = valor
                    )
                )
            }
        }
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Branco,
        unfocusedContainerColor = Branco,
        focusedBorderColor = Marrom,
        unfocusedBorderColor = Borda,
        focusedTextColor = Azul,
        unfocusedTextColor = Azul
    )

    Dialog(
        onDismissRequest = {
            if (!salvando) {
                onFechar()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !salvando,
            dismissOnClickOutside = !salvando,
            usePlatformDefaultWidth = false
        )
    ) {
        // USA O ESPACO DISPONIVEL QUANDO
        // O TECLADO ESTIVER ABERTO
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        max = maxHeight
                    ),
                shape = RoundedCornerShape(22.dp),
                color = Fundo,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    // CABECALHO
                    Text(
                        text = if (material == null) {
                            "Cadastrar material"
                        } else {
                            "Editar material"
                        },
                        color = Azul,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = if (material == null) {
                            "Adicione um material ao seu estoque."
                        } else {
                            "Atualize os dados do material."
                        },
                        color = Cinza,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // CAMPOS COM ROLAGEM INDEPENDENTE
                    Column(
                        modifier = Modifier
                            .weight(
                                weight = 1f,
                                fill = false
                            )
                            .verticalScroll(
                                rememberScrollState()
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(11.dp)
                    ) {

                        Text(
                            text = "Nome do material",
                            color = Azul,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = nome,
                            onValueChange = {
                                nome = it
                                erroFormulario = null
                            },
                            placeholder = {
                                Text("Ex.: MDF branco")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !salvando,
                            shape = RoundedCornerShape(11.dp),
                            colors = coresCampo,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        Text(
                            text = "Categoria",
                            color = Azul,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = categoria,
                            onValueChange = {
                                categoria = it
                            },
                            placeholder = {
                                Text("Ex.: Madeiras")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !salvando,
                            shape = RoundedCornerShape(11.dp),
                            colors = coresCampo,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        Text(
                            text = "Unidade de medida",
                            color = Azul,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = unidade,
                            onValueChange = {
                                unidade = it
                            },
                            placeholder = {
                                Text("UN, M, M², KG")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !salvando,
                            shape = RoundedCornerShape(11.dp),
                            colors = coresCampo,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        Text(
                            text = "Quantidade em estoque",
                            color = Azul,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = quantidade,
                            onValueChange = {
                                quantidade = it
                            },
                            placeholder = {
                                Text("0")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !salvando,
                            shape = RoundedCornerShape(11.dp),
                            colors = coresCampo,
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )
                        )

                        Text(
                            text = "Estoque mínimo",
                            color = Azul,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = minimo,
                            onValueChange = {
                                minimo = it
                            },
                            placeholder = {
                                Text("0")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !salvando,
                            shape = RoundedCornerShape(11.dp),
                            colors = coresCampo,
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )
                        )

                        Text(
                            text = "Custo unitário (R$)",
                            color = Azul,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = custo,
                            onValueChange = {
                                custo = it
                                erroFormulario = null
                            },
                            placeholder = {
                                Text("0,00")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !salvando,
                            shape = RoundedCornerShape(11.dp),
                            colors = coresCampo,
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    teclado?.hide()
                                }
                            )
                        )

                        erroFormulario?.let { mensagem ->
                            Text(
                                text = mensagem,
                                color = MaterialTheme
                                    .colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(5.dp))
                    }

                    Spacer(Modifier.height(14.dp))

                    HorizontalDivider(
                        color = Borda
                    )

                    Spacer(Modifier.height(14.dp))

                    // BOTOES SEMPRE FORA DA ROLAGEM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                teclado?.hide()
                                onFechar()
                            },
                            enabled = !salvando,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = Branco,
                                    contentColor = Azul
                                ),
                            border = BorderStroke(
                                1.dp,
                                Borda
                            )
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 13.sp,
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                salvarMaterial()
                            },
                            enabled = !salvando,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Azul,
                                contentColor = Branco
                            )
                        ) {
                            if (salvando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(19.dp),
                                    color = Branco,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Salvar",
                                    fontSize = 13.sp,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
