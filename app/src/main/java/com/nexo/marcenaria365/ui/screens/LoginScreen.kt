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
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexo.marcenaria365.R

private val FundoLogin = Color(0xFFFAF6F0)
private val AzulLogin = Color(0xFF193447)
private val CobreLogin = Color(0xFFBD8B60)
private val CinzaLogin = Color(0xFF929292)

@Composable
fun LoginScreen(
    onBackClick: () -> Unit
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

    val focusManager = LocalFocusManager.current

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
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
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
            color = CinzaLogin,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Acesse sua conta para continuar.",
            color = CinzaLogin,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                mensagem = ""
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("E-mail", color = CinzaLogin)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = CinzaLogin
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = CobreLogin,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = AzulLogin,
                unfocusedTextColor = AzulLogin
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                mensagem = ""
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Senha", color = CinzaLogin)
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
                        imageVector = if (senhaVisivel)
                            Icons.Outlined.VisibilityOff
                        else
                            Icons.Outlined.Visibility,
                        contentDescription = if (senhaVisivel)
                            "Ocultar senha"
                        else
                            "Mostrar senha",
                        tint = CinzaLogin
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (senhaVisivel)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = CobreLogin,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = AzulLogin,
                unfocusedTextColor = AzulLogin
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Esqueceu sua senha?",
            fontSize = 12.sp,
            color = AzulLogin,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    mensagem =
                        "Recuperação de senha será integrada à API."
                }
        )

        Spacer(Modifier.height(25.dp))

        Button(
            onClick = {
                focusManager.clearFocus()

                mensagem = when {
                    email.isBlank() || senha.isBlank() ->
                        "Preencha o e-mail e a senha."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches() ->
                        "Digite um e-mail válido."

                    else ->
                        "Login disponível após a integração com o servidor."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulLogin,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Entrar",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (mensagem.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensagem,
                color = AzulLogin,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.heightIn(min = 80.dp))
        Spacer(Modifier.weight(1f, fill = false))

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
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    mensagem =
                        "A tela de cadastro será desenvolvida em breve."
                }
            )
        }
    }
}
