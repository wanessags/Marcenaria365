
package com.nexo.marcenaria365.ui.screens.servicos

import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val FundoEdicao = Color(0xFFFAF6F0)
private val AzulEdicao = Color(0xFF193447)
private val CobreEdicao = Color(0xFFBD906E)
private val CinzaEdicao = Color(0xFF929292)
private val BordaEdicao = Color(0xFFE8DFD4)
private val VermelhoEdicao = Color(0xFFB34D4D)

@Composable
fun EditarServicoScreen(
    servico: ServicoUi,
    onVoltar: () -> Unit,
    onSalvar: (ServicoUi) -> Unit
) {
    val context = LocalContext.current

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

    var erro by rememberSaveable {
        mutableStateOf("")
    }

    var menuTipos by remember {
        mutableStateOf(false)
    }

    var menuStatus by remember {
        mutableStateOf(false)
    }

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
                erro = ""
            } else {
                erro = "Não foi possível manter acesso à imagem. " +
                        "Escolha outra foto."
            }
        }
    }

    val hoje = remember {
        Calendar.getInstance()
    }

    val dataInicial = remember(servico.entrega) {
        val partes = servico.entrega.split("/")

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

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreEdicao,
        unfocusedBorderColor = BordaEdicao,
        focusedTextColor = AzulEdicao,
        unfocusedTextColor = AzulEdicao
    )

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoEdicao)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(AzulEdicao)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Text(
                text = "Editar serviço",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
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

            // FOTO: PERMITE ADICIONAR DEPOIS DO CADASTRO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .clickable {
                        seletorFoto.launch(arrayOf("image/*"))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!fotoUri.isNullOrBlank()) {
                    FotoServico(
                        fotoUri = fotoUri,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Adicionar foto",
                            tint = CinzaEdicao,
                            modifier = Modifier.size(42.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Adicionar foto do serviço",
                            color = CinzaEdicao,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (!fotoUri.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            seletorFoto.launch(arrayOf("image/*"))
                        }
                    ) {
                        Text("Trocar foto", color = CobreEdicao)
                    }

                    TextButton(
                        onClick = {
                            fotoUri = null
                        }
                    ) {
                        Text("Remover foto", color = VermelhoEdicao)
                    }
                }
            }

            Spacer(Modifier.height(17.dp))

            // CLIENTE VINCULADO - NÃO EDITÁVEL
            Text(
                text = "Cliente",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value = servico.clienteNome,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = "O cliente vinculado não pode ser alterado aqui.",
                color = CinzaEdicao,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(20.dp))

            // TIPO DE SERVIÇO
            Text(
                text = "Tipo de serviço",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Box {
                OutlinedButton(
                    onClick = { menuTipos = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.dp, BordaEdicao),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulEdicao
                    )
                ) {
                    Text(
                        text = tipo.ifBlank { "Selecione o tipo" },
                        color = AzulEdicao,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(Icons.Outlined.ExpandMore, null)
                }

                DropdownMenu(
                    expanded = menuTipos,
                    onDismissRequest = {
                        menuTipos = false
                    }
                ) {
                    (tiposServico + tipo)
                        .distinct()
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    tipo = item
                                    menuTipos = false
                                    erro = ""
                                }
                            )
                        }
                }
            }

            Spacer(Modifier.height(20.dp))

            // DESCRIÇÃO
            Text(
                text = "Descrição",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                    erro = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 115.dp),
                minLines = 3,
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            Spacer(Modifier.height(20.dp))

            // PRAZO
            Text(
                text = "Data prevista de entrega",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, ano, mes, dia ->
                            entrega = "%02d/%02d/%04d".format(
                                dia,
                                mes + 1,
                                ano
                            )
                            erro = ""
                        },
                        dataInicial.first,
                        dataInicial.second,
                        dataInicial.third
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(1.dp, BordaEdicao),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = AzulEdicao
                )
            ) {
                Text(
                    text = entrega.ifBlank { "DD/MM/AAAA" },
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Alterar data"
                )
            }

            Spacer(Modifier.height(20.dp))

            // STATUS
            Text(
                text = "Situação do serviço",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Box {
                OutlinedButton(
                    onClick = { menuStatus = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(1.dp, BordaEdicao),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulEdicao
                    )
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(Icons.Outlined.ExpandMore, null)
                }

                DropdownMenu(
                    expanded = menuStatus,
                    onDismissRequest = {
                        menuStatus = false
                    }
                ) {
                    listOf(
                        "Orçamento",
                        "Em produção",
                        "Concluído",
                        "Cancelado"
                    ).forEach { situacao ->
                        DropdownMenuItem(
                            text = { Text(situacao) },
                            onClick = {
                                status = situacao
                                menuStatus = false
                                erro = ""
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // VALOR ESTIMADO
            Text(
                text = "Valor estimado",
                color = AzulEdicao,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value = valor,
                onValueChange = {
                    valor = it
                    erro = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(9.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                colors = coresCampo
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Já recebido: ${
                    moedaServico(servico.valorRecebido)
                }",
                color = CinzaEdicao,
                fontSize = 12.sp
            )

            Text(
                text = "O valor estimado não pode ser menor " +
                        "que o valor já recebido.",
                color = CinzaEdicao,
                fontSize = 11.sp
            )

            if (erro.isNotBlank()) {
                Spacer(Modifier.height(15.dp))

                Text(
                    text = erro,
                    color = VermelhoEdicao,
                    fontSize = 12.sp
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
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BordaEdicao),
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
                    val numero = valor
                        .replace("R$", "")
                        .replace(".", "")
                        .replace(",", ".")
                        .trim()
                        .toDoubleOrNull()

                    erro = when {
                        tipo.isBlank() ->
                            "Selecione um tipo de serviço."

                        descricao.isBlank() ->
                            "Informe a descrição."

                        entrega.isBlank() ->
                            "Selecione a data de entrega."

                        numero == null || numero <= 0.0 ->
                            "Informe um valor válido."

                        numero < servico.valorRecebido ->
                            "O valor não pode ser menor " +
                                    "que o total já recebido."

                        else -> {
                            onSalvar(
                                servico.copy(
                                    titulo = tipo,
                                    descricao = descricao.trim(),
                                    entrega = entrega,
                                    status = status,
                                    valorEstimado = numero,
                                    fotoUri = fotoUri
                                )
                            )
                            ""
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulEdicao,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Salvar alterações",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
