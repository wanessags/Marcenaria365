package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoCadastro = Color(0xFFFAF6F0)
private val AzulCadastro = Color(0xFF193447)
private val CobreCadastro = Color(0xFFBD8B60)
private val CinzaCadastro = Color(0xFF929292)

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmarSenha by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    var confirmarVisivel by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf("") }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreCadastro,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = AzulCadastro,
        unfocusedTextColor = AzulCadastro,
        cursorColor = CobreCadastro
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoCadastro)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp)
    ) {

        Spacer(Modifier.height(8.dp))

        IconButton(onClick = onBackClick) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = AzulCadastro
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Crie sua conta",
            color = AzulCadastro,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Organize seus serviços em um só lugar.",
            color = CinzaCadastro,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                mensagem = ""
            },
            placeholder = { Text("Nome completo", color = CinzaCadastro) },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = CinzaCadastro
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                mensagem = ""
            },
            placeholder = { Text("E-mail", color = CinzaCadastro) },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = null,
                    tint = CinzaCadastro
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                mensagem = ""
            },
            placeholder = { Text("Senha", color = CinzaCadastro) },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = CinzaCadastro
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { senhaVisivel = !senhaVisivel }
                ) {
                    Icon(
                        imageVector = if (senhaVisivel)
                            Icons.Outlined.VisibilityOff
                        else Icons.Outlined.Visibility,
                        contentDescription = if (senhaVisivel)
                            "Ocultar senha" else "Mostrar senha",
                        tint = CinzaCadastro
                    )
                }
            },
            visualTransformation = if (senhaVisivel)
                VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = {
                confirmarSenha = it
                mensagem = ""
            },
            placeholder = {
                Text("Confirmar senha", color = CinzaCadastro)
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = CinzaCadastro
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        confirmarVisivel = !confirmarVisivel
                    }
                ) {
                    Icon(
                        imageVector = if (confirmarVisivel)
                            Icons.Outlined.VisibilityOff
                        else Icons.Outlined.Visibility,
                        contentDescription = if (confirmarVisivel)
                            "Ocultar confirmação de senha"
                        else "Mostrar confirmação de senha",
                        tint = CinzaCadastro
                    )
                }
            },
            visualTransformation = if (confirmarVisivel)
                VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Use pelo menos 8 caracteres para sua senha.",
            color = CinzaCadastro,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = {
                mensagem = when {
                    nome.isBlank() ||
                            email.isBlank() ||
                            senha.isBlank() ||
                            confirmarSenha.isBlank() ->
                        "Preencha todos os campos."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches() ->
                        "Digite um e-mail válido."

                    senha.length < 8 ->
                        "A senha deve ter pelo menos 8 caracteres."

                    senha != confirmarSenha ->
                        "As senhas não coincidem."

                    else ->
                        "Dados válidos! O cadastro será ativado " +
                                "quando a API estiver integrada."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulCadastro,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Criar conta",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (mensagem.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensagem,
                color = AzulCadastro,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(55.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Já possui uma conta? ",
                fontSize = 12.sp,
                color = CinzaCadastro
            )

            Text(
                text = "Entrar",
                color = CobreCadastro,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onLoginClick()
                }
            )
        }
    }
}
