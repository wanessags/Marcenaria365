
package com.nexo.marcenaria365.ui.screens.materiais

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

private val FundoMateriais = Color(0xFFFAF6F0)
private val AzulMateriais = Color(0xFF193447)
private val CobreMateriais = Color(0xFF94613C)
private val CinzaMateriais = Color(0xFF53616A)
private val BordaMateriais = Color(0xFF89969E)
private val VermelhoMateriais = Color(0xFFB3261E)
private val AlertaMateriais = Color(0xFF825B12)

private fun numeroMaterial(valor: Double): String {
    return if (valor.isFinite() && valor % 1.0 == 0.0) {
        valor.toLong().toString()
    } else {
        valor.toString().replace('.', ',')
    }
}

private fun moedaMaterial(valor: Double): String {
    return "R$ " + String.format(
        Locale.forLanguageTag("pt-BR"),
        "%.2f",
        valor
    )
}

private fun converterNumeroMaterial(texto: String): Double? {
    return texto.trim().replace(",", ".").toDoubleOrNull()
}

@Composable
fun MateriaisScreen(
    onVoltar: () -> Unit
) {
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

    var erroFormularioApi by remember {
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

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

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
                    ?: "Não foi possível carregar os materiais."
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
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoMateriais,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulMateriais)
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
                        contentDescription = "Voltar",
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
                        .semantics {
                            heading()
                        }
                )

                IconButton(
                    onClick = {
                        materialEditando = null
                        erroFormularioApi = null
                        mostrarFormulario = true
                    },
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
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
                onValueChange = {
                    pesquisa = it
                },
                label = {
                    Text("Buscar materiais")
                },
                placeholder = {
                    Text(
                        "Nome ou categoria",
                        color = CinzaMateriais
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = CinzaMateriais
                    )
                },
                modifier = Modifier.fillMaxWidth(),
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
                shape = RoundedCornerShape(14.dp),
                colors = coresCamposMateriais()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Filtrar por categoria",
                color = AzulMateriais,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

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
                            teclado?.hide()
                            foco.clearFocus()
                        },
                        label = {
                            Text(
                                text = item,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.heightIn(
                            min = 48.dp
                        ),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = AzulMateriais,
                                selectedContainerColor =
                                    Color(0xFFF1E4D7),
                                selectedLabelColor = AzulMateriais
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = categoria == item,
                                borderColor = BordaMateriais,
                                selectedBorderColor = CobreMateriais
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
                color = CinzaMateriais,
                fontSize = 12.sp,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )

            if (carregando) {
                Row(
                    modifier = Modifier.padding(
                        vertical = 20.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AzulMateriais,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Carregando materiais...",
                        color = CinzaMateriais,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }

            if (!carregando && erro != null) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = erro ?: "",
                    color = VermelhoMateriais,
                    fontSize = 13.sp,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )

                TextButton(
                    onClick = {
                        carregarMateriais()
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Tentar novamente",
                        color = AzulMateriais
                    )
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
                        .padding(top = 35.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (materiais.isEmpty()) {
                            "Nenhum material cadastrado. " +
                                    "Use o botão Adicionar material."
                        } else {
                            "Nenhum material encontrado."
                        },
                        color = CinzaMateriais,
                        fontSize = 14.sp,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(
                    bottom = 24.dp
                )
            ) {
                items(
                    items = filtrados,
                    key = { it.id }
                ) { material ->
                    val estoqueBaixo =
                        material.quantidadeEstoque <=
                                material.estoqueMinimo

                    Card(
                        onClick = {
                            materialEditando = material
                            erroFormularioApi = null
                            mostrarFormulario = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = BordaMateriais
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = CobreMateriais,
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics(
                                        mergeDescendants = true
                                    ) {
                                        contentDescription =
                                            "Editar material ${material.nome}. " +
                                                    "Categoria: ${material.categoria}. " +
                                                    "Estoque: ${
                                                        numeroMaterial(
                                                            material.quantidadeEstoque
                                                        )
                                                    } ${material.unidade}. " +
                                                    "Custo unitário: ${
                                                        moedaMaterial(
                                                            material.custoUnitario
                                                        )
                                                    }. " +
                                                    if (estoqueBaixo) {
                                                        "Estoque baixo."
                                                    } else {
                                                        "Estoque regular."
                                                    }
                                    }
                            ) {
                                Text(
                                    text = material.nome,
                                    color = AzulMateriais,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = material.categoria.ifBlank {
                                        "Sem categoria"
                                    },
                                    color = CobreMateriais,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "Estoque: ${
                                        numeroMaterial(
                                            material.quantidadeEstoque
                                        )
                                    } ${material.unidade}",
                                    color = CinzaMateriais,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Custo unitário: ${
                                        moedaMaterial(
                                            material.custoUnitario
                                        )
                                    }",
                                    color = CinzaMateriais,
                                    fontSize = 12.sp
                                )

                                if (estoqueBaixo) {
                                    Spacer(Modifier.height(5.dp))

                                    Text(
                                        text = "Estoque baixo",
                                        color = AlertaMateriais,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.width(5.dp))

                            TextButton(
                                onClick = {
                                    materialExcluindo = material
                                },
                                modifier = Modifier.heightIn(
                                    min = 48.dp
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 6.dp
                                )
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = VermelhoMateriais,
                                    modifier = Modifier.size(19.dp)
                                )

                                Spacer(Modifier.width(3.dp))

                                Text(
                                    text = "Excluir",
                                    color = VermelhoMateriais,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // CADASTRO E EDIÇÃO
    if (mostrarFormulario) {
        FormularioMaterialAcessivel(
            material = materialEditando,
            salvando = salvando,
            erroApi = erroFormularioApi,
            onFechar = {
                if (!salvando) {
                    mostrarFormulario = false
                    materialEditando = null
                    erroFormularioApi = null
                }
            },
            onSalvar = { dados ->
                if (!salvando) {
                    escopo.launch {
                        salvando = true
                        erroFormularioApi = null

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

                        salvando = false

                        resultado.onSuccess {
                            mostrarFormulario = false
                            materialEditando = null
                            carregarMateriais()
                        }.onFailure {
                            erroFormularioApi = it.message
                                ?: "Não foi possível salvar o material."
                        }
                    }
                }
            }
        )
    }

    // CONFIRMAÇÃO DE EXCLUSÃO
    materialExcluindo?.let { material ->
        AlertDialog(
            onDismissRequest = {
                if (!salvando) {
                    materialExcluindo = null
                }
            },
            title = {
                Text(
                    text = "Excluir material?",
                    color = AzulMateriais,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deseja excluir o material ${
                            material.nome
                        }?",
                        color = AzulMateriais
                    )

                    Text(
                        text = "Categoria: ${
                            material.categoria.ifBlank {
                                "Sem categoria"
                            }
                        }",
                        color = CinzaMateriais
                    )

                    Text(
                        text = "Esta ação não poderá ser desfeita.",
                        color = VermelhoMateriais,
                        fontWeight = FontWeight.SemiBold
                    )
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
                                MaterialRepository.excluir(
                                    material.id
                                )
                            }

                            salvando = false

                            resultado.onSuccess {
                                materialExcluindo = null
                                carregarMateriais()
                            }.onFailure {
                                materialExcluindo = null
                                erro = it.message
                                    ?: "Não foi possível excluir o material."
                            }
                        }
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = if (salvando) {
                            "Excluindo..."
                        } else {
                            "Excluir definitivamente"
                        },
                        color = VermelhoMateriais
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !salvando,
                    onClick = {
                        materialExcluindo = null
                    },
                    modifier = Modifier.heightIn(
                        min = 48.dp
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulMateriais
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun FormularioMaterialAcessivel(
    material: MaterialItem?,
    salvando: Boolean,
    erroApi: String?,
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
            material?.quantidadeEstoque
                ?.let(::numeroMaterial) ?: "0"
        )
    }

    var minimo by remember(material?.id) {
        mutableStateOf(
            material?.estoqueMinimo
                ?.let(::numeroMaterial) ?: "0"
        )
    }

    var custo by remember(material?.id) {
        mutableStateOf(
            material?.custoUnitario
                ?.let(::numeroMaterial) ?: "0"
        )
    }

    var erroFormulario by remember(material?.id) {
        mutableStateOf("")
    }

    var tentouSalvar by remember(material?.id) {
        mutableStateOf(false)
    }

    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current

    val qtd = converterNumeroMaterial(quantidade)
    val estoqueMinimo = converterNumeroMaterial(minimo)
    val custoUnitario = converterNumeroMaterial(custo)

    val erroNome = when {
        nome.isBlank() ->
            "Informe o nome do material."

        nome.trim().length > 120 ->
            "O nome pode ter no máximo 120 caracteres."

        else -> ""
    }

    val erroCategoria = if (categoria.trim().length > 60) {
        "A categoria pode ter no máximo 60 caracteres."
    } else {
        ""
    }

    val erroUnidade = if (
        unidade.isBlank() || unidade.trim().length > 20
    ) {
        "Informe uma unidade válida com até 20 caracteres."
    } else {
        ""
    }

    val erroQuantidade = if (
        qtd == null || !qtd.isFinite() || qtd < 0
    ) {
        "Informe uma quantidade válida, igual ou maior que zero."
    } else {
        ""
    }

    val erroMinimo = if (
        estoqueMinimo == null ||
        !estoqueMinimo.isFinite() ||
        estoqueMinimo < 0
    ) {
        "Informe um estoque mínimo válido, igual ou maior que zero."
    } else {
        ""
    }

    val erroCusto = if (
        custoUnitario == null ||
        !custoUnitario.isFinite() ||
        custoUnitario < 0
    ) {
        "Informe um custo válido, igual ou maior que zero."
    } else {
        ""
    }

    fun validarSalvar() {
        tentouSalvar = true

        erroFormulario = listOf(
            erroNome,
            erroCategoria,
            erroUnidade,
            erroQuantidade,
            erroMinimo,
            erroCusto
        ).firstOrNull { it.isNotBlank() } ?: ""

        if (erroFormulario.isNotBlank()) {
            return
        }

        val quantidadeValida = qtd ?: return
        val minimoValido = estoqueMinimo ?: return
        val custoValido = custoUnitario ?: return

        teclado?.hide()
        foco.clearFocus()

        onSalvar(
            MaterialDados(
                nome = nome.trim(),
                categoria = categoria.trim(),
                unidade = unidade.trim(),
                quantidadeEstoque = quantidadeValida,
                estoqueMinimo = minimoValido,
                custoUnitario = custoValido
            )
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!salvando) {
                onFechar()
            }
        },
        title = {
            Text(
                text = if (material == null) {
                    "Cadastrar material"
                } else {
                    "Editar material"
                },
                color = AzulMateriais,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erroFormulario = ""
                    },
                    label = {
                        Text("Nome do material *")
                    },
                    enabled = !salvando,
                    singleLine = true,
                    isError = tentouSalvar &&
                            erroNome.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (
                                tentouSalvar &&
                                erroNome.isNotBlank()
                            ) {
                                Modifier.semantics {
                                    error(erroNome)
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = coresCamposMateriais()
                )

                if (
                    tentouSalvar &&
                    erroNome.isNotBlank()
                ) {
                    ErroMaterialAcessivel(erroNome)
                }

                OutlinedTextField(
                    value = categoria,
                    onValueChange = {
                        categoria = it
                        erroFormulario = ""
                    },
                    label = {
                        Text("Categoria (opcional)")
                    },
                    placeholder = {
                        Text("Ex.: Madeiras")
                    },
                    enabled = !salvando,
                    singleLine = true,
                    isError = tentouSalvar &&
                            erroCategoria.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCamposMateriais()
                )

                if (
                    tentouSalvar &&
                    erroCategoria.isNotBlank()
                ) {
                    ErroMaterialAcessivel(erroCategoria)
                }

                OutlinedTextField(
                    value = unidade,
                    onValueChange = {
                        unidade = it
                        erroFormulario = ""
                    },
                    label = {
                        Text("Unidade *")
                    },
                    supportingText = {
                        Text(
                            text = "Ex.: UN, M, M², KG",
                            color = CinzaMateriais
                        )
                    },
                    enabled = !salvando,
                    singleLine = true,
                    isError = tentouSalvar &&
                            erroUnidade.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCamposMateriais()
                )

                if (
                    tentouSalvar &&
                    erroUnidade.isNotBlank()
                ) {
                    ErroMaterialAcessivel(erroUnidade)
                }

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = {
                        quantidade = it
                        erroFormulario = ""
                    },
                    label = {
                        Text("Quantidade em estoque *")
                    },
                    enabled = !salvando,
                    singleLine = true,
                    isError = tentouSalvar &&
                            erroQuantidade.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCamposMateriais()
                )

                if (
                    tentouSalvar &&
                    erroQuantidade.isNotBlank()
                ) {
                    ErroMaterialAcessivel(erroQuantidade)
                }

                OutlinedTextField(
                    value = minimo,
                    onValueChange = {
                        minimo = it
                        erroFormulario = ""
                    },
                    label = {
                        Text("Estoque mínimo *")
                    },
                    enabled = !salvando,
                    singleLine = true,
                    isError = tentouSalvar &&
                            erroMinimo.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCamposMateriais()
                )

                if (
                    tentouSalvar &&
                    erroMinimo.isNotBlank()
                ) {
                    ErroMaterialAcessivel(erroMinimo)
                }

                OutlinedTextField(
                    value = custo,
                    onValueChange = {
                        custo = it
                        erroFormulario = ""
                    },
                    label = {
                        Text("Custo unitário (R$) *")
                    },
                    enabled = !salvando,
                    singleLine = true,
                    isError = tentouSalvar &&
                            erroCusto.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            teclado?.hide()
                            foco.clearFocus()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = coresCamposMateriais()
                )

                if (
                    tentouSalvar &&
                    erroCusto.isNotBlank()
                ) {
                    ErroMaterialAcessivel(erroCusto)
                }

                Text(
                    text = "* Campos obrigatórios",
                    color = CinzaMateriais,
                    fontSize = 12.sp
                )

                if (erroFormulario.isNotBlank()) {
                    ErroMaterialAcessivel(erroFormulario)
                }

                if (erroApi != null) {
                    ErroMaterialAcessivel(erroApi)
                }

                if (salvando) {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = AzulMateriais,
                            strokeWidth = 2.dp
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "Salvando material...",
                            color = CinzaMateriais,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !salvando,
                onClick = {
                    validarSalvar()
                },
                modifier = Modifier.heightIn(
                    min = 48.dp
                )
            ) {
                Text(
                    text = "Salvar",
                    color = AzulMateriais,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !salvando,
                onClick = onFechar,
                modifier = Modifier.heightIn(
                    min = 48.dp
                )
            ) {
                Text(
                    text = "Cancelar",
                    color = CinzaMateriais
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun ErroMaterialAcessivel(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = VermelhoMateriais,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        }
    )
}

@Composable
private fun coresCamposMateriais() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreMateriais,
        unfocusedBorderColor = BordaMateriais,
        focusedTextColor = AzulMateriais,
        unfocusedTextColor = AzulMateriais,
        focusedLabelColor = AzulMateriais,
        unfocusedLabelColor = CinzaMateriais,
        cursorColor = AzulMateriais,
        errorBorderColor = VermelhoMateriais,
        errorLabelColor = VermelhoMateriais
    )
