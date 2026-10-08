
package com.nexo.marcenaria365.ui.screens.servicos

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import java.util.Calendar

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD906E)
private val Cinza = Color(0xFF929292)
private val Borda = Color(0xFFE8DFD4)

@Composable
fun NovoServicoScreen(
    clientes: List<ClienteUi>,
    onVoltar: () -> Unit,
    onSalvar: (ServicoUi) -> Unit
) {
    var clienteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var tipo by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var dataEntrega by rememberSaveable { mutableStateOf("") }
    var valor by rememberSaveable { mutableStateOf("") }
    var fotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var erro by rememberSaveable { mutableStateOf("") }
    var menuClientes by remember { mutableStateOf(false) }
    var menuTipos by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendario = Calendar.getInstance()

    val seletorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fotoUri = uri?.toString()
    }

    val seletorData = DatePickerDialog(
        context,
        { _, ano, mes, dia ->
            dataEntrega = "%02d/%02d/%04d".format(
                dia,
                mes + 1,
                ano
            )
        },
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH),
        calendario.get(Calendar.DAY_OF_MONTH)
    )

    val clienteSelecionado = clientes.find { it.id == clienteId }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Cobre,
        unfocusedBorderColor = Borda,
        focusedTextColor = Azul,
        unfocusedTextColor = Azul
    )

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Azul)
                .height(58.dp)
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
                text = "Novo serviço",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clickable {
                        seletorFoto.launch("image/*")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "Selecionar foto",
                    tint = Cinza,
                    modifier = Modifier.size(43.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (fotoUri == null) {
                        "Adicionar foto (opcional)"
                    } else {
                        "Foto selecionada • Toque para trocar"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Cinza
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Cliente",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            Box {
                OutlinedButton(
                    onClick = { menuClientes = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Azul
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Borda
                    )
                ) {
                    Text(
                        text = clienteSelecionado?.nome
                            ?: "Selecione o cliente",
                        modifier = Modifier.weight(1f),
                        color = if (clienteSelecionado == null) Cinza else Azul
                    )

                    Icon(Icons.Outlined.ExpandMore, null)
                }

                DropdownMenu(
                    expanded = menuClientes,
                    onDismissRequest = { menuClientes = false }
                ) {
                    clientes.filter { it.ativo }.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text(cliente.nome) },
                            onClick = {
                                clienteId = cliente.id
                                menuClientes = false
                                erro = ""
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Tipo de serviço",
                color = Azul,
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Azul
                    )
                ) {
                    Text(
                        text = tipo.ifEmpty { "Selecione o tipo" },
                        modifier = Modifier.weight(1f),
                        color = if (tipo.isEmpty()) Cinza else Azul
                    )

                    Icon(Icons.Outlined.ExpandMore, null)
                }

                DropdownMenu(
                    expanded = menuTipos,
                    onDismissRequest = { menuTipos = false }
                ) {
                    tiposServico.forEach { item ->
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

            Spacer(Modifier.height(18.dp))

            Text(
                "Descrição",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it; erro = "" },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 115.dp),
                placeholder = {
                    Text(
                        "Descreva o serviço...",
                        color = Cinza,
                        fontSize = 13.sp
                    )
                },
                minLines = 3,
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Data prevista de entrega",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedButton(
                onClick = { seletorData.show() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(9.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Azul
                )
            ) {
                Text(
                    dataEntrega.ifEmpty { "DD/MM/AAAA" },
                    color = if (dataEntrega.isEmpty()) Cinza else Azul,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "Selecionar data",
                    tint = Cinza
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Valor estimado",
                color = Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it; erro = "" },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("R$ 0,00", color = Cinza)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(9.dp),
                colors = coresCampo
            )

            if (erro.isNotBlank()) {
                Spacer(Modifier.height(12.dp))

                Text(
                    erro,
                    color = Color(0xFFB34D4D),
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(28.dp))
        }

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
                border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Azul,
                    containerColor = Color.White
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val numero = valor
                        .replace(".", "")
                        .replace(",", ".")
                        .replace("R$", "")
                        .trim()
                        .toDoubleOrNull()

                    erro = when {
                        clienteSelecionado == null ->
                            "Selecione um cliente."

                        tipo.isBlank() ->
                            "Selecione o tipo de serviço."

                        descricao.isBlank() ->
                            "Informe a descrição."

                        dataEntrega.isBlank() ->
                            "Selecione a data de entrega."

                        numero == null || numero <= 0.0 ->
                            "Informe um valor estimado válido."

                        else -> {
                            onSalvar(
                                ServicoUi(
                                    id = System.currentTimeMillis(),
                                    clienteId = clienteSelecionado.id,
                                    clienteNome = clienteSelecionado.nome,
                                    titulo = tipo,
                                    descricao = descricao.trim(),
                                    entrega = dataEntrega,
                                    valorEstimado = numero,
                                    status = "Orçamento",
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
                    containerColor = Azul,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Salvar",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
