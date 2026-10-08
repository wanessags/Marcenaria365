
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            titulo = "Editar cliente",
            subtitulo = "Atualize as informações do cadastro.",
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

        Spacer(Modifier.height(22.dp))

        ClienteTituloSecao("Situação do cliente")

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
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Cliente ativo",
                        color = ClienteCores.Azul,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = if (ativo) {
                            "Disponível para novos serviços."
                        } else {
                            "Cliente marcado como inativo."
                        },
                        color = ClienteCores.Cinza,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = ativo,
                    onCheckedChange = { ativo = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = ClienteCores.Azul
                    )
                )
            }
        }

        if (erro.isNotEmpty()) {
            Spacer(Modifier.height(15.dp))

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
                            cliente.copy(
                                nome = nome.trim(),
                                telefone = telefone.trim(),
                                email = emailLimpo,
                                ativo = ativo
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
                "Salvar alterações",
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
