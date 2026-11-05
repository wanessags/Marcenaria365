
package com.nexo.marcenaria365.ui.screens.clientes

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CinzaAcessivelCliente = Color(0xFF53616A)
private val BordaAcessivelCliente = Color(0xFF89969E)

@Composable
fun ClienteCadastroScreen(
    onVoltar: () -> Unit,
    onSalvar: (ClienteUi) -> Unit
) {
    var nome by rememberSaveable {
        mutableStateOf("")
    }

    var telefone by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var tentouSalvar by rememberSaveable {
        mutableStateOf(false)
    }

    var erroGeral by rememberSaveable {
        mutableStateOf("")
    }

    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val numerosTelefone = telefone.filter {
        it.isDigit()
    }

    val emailLimpo = email.trim()

    val nomeInvalido =
        tentouSalvar && nome.isBlank()

    val telefoneInvalido =
        tentouSalvar &&
                numerosTelefone.length !in 10..11

    val emailInvalido =
        emailLimpo.isNotEmpty() &&
                !Patterns.EMAIL_ADDRESS
                    .matcher(emailLimpo)
                    .matches()

    fun salvarCliente() {
        tentouSalvar = true

        erroGeral = when {
            nome.isBlank() ->
                "Informe o nome do cliente."

            numerosTelefone.length !in 10..11 ->
                "Informe um telefone válido com DDD."

            emailInvalido ->
                "Informe um e-mail válido."

            else -> ""
        }

        if (erroGeral.isNotEmpty()) {
            return
        }

        teclado?.hide()
        foco.clearFocus()

        onSalvar(
            ClienteUi(
                id = System.currentTimeMillis(),
                nome = nome.trim(),
                telefone = telefone.trim(),
                email = emailLimpo,
                ativo = true
            )
        )
    }

    BackHandler(onBack = onVoltar)

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ClienteCores.Azul,
        unfocusedTextColor = ClienteCores.Azul,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = ClienteCores.Cobre,
        unfocusedBorderColor = BordaAcessivelCliente,
        focusedLabelColor = ClienteCores.Azul,
        unfocusedLabelColor = CinzaAcessivelCliente,
        focusedLeadingIconColor = ClienteCores.Azul,
        unfocusedLeadingIconColor = CinzaAcessivelCliente,
        cursorColor = ClienteCores.Cobre,
        errorBorderColor = ClienteCores.Vermelho,
        errorLabelColor = ClienteCores.Vermelho
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClienteCores.Fundo)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
            .imePadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // CABECALHO ORIGINAL
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
                verticalArrangement = Arrangement.spacedBy(
                    12.dp
                )
            ) {

                // NOME
                Column {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            erroGeral = ""
                        },
                        label = {
                            Text(
                                text = "Nome completo *",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = ClienteCores.Cobre,
                                modifier = Modifier.size(
                                    20.dp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (nomeInvalido) {
                                    Modifier.semantics {
                                        error(
                                            "Informe o nome do cliente."
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        singleLine = true,
                        isError = nomeInvalido,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = coresCampo,
                        textStyle = TextStyle(
                            color = ClienteCores.Azul,
                            fontSize = 14.sp
                        )
                    )

                    if (nomeInvalido) {
                        ErroCampoCliente(
                            "Informe o nome do cliente."
                        )
                    }
                }

                // TELEFONE
                Column {
                    OutlinedTextField(
                        value = telefone,
                        onValueChange = {
                            telefone = it
                            erroGeral = ""
                        },
                        label = {
                            Text(
                                text = "Telefone com DDD *",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = ClienteCores.Cobre,
                                modifier = Modifier.size(
                                    20.dp
                                )
                            )
                        },
                        supportingText = {
                            Text(
                                text = "Ex.: 11987654321",
                                fontSize = 12.sp,
                                color = CinzaAcessivelCliente
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (telefoneInvalido) {
                                    Modifier.semantics {
                                        error(
                                            "Informe um telefone válido com DDD."
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        singleLine = true,
                        isError = telefoneInvalido,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = coresCampo,
                        textStyle = TextStyle(
                            color = ClienteCores.Azul,
                            fontSize = 14.sp
                        )
                    )

                    if (telefoneInvalido) {
                        ErroCampoCliente(
                            "Informe um telefone válido com DDD."
                        )
                    }
                }

                // E-MAIL
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            erroGeral = ""
                        },
                        label = {
                            Text(
                                text = "E-mail (opcional)",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Email,
                                contentDescription = null,
                                tint = ClienteCores.Cobre,
                                modifier = Modifier.size(
                                    20.dp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (emailInvalido) {
                                    Modifier.semantics {
                                        error(
                                            "Informe um e-mail válido."
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        singleLine = true,
                        isError = emailInvalido,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                teclado?.hide()
                                foco.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = coresCampo,
                        textStyle = TextStyle(
                            color = ClienteCores.Azul,
                            fontSize = 14.sp
                        )
                    )

                    if (emailInvalido) {
                        ErroCampoCliente(
                            "Informe um e-mail válido."
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "* Campos obrigatórios",
            color = CinzaAcessivelCliente,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        // MENSAGEM GERAL ACESSIVEL
        if (erroGeral.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))

            Text(
                text = erroGeral,
                color = ClienteCores.Vermelho,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )
        }

        Spacer(Modifier.height(28.dp))

        // SALVAR
        Button(
            onClick = {
                salvarCliente()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp),
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

        // CANCELAR
        OutlinedButton(
            onClick = onVoltar,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(
                width = 1.dp,
                color = BordaAcessivelCliente
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

@Composable
private fun ErroCampoCliente(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = ClienteCores.Vermelho,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
            }
    )
}
