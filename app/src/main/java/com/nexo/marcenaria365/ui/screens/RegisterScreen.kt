
package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
private val CinzaCadastro = Color(0xFF929292)
private val BordaCadastro = Color(0xFFE8DFD4)
private val VerdeCadastro = Color(0xFF239B7A)
private val AmareloCadastro = Color(0xFFD49A34)
private val VermelhoCadastro = Color(0xFFC75D5D)

// REGRAS DA SENHA
private data class RequisitosSenha(
    val tamanho: Boolean,
    val maiuscula: Boolean,
    val minuscula: Boolean,
    val numero: Boolean,
    val especial: Boolean,
    val semEspacos: Boolean
) {
    val quantidadeAtendida: Int
        get() = listOf(
            tamanho,
            maiuscula,
            minuscula,
            numero,
            especial,
            semEspacos
        ).count { it }

    val valida: Boolean
        get() = tamanho &&
                maiuscula &&
                minuscula &&
                numero &&
                especial &&
                semEspacos
}

private fun verificarSenha(senha: String): RequisitosSenha {
    return RequisitosSenha(
        tamanho = senha.length >= 8,
        maiuscula = senha.any { it.isUpperCase() },
        minuscula = senha.any { it.isLowerCase() },
        numero = senha.any { it.isDigit() },
        especial = senha.any {
            !it.isLetterOrDigit() && !it.isWhitespace()
        },
        semEspacos = senha.none { it.isWhitespace() }
    )
}

// INDICADOR VISUAL DE FORCA
@Composable
private fun IndicadorForcaSenha(
    senha: String,
    requisitos: RequisitosSenha
) {
    val forca = when {
        senha.isBlank() -> 0
        requisitos.quantidadeAtendida <= 2 -> 1
        requisitos.quantidadeAtendida <= 4 -> 2
        requisitos.quantidadeAtendida == 5 -> 3
        else -> 4
    }

    val cor = when (forca) {
        1 -> VermelhoCadastro
        2 -> AmareloCadastro
        3 -> AmareloCadastro
        4 -> VerdeCadastro
        else -> BordaCadastro
    }

    val classificacao = when (forca) {
        1 -> "Fraca"
        2 -> "Média"
        3 -> "Quase pronta"
        4 -> "Forte"
        else -> "Não informada"
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Força da senha",
                color = AzulCadastro,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = classificacao,
                color = if (forca == 0) {
                    CinzaCadastro
                } else {
                    cor
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(9.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(4) { indice ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            color = if (indice < forca) {
                                cor
                            } else {
                                BordaCadastro
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        Text(
            text = "Sua senha deve conter:",
            color = AzulCadastro,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(9.dp))

        RequisitoSenhaItem(
            texto = "Pelo menos 8 caracteres",
            atendido = requisitos.tamanho
        )

        RequisitoSenhaItem(
            texto = "Uma letra maiúscula",
            atendido = requisitos.maiuscula
        )

        RequisitoSenhaItem(
            texto = "Uma letra minúscula",
            atendido = requisitos.minuscula
        )

        RequisitoSenhaItem(
            texto = "Um número",
            atendido = requisitos.numero
        )

        RequisitoSenhaItem(
            texto = "Um caractere especial (@, #, !...)",
            atendido = requisitos.especial
        )

        RequisitoSenhaItem(
            texto = "Sem espaços",
            atendido = requisitos.semEspacos
        )

        if (requisitos.valida) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sua senha atende aos requisitos!",
                color = VerdeCadastro,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RequisitoSenhaItem(
    texto: String,
    atendido: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (atendido) {
                Icons.Outlined.CheckCircleOutline
            } else {
                Icons.Outlined.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = if (atendido) {
                VerdeCadastro
            } else {
                CinzaCadastro
            },
            modifier = Modifier.size(16.dp)
        )

        Spacer(Modifier.width(9.dp))

        Text(
            text = texto,
            color = if (atendido) {
                VerdeCadastro
            } else {
                CinzaCadastro
            },
            fontSize = 11.sp
        )
    }
}

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

    val scope = rememberCoroutineScope()

    val requisitos = verificarSenha(senha)

    val senhasCoincidem =
        confirmarSenha.isNotEmpty() &&
                senha == confirmarSenha

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobreCadastro,
        unfocusedBorderColor = BordaCadastro,
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
            .padding(horizontal = 27.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // VOLTAR
        IconButton(
            onClick = onBackClick,
            enabled = !carregando
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = AzulCadastro
            )
        }

        Spacer(Modifier.height(15.dp))

        // TITULO
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
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        // NOME COMPLETO
        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                mensagem = ""
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
            readOnly = carregando || cadastroConcluido,
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
                email = it
                mensagem = ""
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
            readOnly = carregando || cadastroConcluido,
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
                senha = it
                mensagem = ""
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
            readOnly = carregando || cadastroConcluido,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        // REQUISITOS E FORCA DA SENHA
        if (senha.isNotEmpty() && !cadastroConcluido) {
            Spacer(Modifier.height(13.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    IndicadorForcaSenha(
                        senha = senha,
                        requisitos = requisitos
                    )
                }
            }
        } else if (!cadastroConcluido) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Use uma senha com letras, números e símbolo.",
                color = CinzaCadastro,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // CONFIRMAR SENHA
        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = {
                confirmarSenha = it
                mensagem = ""
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
                            "Ocultar confirmação"
                        } else {
                            "Mostrar confirmação"
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
            readOnly = carregando || cadastroConcluido,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo,
            isError = confirmarSenha.isNotEmpty() &&
                    !senhasCoincidem &&
                    !cadastroConcluido
        )

        // CONFIRMACAO VISUAL
        if (
            confirmarSenha.isNotEmpty() &&
            !cadastroConcluido
        ) {
            Spacer(Modifier.height(7.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (senhasCoincidem) {
                        Icons.Outlined.CheckCircleOutline
                    } else {
                        Icons.Outlined.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = if (senhasCoincidem) {
                        VerdeCadastro
                    } else {
                        VermelhoCadastro
                    },
                    modifier = Modifier.size(15.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = if (senhasCoincidem) {
                        "As senhas coincidem."
                    } else {
                        "As senhas não coincidem."
                    },
                    color = if (senhasCoincidem) {
                        VerdeCadastro
                    } else {
                        VermelhoCadastro
                    },
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // CRIAR CONTA
        Button(
            onClick = {
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

                    !requisitos.valida ->
                        "Sua senha ainda não atende a todos os requisitos."

                    senha != confirmarSenha ->
                        "As senhas não coincidem."

                    else -> ""
                }

                if (mensagem.isEmpty() && !carregando) {
                    carregando = true

                    scope.launch {
                        val resultado = withContext(
                            Dispatchers.IO
                        ) {
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
                                senha = ""
                                confirmarSenha = ""

                                mensagem =
                                    "Conta criada com sucesso! " +
                                            "Agora você pode entrar."
                            },
                            onFailure = { erro ->
                                mensagem = erro.message
                                    ?: "Não foi possível realizar o cadastro."
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
                contentColor = Color.White
            )
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(23.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (cadastroConcluido) {
                        "Conta criada"
                    } else {
                        "Criar conta"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // MENSAGEM DE RESULTADO
        if (mensagem.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = mensagem,
                color = if (cadastroConcluido) {
                    VerdeCadastro
                } else {
                    VermelhoCadastro
                },
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(38.dp))

        // ENTRAR
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(
                    enabled = !carregando
                ) {
                    onLoginClick()
                }
            )
        }
    }
}
