
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
import androidx.compose.ui.Alignment
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

private val CinzaEdicao = Color(0xFF53616A)
private val BordaEdicao = Color(0xFF89969E)
private val VermelhoEdicao = Color(0xFFB3261E)
private val VerdeEdicao = Color(0xFF237A58)

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

    var tentouSalvar by rememberSaveable(cliente.id) {
        mutableStateOf(false)
    }

    var mostrarConfirmacao by remember {
        mutableStateOf(false)
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

    fun validarAlteracoes() {
        tentouSalvar = true

        erro = when {
            nome.isBlank() ->
                "Informe o nome do cliente."

            numerosTelefone.length !in 10..11 ->
                "Informe um telefone válido com DDD."

            emailInvalido ->
                "Informe um e-mail válido."

            else -> ""
        }

        if (erro.isEmpty()) {
            teclado?.hide()
            foco.clearFocus()
            mostrarConfirmacao = true
        }
    }

    BackHandler(
        enabled = !mostrarConfirmacao
    ) {
        onVoltar()
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ClienteCores.Azul,
        unfocusedTextColor = ClienteCores.Azul,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = ClienteCores.Cobre,
        unfocusedBorderColor = BordaEdicao,
        focusedLabelColor = ClienteCores.Azul,
        unfocusedLabelColor = CinzaEdicao,
        focusedLeadingIconColor = ClienteCores.Azul,
        unfocusedLeadingIconColor = CinzaEdicao,
        cursorColor = ClienteCores.Cobre,
        errorBorderColor = VermelhoEdicao,
        errorLabelColor = VermelhoEdicao
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

        // CABEÇALHO ORIGINAL
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

        // DADOS DO CLIENTE
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
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                // NOME COMPLETO
                Column {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            erro = ""
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
                                modifier = Modifier.size(20.dp)
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
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = coresCampo,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = ClienteCores.Azul
                        )
                    )

                    if (nomeInvalido) {
                        ErroEdicaoCliente(
                            mensagem =
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
                            erro = ""
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
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        supportingText = {
                            Text(
                                text = "Ex.: 11987654321",
                                color = CinzaEdicao,
                                fontSize = 12.sp
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
                            fontSize = 14.sp,
                            color = ClienteCores.Azul
                        )
                    )

                    if (telefoneInvalido) {
                        ErroEdicaoCliente(
                            mensagem =
                                "Informe um telefone válido com DDD."
                        )
                    }
                }

                // E-MAIL OPCIONAL
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            erro = ""
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
                                modifier = Modifier.size(20.dp)
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
                            fontSize = 14.sp,
                            color = ClienteCores.Azul
                        )
                    )

                    if (emailInvalido) {
                        ErroEdicaoCliente(
                            mensagem =
                                "Informe um e-mail válido."
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "* Campos obrigatórios",
            fontSize = 12.sp,
            color = CinzaEdicao,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(Modifier.height(22.dp))

        // SITUAÇÃO DO CLIENTE
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
                        text = if (ativo) {
                            "Cliente ativo"
                        } else {
                            "Cliente inativo"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClienteCores.Azul
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = if (ativo) {
                            "Disponível para novos serviços."
                        } else {
                            "Marcado como inativo."
                        },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = CinzaEdicao
                    )
                }

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = ativo,
                    onCheckedChange = {
                        ativo = it
                    },
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    ),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ClienteCores.Azul,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = BordaEdicao,
                        uncheckedBorderColor = CinzaEdicao
                    )
                )
            }
        }

        // ERRO GERAL
        if (erro.isNotBlank()) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = erro,
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

        // SALVAR ALTERAÇÕES
        Button(
            onClick = {
                validarAlteracoes()
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
                text = "Salvar alterações",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
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
                color = BordaEdicao
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

    // NOVO DESIGN DA CONFIRMAÇÃO
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacao = false
            },

            title = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Salvar alterações?",
                        color = ClienteCores.Azul,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )

                    Spacer(Modifier.height(7.dp))

                    Text(
                        text = "Confira os dados antes de confirmar.",
                        color = CinzaEdicao,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            },

            text = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ClienteCores.Fundo,
                    border = BorderStroke(
                        width = 1.dp,
                        color = ClienteCores.Borda
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        DadoConfirmacaoEdicao(
                            rotulo = "Nome completo",
                            valor = nome.trim()
                        )

                        HorizontalDivider(
                            color = ClienteCores.Borda
                        )

                        DadoConfirmacaoEdicao(
                            rotulo = "Telefone",
                            valor = telefone.trim()
                        )

                        HorizontalDivider(
                            color = ClienteCores.Borda
                        )

                        DadoConfirmacaoEdicao(
                            rotulo = "E-mail",
                            valor = emailLimpo.ifBlank {
                                "Não informado"
                            }
                        )

                        HorizontalDivider(
                            color = ClienteCores.Borda
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Situação",
                                color = CinzaEdicao,
                                fontSize = 12.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ativo) {
                                    Color(0xFFE5F4EC)
                                } else {
                                    Color(0xFFF1EEEE)
                                }
                            ) {
                                Text(
                                    text = if (ativo) {
                                        "Ativo"
                                    } else {
                                        "Inativo"
                                    },
                                    color = if (ativo) {
                                        VerdeEdicao
                                    } else {
                                        ClienteCores.Azul
                                    },
                                    fontSize = 12.sp,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }
                    }
                }
            },

            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {
                    Button(
                        onClick = {
                            mostrarConfirmacao = false

                            onSalvar(
                                cliente.copy(
                                    nome = nome.trim(),
                                    telefone = telefone.trim(),
                                    email = emailLimpo,
                                    ativo = ativo
                                )
                            )
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
                            text = "Confirmar alterações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            mostrarConfirmacao = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(11.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = ClienteCores.Borda
                        ),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = ClienteCores.Azul
                            )
                    ) {
                        Text(
                            text = "Corrigir",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },

            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DadoConfirmacaoEdicao(
    rotulo: String,
    valor: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = rotulo,
            color = CinzaEdicao,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Text(
            text = valor,
            color = ClienteCores.Azul,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ErroEdicaoCliente(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = VermelhoEdicao,
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
