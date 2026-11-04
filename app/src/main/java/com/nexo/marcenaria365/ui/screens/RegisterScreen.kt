
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.nexo.marcenaria365.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FundoCadastro = Color(0xFFFAF6F0)
private val AzulCadastro = Color(0xFF193447)
private val CobreCadastro = Color(0xFFBD8B60)
private val CinzaCadastro = Color(0xFF53616A)
private val BordaCadastro = Color(0xFFABB5BA)
private val VermelhoCadastro = Color(0xFFB3261E)
private val VerdeCadastro = Color(0xFF237A58)

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var nome by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var senha by rememberSaveable {
        mutableStateOf("")
    }

    var confirmarSenha by rememberSaveable {
        mutableStateOf("")
    }

    var senhaVisivel by rememberSaveable {
        mutableStateOf(false)
    }

    var confirmarVisivel by rememberSaveable {
        mutableStateOf(false)
    }

    var mensagem by rememberSaveable {
        mutableStateOf("")
    }

    var cadastroConcluido by rememberSaveable {
        mutableStateOf(false)
    }

    var carregando by remember {
        mutableStateOf(false)
    }

    var mensagemSucesso by rememberSaveable {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    // Mantem os campos visiveis mesmo durante a requisicao.
    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,

        focusedBorderColor = CobreCadastro,
        unfocusedBorderColor = BordaCadastro,

        focusedTextColor = AzulCadastro,
        unfocusedTextColor = AzulCadastro,

        focusedPlaceholderColor = CinzaCadastro,
        unfocusedPlaceholderColor = CinzaCadastro,

        focusedLeadingIconColor = CinzaCadastro,
        unfocusedLeadingIconColor = CinzaCadastro,

        focusedTrailingIconColor = CinzaCadastro,
        unfocusedTrailingIconColor = CinzaCadastro,

        cursorColor = CobreCadastro
    )

    // Em vez de desabilitar os campos, bloqueamos apenas a edicao.
    // Assim o Material 3 nao deixa os textos transparentes.
    val bloquearEdicao = carregando || cadastroConcluido

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

        IconButton(
            onClick = onBackClick,
            enabled = !carregando
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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

        // NOME COMPLETO
        OutlinedTextField(
            value = nome,
            onValueChange = {
                if (!bloquearEdicao) {
                    nome = it
                    mensagem = ""
                    mensagemSucesso = false
                }
            },
            placeholder = {
                Text(
                    text = "Nome completo",
                    color = CinzaCadastro
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = CinzaCadastro
                )
            },
            readOnly = bloquearEdicao,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        // E-MAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                if (!bloquearEdicao) {
                    email = it
                    mensagem = ""
                    mensagemSucesso = false
                }
            },
            placeholder = {
                Text(
                    text = "E-mail",
                    color = CinzaCadastro
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = CinzaCadastro
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            readOnly = bloquearEdicao,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        // SENHA
        OutlinedTextField(
            value = senha,
            onValueChange = {
                if (!bloquearEdicao) {
                    senha = it
                    mensagem = ""
                    mensagemSucesso = false
                }
            },
            placeholder = {
                Text(
                    text = "Senha",
                    color = CinzaCadastro
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = CinzaCadastro
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
                        tint = CinzaCadastro
                    )
                }
            },
            visualTransformation = if (senhaVisivel) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            readOnly = bloquearEdicao,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        // CONFIRMAR SENHA
        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = {
                if (!bloquearEdicao) {
                    confirmarSenha = it
                    mensagem = ""
                    mensagemSucesso = false
                }
            },
            placeholder = {
                Text(
                    text = "Confirmar senha",
                    color = CinzaCadastro
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
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
                        imageVector = if (confirmarVisivel) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (confirmarVisivel) {
                            "Ocultar confirmação de senha"
                        } else {
                            "Mostrar confirmação de senha"
                        },
                        tint = CinzaCadastro
                    )
                }
            },
            visualTransformation = if (confirmarVisivel) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            readOnly = bloquearEdicao,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Use pelo menos 8 caracteres para sua senha.",
            color = CinzaCadastro,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(26.dp))

        // BOTAO DE CADASTRO
        Button(
            onClick = {
                mensagemSucesso = false

                mensagem = when {
                    nome.isBlank() ||
                            email.isBlank() ||
                            senha.isBlank() ||
                            confirmarSenha.isBlank() ->
                        "Preencha todos os campos."

                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim())
                        .matches() ->
                        "Digite um e-mail válido."

                    senha.length < 8 ->
                        "A senha deve ter pelo menos 8 caracteres."

                    senha != confirmarSenha ->
                        "As senhas não coincidem."

                    else -> ""
                }

                if (
                    mensagem.isEmpty() &&
                    !carregando &&
                    !cadastroConcluido
                ) {
                    carregando = true

                    // Evita novas edicoes e novos envios.
                    scope.launch {
                        val resultado = withContext(Dispatchers.IO) {
                            AuthRepository.cadastrar(
                                nome = nome.trim(),
                                email = email.trim(),
                                senha = senha
                            )
                        }

                        carregando = false

                        resultado.fold(
                            onSuccess = {
                                cadastroConcluido = true
                                mensagemSucesso = true

                                senha = ""
                                confirmarSenha = ""

                                mensagem =
                                    "Conta criada com sucesso! " +
                                            "Agora você pode entrar."
                            },
                            onFailure = { erro ->
                                cadastroConcluido = false
                                mensagemSucesso = false

                                mensagem = erro.message
                                    ?: "Não foi possível realizar o cadastro. " +
                                            "Tente novamente."
                            }
                        )
                    }
                }
            },
            enabled = !carregando && !cadastroConcluido,
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulCadastro,
                contentColor = Color.White,
                disabledContainerColor = AzulCadastro,
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
                        text = "Criando conta...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = if (cadastroConcluido) {
                        "Conta criada"
                    } else {
                        "Criar conta"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // MENSAGEM DE CARREGAMENTO
        if (carregando) {
            Spacer(Modifier.height(15.dp))

            Text(
                text = "Estamos realizando seu cadastro. " +
                        "Isso pode levar alguns instantes.",
                color = AzulCadastro,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // MENSAGEM DE RESULTADO
        if (mensagem.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensagem,
                color = if (mensagemSucesso) {
                    VerdeCadastro
                } else {
                    VermelhoCadastro
                },
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
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
                text = if (cadastroConcluido) {
                    "Sua conta está pronta. "
                } else {
                    "Já possui uma conta? "
                },
                fontSize = 12.sp,
                color = CinzaCadastro
            )

            Text(
                text = "Entrar",
                color = CobreCadastro,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    enabled = !carregando
                ) {
                    onLoginClick()
                }
            )
        }
    }
}
