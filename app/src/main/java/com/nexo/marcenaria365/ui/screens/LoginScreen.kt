
package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
private val CobreLogin = Color(0xFFBD8B60)
private val CinzaLogin = Color(0xFF53616A)
private val BordaLogin = Color(0xFFABB5BA)
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

    val scope = rememberCoroutineScope()

    // Campos permanecem legiveis durante a requisicao.
    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreLogin,
        unfocusedBorderColor = BordaLogin,
        focusedTextColor = AzulLogin,
        unfocusedTextColor = AzulLogin,
        focusedPlaceholderColor = CinzaLogin,
        unfocusedPlaceholderColor = CinzaLogin,
        focusedLeadingIconColor = CinzaLogin,
        unfocusedLeadingIconColor = CinzaLogin,
        focusedTrailingIconColor = CinzaLogin,
        unfocusedTrailingIconColor = CinzaLogin,
        cursorColor = CobreLogin
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoLogin)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp)
    ) {

        Spacer(Modifier.height(10.dp))

        IconButton(
            onClick = onBackClick,
            enabled = !carregando
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = AzulLogin
            )
        }

        Spacer(Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Marcenaria 365",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Bem-vindo de volta!",
            color = AzulLogin,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Acesse sua conta para continuar.",
            color = CinzaLogin,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(36.dp))

        // E-MAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                if (!carregando) {
                    email = it
                    mensagem = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "E-mail",
                    color = CinzaLogin
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = CinzaLogin
                )
            },
            singleLine = true,
            readOnly = carregando,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        // SENHA
        OutlinedTextField(
            value = senha,
            onValueChange = {
                if (!carregando) {
                    senha = it
                    mensagem = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Senha",
                    color = CinzaLogin
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = CinzaLogin
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        senhaVisivel = !senhaVisivel
                    }
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
            singleLine = true,
            readOnly = carregando,
            visualTransformation = if (senhaVisivel) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        // RECUPERACAO DE SENHA
        Text(
            text = "Esqueceu sua senha?",
            fontSize = 12.sp,
            color = AzulLogin,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !carregando) {
                    mensagem =
                        "A recuperação de senha ainda não está disponível."
                }
        )

        Spacer(Modifier.height(25.dp))

        // BOTAO ENTRAR
        Button(
            onClick = {
                mensagem = when {
                    email.isBlank() || senha.isBlank() ->
                        "Preencha o e-mail e a senha."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim())
                        .matches() ->
                        "Digite um e-mail válido."

                    else -> ""
                }

                if (mensagem.isEmpty() && !carregando) {
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
            },
            enabled = !carregando,
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulLogin,
                contentColor = Color.White,
                disabledContainerColor = AzulLogin,
                disabledContentColor = Color.White
            )
        ) {
            if (carregando) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
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
                }
            } else {
                Text(
                    text = "Entrar",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // INFORMACAO DURANTE O CARREGAMENTO
        if (carregando) {
            Spacer(Modifier.height(15.dp))

            Text(
                text = "Conectando à sua conta...",
                color = AzulLogin,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // MENSAGEM DE ERRO
        if (mensagem.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensagem,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = VermelhoLogin,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(90.dp))

        // ACESSO AO CADASTRO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ainda não possui conta? ",
                fontSize = 12.sp,
                color = CinzaLogin
            )

            Text(
                text = "Cadastre-se",
                fontSize = 12.sp,
                color = CobreLogin,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    enabled = !carregando
                ) {
                    onRegisterClick()
                }
            )
        }
    }
}
