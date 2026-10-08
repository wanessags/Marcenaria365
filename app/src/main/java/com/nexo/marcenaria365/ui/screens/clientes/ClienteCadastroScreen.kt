
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
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = ClienteCores.Cobre,
        unfocusedBorderColor = ClienteCores.Borda,
        focusedLabelColor = ClienteCores.Azul
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
            subtitulo = "Preencha as informações para cadastrar.",
            onVoltar = onVoltar
        )

        Spacer(Modifier.height(28.dp))

        ClienteTituloSecao("Dados do cliente")

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erro = ""
                    },
                    label = { Text("Nome completo *") },
                    placeholder = { Text("Ex.: Ana Silva") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint = ClienteCores.Cinza
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coresCampo
                )

                OutlinedTextField(
                    value = telefone,
                    onValueChange = {
                        telefone = it
                        erro = ""
                    },
                    label = { Text("Telefone *") },
                    placeholder = { Text("(11) 99999-9999") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = ClienteCores.Cinza
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coresCampo
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        erro = ""
                    },
                    label = { Text("E-mail (opcional)") },
                    placeholder = { Text("cliente@email.com") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            tint = ClienteCores.Cinza
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = coresCampo
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "* Campos obrigatórios",
            fontSize = 11.sp,
            color = ClienteCores.Cinza
        )

        if (erro.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = erro,
                fontSize = 12.sp,
                color = ClienteCores.Vermelho
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                val numeros = telefone.filter { it.isDigit() }
                val emailLimpo = email.trim()

                erro = when {
                    nome.isBlank() ->
                        "Informe o nome do cliente."

                    numeros.length !in 10..11 ->
                        "Informe um telefone com DDD válido."

                    emailLimpo.isNotEmpty() &&
                            !Patterns.EMAIL_ADDRESS.matcher(
                                emailLimpo
                            ).matches() ->
                        "Informe um e-mail válido."

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
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClienteCores.Azul
            )
        ) {
            Text(
                "Salvar cliente",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                ClienteCores.Borda
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ClienteCores.Azul
            )
        ) {
            Text("Cancelar")
        }

        Spacer(Modifier.height(28.dp))
    }
}
