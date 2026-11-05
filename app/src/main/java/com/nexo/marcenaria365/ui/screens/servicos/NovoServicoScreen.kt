
package com.nexo.marcenaria365.ui.screens.servicos

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFF94613C)
private val Cinza = Color(0xFF53616A)
private val Borda = Color(0xFF89969E)
private val Verde = Color(0xFF237A58)
private val Vermelho = Color(0xFFB3261E)

@Composable
fun NovoServicoScreen(
    clientes: List<ClienteUi>,
    onVoltar: () -> Unit,
    onSalvar: (ServicoUi) -> Unit
) {
    var clienteId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var tipo by rememberSaveable {
        mutableStateOf("")
    }

    var descricao by rememberSaveable {
        mutableStateOf("")
    }

    var dataEntrega by rememberSaveable {
        mutableStateOf("")
    }

    var valor by rememberSaveable {
        mutableStateOf("")
    }

    var fotoUri by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var erro by rememberSaveable {
        mutableStateOf("")
    }

    var tentouSalvar by rememberSaveable {
        mutableStateOf(false)
    }

    var menuClientes by remember {
        mutableStateOf(false)
    }

    var menuTipos by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    // SELETOR DE FOTO
    val seletorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            fotoUri = uri.toString()
        }
    }

    // PREVIA DA FOTO REAL
    val imagemSelecionada by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = fotoUri
    ) {
        value = null

        val uriAtual = fotoUri ?: return@produceState

        value = withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(uriAtual)
                val resolver = context.contentResolver

                val limites = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                resolver.openInputStream(uri)?.use { entrada ->
                    BitmapFactory.decodeStream(
                        entrada,
                        null,
                        limites
                    )
                }

                var amostragem = 1

                while (
                    limites.outWidth / amostragem > 1200 ||
                    limites.outHeight / amostragem > 1200
                ) {
                    amostragem *= 2
                }

                val opcoes = BitmapFactory.Options().apply {
                    inSampleSize = amostragem
                }

                resolver.openInputStream(uri)?.use { entrada ->
                    BitmapFactory.decodeStream(
                        entrada,
                        null,
                        opcoes
                    )
                }?.asImageBitmap()
            }.getOrNull()
        }
    }

    // SELETOR DE DATA
    val calendario = Calendar.getInstance()

    val seletorData = DatePickerDialog(
        context,
        { _, ano, mes, dia ->
            dataEntrega = "%02d/%02d/%04d".format(
                dia,
                mes + 1,
                ano
            )
            erro = ""
        },
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH),
        calendario.get(Calendar.DAY_OF_MONTH)
    )

    val clienteSelecionado = clientes.find {
        it.id == clienteId
    }

    val clientesDisponiveis = clientes.filter {
        it.ativo
    }

    // VALIDACAO
    val valorNumero = valor
        .replace("R$", "")
        .trim()
        .let { texto ->
            if (texto.contains(",")) {
                texto
                    .replace(".", "")
                    .replace(",", ".")
            } else {
                texto
            }
        }
        .toDoubleOrNull()

    val clienteInvalido =
        tentouSalvar && clienteSelecionado == null

    val tipoInvalido =
        tentouSalvar && tipo.isBlank()

    val descricaoInvalida =
        tentouSalvar && descricao.isBlank()

    val dataInvalida =
        tentouSalvar && dataEntrega.isBlank()

    val valorInvalido =
        tentouSalvar && (
                valorNumero == null ||
                        !valorNumero.isFinite() ||
                        valorNumero <= 0.0
                )

    fun salvarServico() {
        tentouSalvar = true

        erro = when {
            clienteSelecionado == null ->
                "Selecione um cliente."

            tipo.isBlank() ->
                "Selecione o tipo de serviço."

            descricao.isBlank() ->
                "Informe a descrição do serviço."

            dataEntrega.isBlank() ->
                "Selecione a data prevista de entrega."

            valorNumero == null ||
                    !valorNumero.isFinite() ||
                    valorNumero <= 0.0 ->
                "Informe um valor estimado maior que zero."

            else -> ""
        }

        if (erro.isNotBlank()) {
            teclado?.hide()
            foco.clearFocus()
            return
        }

        val clienteValido = clienteSelecionado
            ?: return

        val numeroValido = valorNumero
            ?: return

        teclado?.hide()
        foco.clearFocus()

        onSalvar(
            ServicoUi(
                id = System.currentTimeMillis(),
                clienteId = clienteValido.id,
                clienteNome = clienteValido.nome,
                titulo = tipo,
                descricao = descricao.trim(),
                entrega = dataEntrega,
                valorEstimado = numeroValido,
                status = "Orçamento",
                fotoUri = fotoUri
            )
        )
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Cobre,
        unfocusedBorderColor = Borda,
        focusedTextColor = Azul,
        unfocusedTextColor = Azul,
        focusedLabelColor = Azul,
        unfocusedLabelColor = Cinza,
        cursorColor = Azul,
        errorBorderColor = Vermelho,
        errorLabelColor = Vermelho
    )

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {

        // CABECALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Azul)
                .heightIn(min = 58.dp)
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
                text = "Novo serviço",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            // FOTO DO SERVICO
            Text(
                text = "Foto do serviço (opcional)",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(9.dp))

            if (fotoUri == null) {
                Surface(
                    onClick = {
                        seletorFoto.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 132.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Borda)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Cobre,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Adicionar foto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Azul
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = "Toque para escolher uma imagem",
                            fontSize = 12.sp,
                            color = Cinza,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Borda)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        if (imagemSelecionada != null) {
                            Image(
                                bitmap = imagemSelecionada!!,
                                contentDescription =
                                    "Prévia da foto selecionada para o serviço",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(165.dp)
                                    .clip(
                                        RoundedCornerShape(11.dp)
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(165.dp)
                                    .background(
                                        Fundo,
                                        RoundedCornerShape(11.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Carregando foto ou imagem indisponível",
                                    color = Cinza,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .semantics {
                                            liveRegion =
                                                LiveRegionMode.Polite
                                        }
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = Verde,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(Modifier.width(7.dp))

                            Text(
                                text = "Foto selecionada",
                                color = Azul,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    seletorFoto.launch("image/*")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Borda),
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Azul
                                )

                                Spacer(Modifier.width(5.dp))

                                Text(
                                    text = "Trocar",
                                    color = Azul,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    fotoUri = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Borda),
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Vermelho
                                )

                                Spacer(Modifier.width(5.dp))

                                Text(
                                    text = "Remover",
                                    color = Vermelho,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(19.dp))

            // CLIENTE
            Text(
                text = "Cliente *",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Box {
                OutlinedButton(
                    onClick = {
                        menuClientes = true
                        teclado?.hide()
                        foco.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .then(
                            if (clienteInvalido) {
                                Modifier.semantics {
                                    error("Selecione um cliente.")
                                }
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Azul
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (clienteInvalido) Vermelho else Borda
                    )
                ) {
                    Text(
                        text = clienteSelecionado?.nome
                            ?: "Selecione o cliente",
                        modifier = Modifier.weight(1f),
                        color = if (clienteSelecionado == null) {
                            Cinza
                        } else {
                            Azul
                        }
                    )

                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = menuClientes,
                    onDismissRequest = {
                        menuClientes = false
                    }
                ) {
                    if (clientesDisponiveis.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text("Nenhum cliente disponível")
                            },
                            enabled = false,
                            onClick = {}
                        )
                    } else {
                        clientesDisponiveis.forEach { cliente ->
                            DropdownMenuItem(
                                text = {
                                    Text(cliente.nome)
                                },
                                onClick = {
                                    clienteId = cliente.id
                                    menuClientes = false
                                    erro = ""
                                }
                            )
                        }
                    }
                }
            }

            if (clienteInvalido) {
                ErroNovoServico("Selecione um cliente.")
            }

            Spacer(Modifier.height(19.dp))

            // TIPO DE SERVICO
            Text(
                text = "Tipo de serviço *",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Box {
                OutlinedButton(
                    onClick = {
                        menuTipos = true
                        teclado?.hide()
                        foco.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .then(
                            if (tipoInvalido) {
                                Modifier.semantics {
                                    error(
                                        "Selecione o tipo de serviço."
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(
                        1.dp,
                        if (tipoInvalido) Vermelho else Borda
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Azul
                    )
                ) {
                    Text(
                        text = tipo.ifEmpty {
                            "Selecione o tipo"
                        },
                        modifier = Modifier.weight(1f),
                        color = if (tipo.isEmpty()) {
                            Cinza
                        } else {
                            Azul
                        }
                    )

                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = menuTipos,
                    onDismissRequest = {
                        menuTipos = false
                    }
                ) {
                    tiposServico.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(item)
                            },
                            onClick = {
                                tipo = item
                                menuTipos = false
                                erro = ""
                            }
                        )
                    }
                }
            }

            if (tipoInvalido) {
                ErroNovoServico(
                    "Selecione o tipo de serviço."
                )
            }

            Spacer(Modifier.height(19.dp))

            // DESCRICAO
            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                    erro = ""
                },
                label = {
                    Text("Descrição do serviço *")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 115.dp)
                    .then(
                        if (descricaoInvalida) {
                            Modifier.semantics {
                                error(
                                    "Informe a descrição do serviço."
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                placeholder = {
                    Text(
                        text = "Descreva o serviço...",
                        color = Cinza,
                        fontSize = 13.sp
                    )
                },
                minLines = 3,
                maxLines = 5,
                isError = descricaoInvalida,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            if (descricaoInvalida) {
                ErroNovoServico(
                    "Informe a descrição do serviço."
                )
            }

            Spacer(Modifier.height(19.dp))

            // DATA PREVISTA DE ENTREGA
            Text(
                text = "Data prevista de entrega *",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedButton(
                onClick = {
                    teclado?.hide()
                    foco.clearFocus()
                    seletorData.show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .then(
                        if (dataInvalida) {
                            Modifier.semantics {
                                error(
                                    "Selecione a data prevista de entrega."
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(
                    1.dp,
                    if (dataInvalida) Vermelho else Borda
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Azul
                )
            ) {
                Text(
                    text = dataEntrega.ifEmpty {
                        "Selecionar data"
                    },
                    color = if (dataEntrega.isEmpty()) {
                        Cinza
                    } else {
                        Azul
                    },
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Cinza
                )
            }

            if (dataInvalida) {
                ErroNovoServico(
                    "Selecione a data prevista de entrega."
                )
            }

            Spacer(Modifier.height(19.dp))

            // VALOR ESTIMADO
            OutlinedTextField(
                value = valor,
                onValueChange = {
                    valor = it
                    erro = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (valorInvalido) {
                            Modifier.semantics {
                                error(
                                    "Informe um valor estimado maior que zero."
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                label = {
                    Text("Valor estimado (R$) *")
                },
                placeholder = {
                    Text(
                        text = "0,00",
                        color = Cinza
                    )
                },
                singleLine = true,
                isError = valorInvalido,
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
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            if (valorInvalido) {
                ErroNovoServico(
                    "Informe um valor estimado maior que zero."
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "* Campos obrigatórios",
                color = Cinza,
                fontSize = 12.sp
            )

            if (erro.isNotBlank()) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = erro,
                    color = Vermelho,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                )
            }

            Spacer(Modifier.height(26.dp))
        }

        // BOTOES INFERIORES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onVoltar,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Borda),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Azul,
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Cancelar",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    salvarServico()
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Azul,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Salvar",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ErroNovoServico(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = Vermelho,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
            }
    )
}
