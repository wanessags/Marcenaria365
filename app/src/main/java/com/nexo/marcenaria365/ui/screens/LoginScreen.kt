
package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.R
import com.nexo.marcenaria365.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FundoLogin = Color(0xFFFAF6F0)
private val AzulLogin = Color(0xFF193447)
private val CobreLogin = Color(0xFF94613C)
private val CinzaLogin = Color(0xFF53616A)
private val BordaLogin = Color(0xFF9AA6AD)
private val VermelhoLogin = Color(0xFFB3261E)

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onDemoLogin: (String) -> Unit
) {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    var senha by rememberSaveable {
        mutableStateOf("")
    }

    var senhaVisivel by rememberSaveable {
        mutableStateOf(false)
    }

    var mensagem by rememberSaveable {
        mutableStateOf("")
    }

    var carregando by remember {
        mutableStateOf(false)
    }

    var tentouEntrar by rememberSaveable {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val emailVazio = tentouEntrar && email.isBlank()

    val emailInvalido = tentouEntrar &&
            email.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()

    val senhaVazia = tentouEntrar && senha.isBlank()

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreLogin,
        unfocusedBorderColor = BordaLogin,
        focusedTextColor = AzulLogin,
        unfocusedTextColor = AzulLogin,
        focusedLabelColor = AzulLogin,
        unfocusedLabelColor = CinzaLogin,
        focusedLeadingIconColor = AzulLogin,
        unfocusedLeadingIconColor = CinzaLogin,
        focusedTrailingIconColor = AzulLogin,
        unfocusedTrailingIconColor = CinzaLogin,
        cursorColor = AzulLogin,
        errorBorderColor = VermelhoLogin,
        errorLabelColor = VermelhoLogin
    )

    fun entrar() {
        if (carregando) {
            return
        }

        tentouEntrar = true

        mensagem = when {
            email.isBlank() ->
                "Informe seu e-mail."

            !Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches() ->
                "Informe um e-mail válido."

            senha.isBlank() ->
                "Informe sua senha."

            else -> ""
        }

        if (mensagem.isNotBlank()) {
            return
        }

        teclado?.hide()
        foco.clearFocus()

        carregando = true

        scope.launch {
            val resultado = withContext(Dispatchers.IO) {
                AuthRepository.login(
                    email = email.trim(),
                    senha = senha
                )
            }

            carregando = false

            resultado.fold(
                onSuccess = { sessao ->
                    senha = ""
                    onDemoLogin(sessao.usuario.nome)
                },
                onFailure = { erro ->
                    mensagem = erro.message
                        ?: "Não foi possível entrar. Tente novamente."
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoLogin)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 25.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // VOLTAR
        IconButton(
            onClick = onBackClick,
            enabled = !carregando,
            modifier = Modifier.sizeIn(
                minWidth = 48.dp,
                minHeight = 48.dp
            )
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = AzulLogin
            )
        }

        Spacer(Modifier.height(20.dp))

        // LOGO
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Marcenaria 365",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        )

        Spacer(Modifier.height(8.dp))

        // TITULO
        Text(
            text = "Bem-vindo de volta!",
            color = AzulLogin,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Acesse sua conta para continuar.",
            color = CinzaLogin,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(30.dp))

        // CAMPO E-MAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                if (!carregando) {
                    email = it
                    mensagem = ""
                }
            },
            label = {
                Text("E-mail")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null
                )
            },
            singleLine = true,
            readOnly = carregando,
            isError = emailVazio || emailInvalido,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (emailVazio || emailInvalido) {
                        Modifier.semantics {
                            error(
                                if (emailVazio) {
                                    "Informe seu e-mail."
                                } else {
                                    "Informe um e-mail válido."
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        if (emailVazio || emailInvalido) {
            Spacer(Modifier.height(5.dp))

            Text(
                text = if (emailVazio) {
                    "Informe seu e-mail."
                } else {
                    "Informe um e-mail válido."
                },
                color = VermelhoLogin,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(15.dp))

        // CAMPO SENHA
        OutlinedTextField(
            value = senha,
            onValueChange = {
                if (!carregando) {
                    senha = it
                    mensagem = ""
                }
            },
            label = {
                Text("Senha")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        senhaVisivel = !senhaVisivel
                    },
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                ) {
                    Icon(
                        imageVector = if (senhaVisivel) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (senhaVisivel) {
                            "Ocultar senha"
                        } else {
                            "Mostrar senha"
                        },
                        tint = CinzaLogin
                    )
                }
            },
            visualTransformation = if (senhaVisivel) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            singleLine = true,
            readOnly = carregando,
            isError = senhaVazia,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    entrar()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (senhaVazia) {
                        Modifier.semantics {
                            error("Informe sua senha.")
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        if (senhaVazia) {
            Spacer(Modifier.height(5.dp))

            Text(
                text = "Informe sua senha.",
                fontSize = 12.sp,
                color = VermelhoLogin,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(10.dp))

        // RECUPERACAO DE SENHA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    mensagem =
                        "A recuperação de senha ainda não está disponível."
                },
                enabled = !carregando,
                modifier = Modifier.heightIn(
                    min = 48.dp
                )
            ) {
                Text(
                    text = "Esqueceu sua senha?",
                    color = AzulLogin,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // BOTAO ENTRAR
        Button(
            onClick = {
                entrar()
            },
            enabled = !carregando,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulLogin,
                contentColor = Color.White,
                disabledContainerColor = AzulLogin,
                disabledContentColor = Color.White
            )
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Entrando...",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Entrar",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // STATUS DE CARREGAMENTO
        if (carregando) {
            Spacer(Modifier.height(15.dp))

            Text(
                text = "Conectando à sua conta...",
                color = AzulLogin,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )
        }

        // MENSAGENS DE ERRO E AVISO
        if (mensagem.isNotBlank()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensagem,
                color = VermelhoLogin,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )
        }

        Spacer(Modifier.height(65.dp))

        // LINK PARA CADASTRO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ainda não possui conta?",
                fontSize = 12.sp,
                color = CinzaLogin
            )

            Spacer(Modifier.width(4.dp))

            TextButton(
                onClick = onRegisterClick,
                enabled = !carregando,
                modifier = Modifier.heightIn(
                    min = 48.dp
                )
            ) {
                Text(
                    text = "Cadastre-se",
                    fontSize = 13.sp,
                    color = CobreLogin,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
