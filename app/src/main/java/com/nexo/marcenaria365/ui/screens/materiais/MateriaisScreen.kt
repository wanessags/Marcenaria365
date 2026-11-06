
package com.nexo.marcenaria365.ui.screens.materiais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

private val AzulMateriais = Color(0xFF193447)
private val CobreMateriais = Color(0xFFA47752)

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

private fun converterNumeroMaterial(
    texto: String
): Double? {
    val limpo = texto.trim().replace("R$", "").trim()
    val normalizado = if (limpo.contains(",")) {
        limpo.replace(".", "").replace(",", ".")
    } else {
        limpo
    }
    return normalizado.toDoubleOrNull()
}

@Composable
fun MateriaisScreen(
    onVoltar: () -> Unit
) {
    val cores = MaterialTheme.colorScheme
    val escopo = rememberCoroutineScope()

    var materiais by remember {
        mutableStateOf<List<MaterialItem>>(emptyList())
    }

    var pesquisa by rememberSaveable {
        mutableStateOf("")
    }

    var categoria by rememberSaveable {
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

    BackHandler(
        enabled = !mostrarFormulario &&
                materialExcluindo == null,
        onBack = onVoltar
    )

    Scaffold(
        containerColor = cores.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulMateriais)
                    .statusBarsPadding()
                    .heightIn(min = 62.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar para Mais",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Materiais",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )

                Spacer(Modifier.width(48.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    materialEditando = null
                    erro = null
                    mostrarFormulario = true
                },
                containerColor = cores.primary,
                contentColor = cores.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Adicionar material",
                    modifier = Modifier.size(26.dp)
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
            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                label = { Text("Buscar materiais") },
                placeholder = {
                    Text("Nome ou categoria")
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cores.surface,
                    unfocusedContainerColor = cores.surface,
                    focusedTextColor = cores.onSurface,
                    unfocusedTextColor = cores.onSurface,
                    focusedBorderColor = cores.primary,
                    unfocusedBorderColor = cores.outline
                )
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Filtrar por categoria",
                color = cores.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

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
                        onClick = { categoria = item },
                        label = {
                            Text(item, fontSize = 13.sp)
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = cores.surface,
                            labelColor = cores.onSurface,
                            selectedContainerColor =
                                cores.primaryContainer,
                            selectedLabelColor =
                                cores.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (filtrados.size == 1) {
                    "1 material encontrado"
                } else {
                    "${filtrados.size} materiais encontrados"
                },
                color = cores.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )

            erro?.let { mensagem ->
                Spacer(Modifier.height(10.dp))

                Text(
                    mensagem,
                    color = cores.error,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Assertive
                    }
                )

                TextButton(onClick = { carregarMateriais() }) {
                    Text("Tentar novamente")
                }
            }

            Spacer(Modifier.height(10.dp))

            if (carregando) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Carregando materiais...")
                }
            }

            if (
                !carregando &&
                erro == null &&
                filtrados.isEmpty()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 35.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = cores.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )

                    Text(
                        text = if (materiais.isEmpty()) {
                            "Nenhum material cadastrado.\nToque no + para adicionar."
                        } else {
                            "Nenhum material encontrado."
                        },
                        textAlign = TextAlign.Center,
                        color = cores.onSurfaceVariant
                    )
                }
            }

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
                        onClick = {
                            materialEditando = material
                            erro = null
                            mostrarFormulario = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = cores.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = cores.onPrimaryContainer,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(25.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = material.nome,
                                    color = cores.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = material.categoria.ifBlank {
                                        "Sem categoria"
                                    },
                                    color = cores.onSurfaceVariant,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Estoque: ${
                                        numero(material.quantidadeEstoque)
                                    } ${material.unidade}",
                                    color = cores.onSurface,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Custo unitário: ${
                                        moeda(material.custoUnitario)
                                    }",
                                    color = cores.onSurfaceVariant,
                                    fontSize = 12.sp
                                )

                                if (
                                    material.quantidadeEstoque <=
                                    material.estoqueMinimo
                                ) {
                                    Text(
                                        "Estoque baixo",
                                        color = cores.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    materialExcluindo = material
                                },
                                modifier = Modifier.sizeIn(
                                    minWidth = 48.dp,
                                    minHeight = 48.dp
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription =
                                        "Excluir ${material.nome}",
                                    tint = cores.error
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
            erroApi = erro,
            onFechar = {
                if (!salvando) {
                    mostrarFormulario = false
                    materialEditando = null
                }
            },
            onSalvar = { dados ->
                if (!salvando) {
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
            }
        )
    }

    materialExcluindo?.let { material ->
        AlertDialog(
            onDismissRequest = {
                if (!salvando) materialExcluindo = null
            },
            title = {
                Text(
                    "Excluir material?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Deseja realmente excluir '${material.nome}'?"
                    )
                    Text(
                        "Essa ação não poderá ser desfeita.",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    erro?.let {
                        Text(it, color = cores.error)
                    }
                }
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
                    Text("Excluir", color = cores.error)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !salvando,
                    onClick = {
                        materialExcluindo = null
                        erro = null
                    }
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
    erroApi: String?,
    onFechar: () -> Unit,
    onSalvar: (MaterialDados) -> Unit
) {
    val cores = MaterialTheme.colorScheme
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

    fun salvarMaterial() {
        val qtd = converterNumeroMaterial(quantidade)
        val estoqueMinimo = converterNumeroMaterial(minimo)
        val valor = converterNumeroMaterial(custo)

        erroFormulario = when {
            nome.isBlank() ->
                "Informe o nome do material."

            nome.trim().length > 120 ->
                "O nome deve ter até 120 caracteres."

            categoria.length > 60 ->
                "A categoria deve ter até 60 caracteres."

            unidade.isBlank() || unidade.length > 20 ->
                "Informe uma unidade de medida válida."

            qtd == null || !qtd.isFinite() || qtd < 0 ->
                "Informe uma quantidade válida."

            estoqueMinimo == null ||
                    !estoqueMinimo.isFinite() ||
                    estoqueMinimo < 0 ->
                "Informe um estoque mínimo válido."

            valor == null || !valor.isFinite() || valor < 0 ->
                "Informe um custo unitário válido."

            else -> null
        }

        if (
            erroFormulario == null &&
            qtd != null &&
            estoqueMinimo != null &&
            valor != null
        ) {
            teclado?.hide()

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

    Dialog(
        onDismissRequest = {
            if (!salvando) onFechar()
        },
        properties = DialogProperties(
            dismissOnBackPress = !salvando,
            dismissOnClickOutside = !salvando,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(
                    horizontal = 14.dp,
                    vertical = 16.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(22.dp),
                color = cores.surface,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    )
                ) {

                    // CABEÇALHO
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = cores.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = cores.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(11.dp)
                                    .size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(11.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (material == null) {
                                    "Cadastrar material"
                                } else {
                                    "Editar material"
                                },
                                color = cores.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.semantics {
                                    heading()
                                }
                            )

                            Spacer(Modifier.height(3.dp))

                            Text(
                                text = if (material == null) {
                                    "Adicione um item ao seu estoque."
                                } else {
                                    "Atualize os dados do estoque."
                                },
                                color = cores.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                teclado?.hide()
                                onFechar()
                            },
                            enabled = !salvando
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Fechar formulário",
                                tint = cores.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(15.dp))

                    HorizontalDivider(
                        color = cores.outlineVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // CAMPOS ROLÁVEIS
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(
                                rememberScrollState()
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Informações do material",
                            color = cores.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                heading()
                            }
                        )

                        CampoFormularioMaterial(
                            valor = nome,
                            onValorChange = {
                                nome = it
                                erroFormulario = null
                            },
                            rotulo = "Nome do material *",
                            exemplo = "Ex.: MDF branco 15 mm",
                            habilitado = !salvando
                        )

                        CampoFormularioMaterial(
                            valor = categoria,
                            onValorChange = {
                                categoria = it
                                erroFormulario = null
                            },
                            rotulo = "Categoria",
                            exemplo = "Ex.: Madeiras",
                            habilitado = !salvando
                        )

                        CampoFormularioMaterial(
                            valor = unidade,
                            onValorChange = {
                                unidade = it
                                erroFormulario = null
                            },
                            rotulo = "Unidade de medida *",
                            exemplo = "UN, M, M² ou KG",
                            habilitado = !salvando
                        )

                        HorizontalDivider(
                            color = cores.outlineVariant,
                            modifier = Modifier.padding(
                                vertical = 4.dp
                            )
                        )

                        Text(
                            "Controle de estoque",
                            color = cores.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                heading()
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(9.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                CampoFormularioMaterial(
                                    valor = quantidade,
                                    onValorChange = {
                                        quantidade = it
                                        erroFormulario = null
                                    },
                                    rotulo = "Quantidade atual",
                                    exemplo = "0",
                                    habilitado = !salvando,
                                    tipoTeclado =
                                        KeyboardType.Decimal
                                )
                            }

                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                CampoFormularioMaterial(
                                    valor = minimo,
                                    onValorChange = {
                                        minimo = it
                                        erroFormulario = null
                                    },
                                    rotulo = "Estoque mínimo",
                                    exemplo = "0",
                                    habilitado = !salvando,
                                    tipoTeclado =
                                        KeyboardType.Decimal
                                )
                            }
                        }

                        Text(
                            text = "O estoque mínimo ajuda a identificar quando um material precisa ser reposto.",
                            color = cores.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        CampoFormularioMaterial(
                            valor = custo,
                            onValorChange = {
                                custo = it
                                erroFormulario = null
                            },
                            rotulo = "Custo unitário (R$)",
                            exemplo = "0,00",
                            habilitado = !salvando,
                            tipoTeclado = KeyboardType.Decimal,
                            acaoIme = ImeAction.Done,
                            aoConcluir = {
                                teclado?.hide()
                            }
                        )

                        Text(
                            text = "Informe o custo de uma unidade, não o valor total do estoque.",
                            color = cores.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        erroFormulario?.let { mensagem ->
                            Text(
                                mensagem,
                                color = cores.error,
                                fontSize = 12.sp,
                                modifier = Modifier.semantics {
                                    liveRegion =
                                        LiveRegionMode.Assertive
                                }
                            )
                        }

                        erroApi?.let { mensagem ->
                            Text(
                                mensagem,
                                color = cores.error,
                                fontSize = 12.sp,
                                modifier = Modifier.semantics {
                                    liveRegion =
                                        LiveRegionMode.Assertive
                                }
                            )
                        }

                        Text(
                            "* Campo obrigatório",
                            color = cores.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    HorizontalDivider(
                        color = cores.outlineVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    // BOTÕES FIXOS
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
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                cores.outline
                            )
                        ) {
                            Text(
                                "Cancelar",
                                color = cores.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { salvarMaterial() },
                            enabled = !salvando,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulMateriais,
                                contentColor = Color.White
                            )
                        ) {
                            if (salvando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )

                                Spacer(Modifier.width(5.dp))

                                Text(
                                    if (material == null) {
                                        "Salvar"
                                    } else {
                                        "Atualizar"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoFormularioMaterial(
    valor: String,
    onValorChange: (String) -> Unit,
    rotulo: String,
    exemplo: String,
    habilitado: Boolean,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    acaoIme: ImeAction = ImeAction.Next,
    aoConcluir: (() -> Unit)? = null
) {
    val cores = MaterialTheme.colorScheme

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = {
            Text(
                rotulo,
                fontSize = 13.sp
            )
        },
        placeholder = {
            Text(
                exemplo,
                fontSize = 13.sp
            )
        },
        enabled = habilitado,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = acaoIme
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                aoConcluir?.invoke()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = cores.surface,
            unfocusedContainerColor = cores.surface,
            focusedTextColor = cores.onSurface,
            unfocusedTextColor = cores.onSurface,
            focusedBorderColor = cores.primary,
            unfocusedBorderColor = cores.outline,
            focusedLabelColor = cores.primary,
            unfocusedLabelColor = cores.onSurfaceVariant,
            cursorColor = cores.primary
        )
    )
}
