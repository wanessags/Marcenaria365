
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClienteCadastroScreen(
    onVoltar: () -> Unit,
    onSalvar: (ClienteUi) -> Unit
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf("") }

    BackHandler(onBack = onVoltar)

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
            titulo = "Novo cliente",
            subtitulo = "Cadastre os dados de contato.",
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
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = ClienteCores.Azul,
                        fontSize = 14.sp
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
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = ClienteCores.Azul,
                        fontSize = 14.sp
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
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = ClienteCores.Azul,
                        fontSize = 14.sp
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "* Campos obrigatórios",
            color = ClienteCores.Cinza,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        if (erro.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))

            Text(
                text = erro,
                color = ClienteCores.Vermelho,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(28.dp))

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
                        onSalvar(
                            ClienteUi(
                                id = System.currentTimeMillis(),
                                nome = nome.trim(),
                                telefone = telefone.trim(),
                                email = emailLimpo,
                                ativo = true
                            )
                        )
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
                text = "Salvar cliente",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
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
                width = 1.dp,
                color = ClienteCores.Borda
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
}
