
package com.nexo.marcenaria365.ui.screens.clientes

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClienteEdicaoScreen(
    cliente: ClienteUi,
    onVoltar: () -> Unit,
    onSalvar: (ClienteUi) -> Unit
) {
    var nome by rememberSaveable(cliente.id) {
        mutableStateOf(cliente.nome)
    }

    var telefone by rememberSaveable(cliente.id) {
        mutableStateOf(cliente.telefone)
    }

    var email by rememberSaveable(cliente.id) {
        mutableStateOf(cliente.email)
    }

    var ativo by rememberSaveable(cliente.id) {
        mutableStateOf(cliente.ativo)
    }

    var erro by rememberSaveable(cliente.id) {
        mutableStateOf("")
    }

    var mostrarConfirmacao by remember {
        mutableStateOf(false)
    }

    BackHandler(enabled = !mostrarConfirmacao) {
        onVoltar()
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ClienteCores.Azul,
        unfocusedTextColor = ClienteCores.Azul,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = ClienteCores.Cobre,
        unfocusedBorderColor = ClienteCores.Borda,
        focusedLabelColor = ClienteCores.Azul,
        unfocusedLabelColor = ClienteCores.Cinza,
        cursorColor = ClienteCores.Cobre
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClienteCores.Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        ClienteCabecalho(
            titulo = "Editar cliente",
            subtitulo = "Atualize os dados de contato.",
            onVoltar = onVoltar
        )

        Spacer(Modifier.height(26.dp))

        ClienteTituloSecao(
            titulo = "Informações pessoais"
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erro = ""
                    },
                    label = {
                        Text(
                            "Nome completo *",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint = ClienteCores.Cobre,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = coresCampo,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = ClienteCores.Azul
                    )
                )

                OutlinedTextField(
                    value = telefone,
                    onValueChange = {
                        telefone = it
                        erro = ""
                    },
                    label = {
                        Text(
                            "Telefone *",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = ClienteCores.Cobre,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = coresCampo,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = ClienteCores.Azul
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        erro = ""
                    },
                    label = {
                        Text(
                            "E-mail (opcional)",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = ClienteCores.Cobre,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = coresCampo,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = ClienteCores.Azul
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "* Campos obrigatórios",
            fontSize = 11.sp,
            color = ClienteCores.Cinza,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(Modifier.height(22.dp))

        ClienteTituloSecao(
            titulo = "Situação do cliente"
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cliente ativo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClienteCores.Azul
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = if (ativo) {
                            "Disponível para novos serviços."
                        } else {
                            "Cliente marcado como inativo."
                        },
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = ClienteCores.Cinza
                    )
                }

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = ativo,
                    onCheckedChange = {
                        ativo = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ClienteCores.Azul,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = ClienteCores.Borda,
                        uncheckedBorderColor = ClienteCores.Cinza
                    )
                )
            }
        }

        if (erro.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = erro,
                color = ClienteCores.Vermelho,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = {
                val numeros = telefone.filter { it.isDigit() }
                val emailLimpo = email.trim()

                erro = when {
                    nome.isBlank() -> {
                        "Informe o nome do cliente."
                    }

                    numeros.length !in 10..11 -> {
                        "Informe um telefone válido com DDD."
                    }

                    emailLimpo.isNotEmpty() &&
                            !Patterns.EMAIL_ADDRESS.matcher(
                                emailLimpo
                            ).matches() -> {
                        "Informe um e-mail válido."
                    }

                    else -> {
                        mostrarConfirmacao = true
                        ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClienteCores.Azul,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Salvar alterações",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(
                1.dp,
                ClienteCores.Borda
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = ClienteCores.Azul
            )
        ) {
            Text(
                text = "Cancelar",
                color = ClienteCores.Azul,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    // Confirmação antes de salvar
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacao = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = ClienteCores.Verde
                )
            },
            title = {
                Text(
                    text = "Salvar alterações?",
                    color = ClienteCores.Azul,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja salvar as alterações realizadas " +
                            "no cadastro deste cliente?",
                    color = ClienteCores.Cinza
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacao = false

                        onSalvar(
                            cliente.copy(
                                nome = nome.trim(),
                                telefone = telefone.trim(),
                                email = email.trim(),
                                ativo = ativo
                            )
                        )
                    }
                ) {
                    Text(
                        text = "Confirmar",
                        color = ClienteCores.Azul,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacao = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = ClienteCores.Cinza
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
