
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

    // Mantém o carregamento original da API.
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

    // Mantém a pesquisa por nome ou categoria.
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

        // Cabeçalho original.
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF193447))
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
                        .semantics {
                            heading()
                        }
                )

                Spacer(Modifier.width(48.dp))
            }
        },

        // Preserva o botão + na parte inferior.
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    materialEditando = null
                    erro = null
                    mostrarFormulario = true
                },
                modifier = Modifier.sizeIn(
                    minWidth = 56.dp,
                    minHeight = 56.dp
                ),
                containerColor = cores.primary,
                contentColor = cores.onPrimary,
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
                label = {
                    Text("Buscar materiais")
                },
                placeholder = {
                    Text("Nome ou categoria")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
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

            Spacer(Modifier.height(12.dp))

            // FILTROS ORIGINAIS POR CATEGORIA
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
                        onClick = {
                            categoria = item
                        },
                        label = {
                            Text(
                                text = item,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.heightIn(
                            min = 48.dp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = cores.surface,
                            labelColor = cores.onSurface,
                            selectedContainerColor =
                                cores.primaryContainer,
                            selectedLabelColor =
                                cores.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = categoria == item,
                            borderColor = cores.outline,
                            selectedBorderColor = cores.primary
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // CONTAGEM ACESSÍVEL
            Text(
                text = if (filtrados.size == 1) {
                    "1 material encontrado"
                } else {
                    "${filtrados.size} materiais encontrados"
                },
                fontSize = 12.sp,
                color = cores.onSurfaceVariant,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )

            // CARREGAMENTO DA API
            if (carregando) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = cores.primary,
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Carregando materiais...",
                        color = cores.onSurfaceVariant
                    )
                }
            }

            // ERROS ORIGINAIS, AGORA ANUNCIADOS
            erro?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = cores.error,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Assertive
                        }
                )

                TextButton(
                    onClick = {
                        carregarMateriais()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
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
                            tint = cores.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )

                        Text(
                            text = if (materiais.isEmpty()) {
                                "Nenhum material cadastrado.\n" +
                                        "Toque no + abaixo para adicionar."
                            } else {
                                "Nenhum material encontrado."
                            },
                            color = cores.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // LISTA ORIGINAL DE MATERIAIS
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
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cores.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = cores.secondary,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = material.nome,
                                    color = cores.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = material.categoria,
                                    color = cores.secondary,
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = "Estoque: " +
                                            numero(
                                                material.quantidadeEstoque
                                            ) +
                                            " ${material.unidade}",
                                    color = cores.onSurface,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Custo unitário: " +
                                            moeda(material.custoUnitario),
                                    color = cores.onSurfaceVariant,
                                    fontSize = 12.sp
                                )

                                // Mantém o alerta de estoque baixo.
                                if (
                                    material.quantidadeEstoque <=
                                    material.estoqueMinimo
                                ) {
                                    Text(
                                        text = "Estoque baixo",
                                        color = cores.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Mantém o botão de exclusão.
                            TextButton(
                                onClick = {
                                    materialExcluindo = material
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription =
                                            "Excluir ${material.nome}"
                                    }
                            ) {
                                Text(
                                    text = "Excluir",
                                    color = cores.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // FORMULÁRIO ORIGINAL DE CADASTRO E EDIÇÃO
    if (mostrarFormulario) {
        FormularioMaterial(
            material = materialEditando,
            salvando = salvando,
            erroApi = erro,
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

    // CONFIRMAÇÃO ORIGINAL DE EXCLUSÃO
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
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deseja realmente excluir '${material.nome}'?"
                    )

                    Text(
                        text = "Esta ação não poderá ser desfeita.",
                        color = cores.onSurfaceVariant,
                        fontSize = 13.sp
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

                            resultado.onSuccess {
                                materialExcluindo = null
                                carregarMateriais()
                            }.onFailure {
                                erro = it.message
                                    ?: "Erro ao excluir material."
                            }

                            salvando = false
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Excluir",
                        color = cores.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !salvando,
                    onClick = {
                        materialExcluindo = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// MANTÉM O FORMULÁRIO COM ROLAGEM E
// BOTÕES FIXOS QUANDO O TECLADO ESTÁ ABERTO.
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

    // Mantém a conversão de números com vírgula.
    fun converterNumero(texto: String): Double? {
        return texto
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()
    }

    // Mantém todas as validações anteriores.
    fun salvarMaterial() {
        val qtd = converterNumero(quantidade)
        val estoqueMinimo = converterNumero(minimo)
        val valor = converterNumero(custo)

        when {
            nome.isBlank() -> {
                erroFormulario =
                    "Informe o nome do material."
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
        // Preserva o ajuste ao teclado.
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
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(22.dp),
                color = cores.background,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // CABEÇALHO
                    Text(
                        text = if (material == null) {
                            "Cadastrar material"
                        } else {
                            "Editar material"
                        },
                        color = cores.onBackground,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = if (material == null) {
                            "Adicione um material ao seu estoque."
                        } else {
                            "Atualize os dados do material."
                        },
                        color = cores.onSurfaceVariant,
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
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement =
                            Arrangement.spacedBy(11.dp)
                    ) {
                        CampoFormularioMaterial(
                            valor = nome,
                            onValorChange = {
                                nome = it
                                erroFormulario = null
                            },
                            rotulo = "Nome do material",
                            exemplo = "Ex.: MDF branco",
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
                            rotulo = "Unidade de medida",
                            exemplo = "UN, M, M², KG",
                            habilitado = !salvando
                        )

                        CampoFormularioMaterial(
                            valor = quantidade,
                            onValorChange = {
                                quantidade = it
                                erroFormulario = null
                            },
                            rotulo = "Quantidade em estoque",
                            exemplo = "0",
                            habilitado = !salvando,
                            tipoTeclado = KeyboardType.Decimal
                        )

                        CampoFormularioMaterial(
                            valor = minimo,
                            onValorChange = {
                                minimo = it
                                erroFormulario = null
                            },
                            rotulo = "Estoque mínimo",
                            exemplo = "0",
                            habilitado = !salvando,
                            tipoTeclado = KeyboardType.Decimal
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

                        erroFormulario?.let { mensagem ->
                            Text(
                                text = mensagem,
                                color = cores.error,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.semantics {
                                    liveRegion =
                                        LiveRegionMode.Assertive
                                }
                            )
                        }

                        erroApi?.let { mensagem ->
                            Text(
                                text = mensagem,
                                color = cores.error,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.semantics {
                                    liveRegion =
                                        LiveRegionMode.Assertive
                                }
                            )
                        }

                        Spacer(Modifier.height(5.dp))
                    }

                    Spacer(Modifier.height(14.dp))

                    HorizontalDivider(
                        color = cores.outline
                    )

                    Spacer(Modifier.height(14.dp))

                    // BOTÕES FORA DA ROLAGEM,
                    // COMO NO FORMULÁRIO ORIGINAL.
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
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = cores.surface,
                                    contentColor = cores.onSurface
                                ),
                            border = BorderStroke(
                                1.dp,
                                cores.outline
                            )
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                salvarMaterial()
                            },
                            enabled = !salvando,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cores.primary,
                                contentColor = cores.onPrimary
                            )
                        ) {
                            if (salvando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(19.dp),
                                    color = cores.onPrimary,
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

// Componente visual reutilizável.
// Não altera os valores nem a validação.
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
            Text(rotulo)
        },
        placeholder = {
            Text(exemplo)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = habilitado,
        shape = RoundedCornerShape(11.dp),
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
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = acaoIme
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                aoConcluir?.invoke()
            }
        )
    )
}
