
package com.nexo.marcenaria365.ui.screens.servicos

import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val FundoEdicao = Color(0xFFFAF6F0)
private val AzulEdicao = Color(0xFF193447)
private val CobreEdicao = Color(0xFF94613C)
private val CinzaEdicao = Color(0xFF53616A)
private val BordaEdicao = Color(0xFF89969E)
private val VermelhoEdicao = Color(0xFFB3261E)

private fun converterValorEdicao(texto: String): Double? {
    val limpo = texto
        .replace("R$", "")
        .trim()

    val normalizado = if (limpo.contains(",")) {
        limpo.replace(".", "").replace(",", ".")
    } else {
        limpo
    }

    return normalizado.toDoubleOrNull()
}

@Composable
fun EditarServicoScreen(
    servico: ServicoUi,
    onVoltar: () -> Unit,
    onSalvar: (ServicoUi) -> Unit
) {
    val context = LocalContext.current
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    var tipo by rememberSaveable(servico.id) {
        mutableStateOf(servico.titulo)
    }

    var descricao by rememberSaveable(servico.id) {
        mutableStateOf(servico.descricao)
    }

    var entrega by rememberSaveable(servico.id) {
        mutableStateOf(servico.entrega)
    }

    var status by rememberSaveable(servico.id) {
        mutableStateOf(servico.status)
    }

    var valor by rememberSaveable(servico.id) {
        mutableStateOf(
            servico.valorEstimado.toString().replace(".", ",")
        )
    }

    var fotoUri by rememberSaveable(servico.id) {
        mutableStateOf(servico.fotoUri)
    }

    var tentouSalvar by rememberSaveable(servico.id) {
        mutableStateOf(false)
    }

    var erroGeral by rememberSaveable(servico.id) {
        mutableStateOf("")
    }

    var menuTipos by remember {
        mutableStateOf(false)
    }

    var menuStatus by remember {
        mutableStateOf(false)
    }

    // Mantém o acesso persistente à foto selecionada.
    val seletorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val acessoPersistente = runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.isSuccess

            if (acessoPersistente) {
                fotoUri = uri.toString()
                erroGeral = ""
            } else {
                erroGeral =
                    "Não foi possível manter acesso à imagem. Escolha outra foto."
            }
        }
    }

    val hoje = remember {
        Calendar.getInstance()
    }

    val dataInicial = remember(entrega) {
        val partes = entrega.split("/")

        runCatching {
            Triple(
                partes[2].toInt(),
                partes[1].toInt() - 1,
                partes[0].toInt()
            )
        }.getOrElse {
            Triple(
                hoje.get(Calendar.YEAR),
                hoje.get(Calendar.MONTH),
                hoje.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    val valorNumero = converterValorEdicao(valor)

    val tipoInvalido =
        tentouSalvar && tipo.isBlank()

    val descricaoInvalida =
        tentouSalvar && descricao.isBlank()

    val entregaInvalida =
        tentouSalvar && entrega.isBlank()

    val valorInvalido =
        tentouSalvar && (
                valorNumero == null ||
                        !valorNumero.isFinite() ||
                        valorNumero <= 0.0 ||
                        valorNumero < servico.valorRecebido
                )

    val erroValor = when {
        valorNumero == null ||
                !valorNumero.isFinite() ||
                valorNumero <= 0.0 ->
            "Informe um valor estimado maior que zero."

        valorNumero < servico.valorRecebido ->
            "O valor estimado não pode ser menor que o total já recebido."

        else -> ""
    }

    fun salvarAlteracoes() {
        tentouSalvar = true

        erroGeral = when {
            tipo.isBlank() ->
                "Selecione o tipo de serviço."

            descricao.isBlank() ->
                "Informe a descrição do serviço."

            entrega.isBlank() ->
                "Selecione a data prevista de entrega."

            erroValor.isNotBlank() ->
                erroValor

            else -> ""
        }

        if (erroGeral.isNotEmpty()) {
            teclado?.hide()
            foco.clearFocus()
            return
        }

        val numeroValido = valorNumero ?: return

        teclado?.hide()
        foco.clearFocus()

        onSalvar(
            servico.copy(
                titulo = tipo,
                descricao = descricao.trim(),
                entrega = entrega,
                status = status,
                valorEstimado = numeroValido,
                fotoUri = fotoUri
            )
        )
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreEdicao,
        unfocusedBorderColor = BordaEdicao,
        focusedTextColor = AzulEdicao,
        unfocusedTextColor = AzulEdicao,
        focusedLabelColor = AzulEdicao,
        unfocusedLabelColor = CinzaEdicao,
        cursorColor = AzulEdicao,
        errorBorderColor = VermelhoEdicao,
        errorLabelColor = VermelhoEdicao
    )

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoEdicao)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        // CABEÇALHO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .background(AzulEdicao)
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
                    contentDescription = "Voltar aos detalhes do serviço",
                    tint = Color.White
                )
            }

            Text(
                text = "Editar serviço",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
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

            // FOTO
            Text(
                text = "Foto do serviço (opcional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AzulEdicao
            )

            Spacer(Modifier.height(9.dp))

            if (fotoUri.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        seletorFoto.launch(arrayOf("image/*"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 135.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        BordaEdicao
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulEdicao
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = CobreEdicao,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Adicionar foto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = "Escolher uma imagem do dispositivo",
                            fontSize = 12.sp,
                            color = CinzaEdicao,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        BordaEdicao
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        FotoServico(
                            fotoUri = fotoUri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Foto selecionada",
                            color = AzulEdicao,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    seletorFoto.launch(
                                        arrayOf("image/*")
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(9.dp),
                                border = BorderStroke(
                                    1.dp,
                                    BordaEdicao
                                ),
                                contentPadding = PaddingValues(6.dp)
                            ) {
                                Text(
                                    text = "Trocar foto",
                                    color = AzulEdicao,
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
                                shape = RoundedCornerShape(9.dp),
                                border = BorderStroke(
                                    1.dp,
                                    BordaEdicao
                                ),
                                contentPadding = PaddingValues(6.dp)
                            ) {
                                Text(
                                    text = "Remover foto",
                                    color = VermelhoEdicao,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // CLIENTE VINCULADO
            OutlinedTextField(
                value = servico.clienteNome,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Cliente vinculado")
                },
                supportingText = {
                    Text(
                        text = "O cliente vinculado não pode ser alterado aqui.",
                        color = CinzaEdicao,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            Spacer(Modifier.height(17.dp))

            // TIPO DE SERVIÇO
            Text(
                text = "Tipo de serviço *",
                color = AzulEdicao,
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
                                    error("Selecione o tipo de serviço.")
                                }
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(
                        1.dp,
                        if (tipoInvalido) {
                            VermelhoEdicao
                        } else {
                            BordaEdicao
                        }
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulEdicao
                    )
                ) {
                    Text(
                        text = tipo.ifBlank {
                            "Selecione o tipo"
                        },
                        color = AzulEdicao,
                        modifier = Modifier.weight(1f)
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
                    (tiposServico + tipo)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(item)
                                },
                                onClick = {
                                    tipo = item
                                    menuTipos = false
                                    erroGeral = ""
                                }
                            )
                        }
                }
            }

            if (tipoInvalido) {
                ErroCampoEdicao(
                    "Selecione o tipo de serviço."
                )
            }

            Spacer(Modifier.height(20.dp))

            // DESCRIÇÃO
            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                    erroGeral = ""
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
                ErroCampoEdicao(
                    "Informe a descrição do serviço."
                )
            }

            Spacer(Modifier.height(20.dp))

            // DATA DE ENTREGA
            Text(
                text = "Data prevista de entrega *",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedButton(
                onClick = {
                    teclado?.hide()
                    foco.clearFocus()

                    DatePickerDialog(
                        context,
                        { _, ano, mes, dia ->
                            entrega = "%02d/%02d/%04d".format(
                                dia,
                                mes + 1,
                                ano
                            )
                            erroGeral = ""
                        },
                        dataInicial.first,
                        dataInicial.second,
                        dataInicial.third
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .then(
                        if (entregaInvalida) {
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
                    if (entregaInvalida) {
                        VermelhoEdicao
                    } else {
                        BordaEdicao
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = AzulEdicao
                )
            ) {
                Text(
                    text = if (entrega.isBlank()) {
                        "Selecionar data"
                    } else {
                        "Entrega: $entrega"
                    },
                    modifier = Modifier.weight(1f),
                    color = AzulEdicao
                )

                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null
                )
            }

            if (entregaInvalida) {
                ErroCampoEdicao(
                    "Selecione a data prevista de entrega."
                )
            }

            Spacer(Modifier.height(20.dp))

            // SITUAÇÃO
            Text(
                text = "Situação do serviço",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Box {
                OutlinedButton(
                    onClick = {
                        menuStatus = true
                        teclado?.hide()
                        foco.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(
                        1.dp,
                        BordaEdicao
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulEdicao
                    )
                ) {
                    Text(
                        text = "Situação: $status",
                        modifier = Modifier.weight(1f),
                        color = AzulEdicao
                    )

                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = menuStatus,
                    onDismissRequest = {
                        menuStatus = false
                    }
                ) {
                    (
                            listOf(
                                "Orçamento",
                                "Em produção",
                                "Concluído",
                                "Cancelado"
                            ) + status
                            )
                        .filter { it.isNotBlank() }
                        .distinct()
                        .forEach { situacao ->
                            DropdownMenuItem(
                                text = {
                                    Text(situacao)
                                },
                                onClick = {
                                    status = situacao
                                    menuStatus = false
                                    erroGeral = ""
                                }
                            )
                        }
                }
            }

            Spacer(Modifier.height(20.dp))

            // VALOR ESTIMADO
            OutlinedTextField(
                value = valor,
                onValueChange = {
                    valor = it
                    erroGeral = ""
                },
                label = {
                    Text("Valor estimado (R$) *")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (valorInvalido) {
                            Modifier.semantics {
                                error(erroValor)
                            }
                        } else {
                            Modifier
                        }
                    ),
                singleLine = true,
                isError = valorInvalido,
                shape = RoundedCornerShape(9.dp),
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
                colors = coresCampo
            )

            if (valorInvalido && erroValor.isNotBlank()) {
                ErroCampoEdicao(erroValor)
            }

            Spacer(Modifier.height(9.dp))

            Text(
                text = "Já recebido: ${
                    moedaServico(servico.valorRecebido)
                }",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "O valor estimado não pode ser menor que o valor já recebido.",
                color = CinzaEdicao,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "* Campos obrigatórios",
                color = CinzaEdicao,
                fontSize = 12.sp
            )

            // MENSAGEM GERAL PARA LEITORES DE TELA
            if (erroGeral.isNotBlank()) {
                Spacer(Modifier.height(13.dp))

                Text(
                    text = erroGeral,
                    color = VermelhoEdicao,
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

        // AÇÕES INFERIORES
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
                border = BorderStroke(
                    1.dp,
                    BordaEdicao
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = AzulEdicao
                )
            ) {
                Text(
                    text = "Cancelar",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    salvarAlteracoes()
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulEdicao,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Salvar",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ErroCampoEdicao(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = VermelhoEdicao,
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
