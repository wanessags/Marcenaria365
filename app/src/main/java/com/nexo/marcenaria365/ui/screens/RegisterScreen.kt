
package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircleOutline
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
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
import com.nexo.marcenaria365.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Mantém os cinco requisitos já existentes.
private data class RegrasCadastro(
    val tamanho: Boolean,
    val maiuscula: Boolean,
    val minuscula: Boolean,
    val numero: Boolean,
    val especial: Boolean
) {
    val quantidade: Int
        get() = listOf(
            tamanho,
            maiuscula,
            minuscula,
            numero,
            especial
        ).count { it }

    val valida: Boolean
        get() = tamanho &&
                maiuscula &&
                minuscula &&
                numero &&
                especial
}

private fun avaliarSenhaCadastro(
    senha: String
): RegrasCadastro {
    return RegrasCadastro(
        tamanho = senha.length >= 8,
        maiuscula = senha.any { it.isUpperCase() },
        minuscula = senha.any { it.isLowerCase() },
        numero = senha.any { it.isDigit() },
        especial = senha.any {
            !it.isLetterOrDigit() && !it.isWhitespace()
        }
    )
}

@Composable
private fun CorSucessoCadastro(): Color {
    val fundo = MaterialTheme.colorScheme.background

    return if (fundo.luminance() < 0.5f) {
        Color(0xFF83DDB5)
    } else {
        Color(0xFF237A58)
    }
}

@Composable
private fun RequisitoCadastro(
    descricao: String,
    atendido: Boolean
) {
    val cores = MaterialTheme.colorScheme
    val verde = CorSucessoCadastro()

    val cor = if (atendido) {
        verde
    } else {
        cores.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${if (atendido) "Atendido" else "Pendente"}: $descricao"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircleOutline,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(9.dp))

        Text(
            text = "${if (atendido) "Atendido" else "Pendente"}: $descricao",
            color = cor,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ForcaSenhaCadastro(
    senha: String,
    regras: RegrasCadastro
) {
    val cores = MaterialTheme.colorScheme
    val verde = CorSucessoCadastro()

    // Nível zero permite mostrar os requisitos
    // logo ao tocar no campo, antes de digitar.
    val nivel = when {
        senha.isEmpty() -> 0
        regras.valida -> 4
        regras.quantidade >= 4 -> 3
        regras.quantidade >= 3 -> 2
        else -> 1
    }

    val descricao = when (nivel) {
        0 -> "Comece a digitar"
        4 -> "Forte"
        3 -> "Quase pronta"
        2 -> "Média"
        else -> "Fraca"
    }

    val cor = when (nivel) {
        0 -> cores.onSurfaceVariant
        4 -> verde
        3 -> cores.secondary
        2 -> cores.secondary
        else -> cores.error
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = cores.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Segurança da senha",
                    color = cores.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = descricao,
                    color = cor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Barra visual acompanhada de descrição textual.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription =
                            "Força da senha: $descricao"
                    },
                horizontalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                repeat(4) { indice ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .background(
                                color = if (indice < nivel) {
                                    cor
                                } else {
                                    cores.outline.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Requisitos da senha",
                color = cores.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Spacer(Modifier.height(5.dp))

            RequisitoCadastro(
                descricao = "Pelo menos 8 caracteres",
                atendido = regras.tamanho
            )

            RequisitoCadastro(
                descricao = "Uma letra maiúscula",
                atendido = regras.maiuscula
            )

            RequisitoCadastro(
                descricao = "Uma letra minúscula",
                atendido = regras.minuscula
            )

            RequisitoCadastro(
                descricao = "Um número",
                atendido = regras.numero
            )

            RequisitoCadastro(
                descricao = "Um caractere especial",
                atendido = regras.especial
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val cores = MaterialTheme.colorScheme
    val verde = CorSucessoCadastro()

    val scope = rememberCoroutineScope()
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    // Estados originais do formulário.
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

    var tentouSalvar by rememberSaveable {
        mutableStateOf(false)
    }

    // NOVO: controla quando o indicador aparece.
    // Começa oculto e aparece ao focar o campo.
    var acessouCampoSenha by rememberSaveable {
        mutableStateOf(false)
    }

    val regras = avaliarSenhaCadastro(senha)

    val bloquearEdicao =
        carregando || cadastroConcluido

    val nomeInvalido =
        tentouSalvar && nome.isBlank()

    val emailInvalido =
        tentouSalvar &&
                !Patterns.EMAIL_ADDRESS.matcher(
                    email.trim()
                ).matches()

    val senhaInvalida =
        tentouSalvar && !regras.valida

    val confirmacaoInvalida =
        (confirmarSenha.isNotEmpty() &&
                confirmarSenha != senha) ||
                (tentouSalvar && confirmarSenha != senha)

    // Cores adaptáveis aos modos claro e escuro.
    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = cores.surface,
        unfocusedContainerColor = cores.surface,
        focusedBorderColor = cores.primary,
        unfocusedBorderColor = cores.outline,
        focusedTextColor = cores.onSurface,
        unfocusedTextColor = cores.onSurface,
        focusedLabelColor = cores.primary,
        unfocusedLabelColor = cores.onSurfaceVariant,
        focusedLeadingIconColor = cores.primary,
        unfocusedLeadingIconColor = cores.onSurfaceVariant,
        cursorColor = cores.primary,
        errorBorderColor = cores.error,
        errorLabelColor = cores.error
    )

    // Preserva a validação e o envio original para a API.
    fun cadastrar() {
        tentouSalvar = true

        mensagem = when {
            nome.isBlank() ->
                "Informe seu nome completo."

            email.isBlank() ->
                "Informe seu e-mail."

            !Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches() ->
                "Informe um e-mail válido."

            !regras.valida ->
                "Confira os requisitos da senha."

            confirmarSenha.isBlank() ->
                "Confirme sua senha."

            senha != confirmarSenha ->
                "A confirmação deve ser igual à senha."

            else -> ""
        }

        if (
            mensagem.isNotBlank() ||
            carregando ||
            cadastroConcluido
        ) {
            return
        }

        teclado?.hide()
        foco.clearFocus()
        carregando = true

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
                    senha = ""
                    confirmarSenha = ""
                    mensagem =
                        "Conta criada com sucesso! " +
                                "Agora você pode entrar."
                },
                onFailure = { erro ->
                    mensagem = erro.message
                        ?: "Não foi possível criar a conta."
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cores.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 25.dp)
    ) {
        Spacer(Modifier.height(8.dp))

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
                tint = cores.onBackground
            )
        }

        Spacer(Modifier.height(15.dp))

        // CABEÇALHO
        Text(
            text = "Crie sua conta",
            color = cores.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    heading()
                }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Organize seus serviços em um só lugar.",
            color = cores.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(25.dp))

        // NOME COMPLETO
        OutlinedTextField(
            value = nome,
            onValueChange = {
                if (!bloquearEdicao) {
                    nome = it
                    mensagem = ""
                }
            },
            label = {
                Text("Nome completo")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null
                )
            },
            singleLine = true,
            readOnly = bloquearEdicao,
            isError = nomeInvalido,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (nomeInvalido) {
                        Modifier.semantics {
                            error("Informe seu nome completo.")
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        if (nomeInvalido) {
            TextoErroCadastro(
                "Informe seu nome completo."
            )
        }

        Spacer(Modifier.height(12.dp))

        // E-MAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                if (!bloquearEdicao) {
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            readOnly = bloquearEdicao,
            isError = emailInvalido,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (emailInvalido) {
                        Modifier.semantics {
                            error("Informe um e-mail válido.")
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        if (emailInvalido) {
            TextoErroCadastro(
                "Informe um e-mail válido."
            )
        }

        Spacer(Modifier.height(12.dp))

        // CRIAR SENHA
        OutlinedTextField(
            value = senha,
            onValueChange = {
                if (!bloquearEdicao) {
                    senha = it
                    mensagem = ""
                    acessouCampoSenha = true
                }
            },
            label = {
                Text("Criar senha")
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
                    enabled = !bloquearEdicao,
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
                        }
                    )
                }
            },
            visualTransformation = if (senhaVisivel) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            readOnly = bloquearEdicao,
            singleLine = true,
            isError = senhaInvalida,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { estado ->
                    if (estado.isFocused && !bloquearEdicao) {
                        acessouCampoSenha = true
                    }
                }
                .then(
                    if (senhaInvalida) {
                        Modifier.semantics {
                            error(
                                "A senha ainda não atende aos requisitos."
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        if (senhaInvalida) {
            TextoErroCadastro(
                "Sua senha ainda não atende aos requisitos."
            )
        }

        // INDICADOR DE FORÇA
        // Só aparece ao tocar no campo Criar senha,
        // digitar ou tentar enviar o formulário.
        if (
            !cadastroConcluido &&
            (acessouCampoSenha || tentouSalvar || senha.isNotEmpty())
        ) {
            Spacer(Modifier.height(12.dp))

            ForcaSenhaCadastro(
                senha = senha,
                regras = regras
            )
        }

        Spacer(Modifier.height(12.dp))

        // CONFIRMAR SENHA
        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = {
                if (!bloquearEdicao) {
                    confirmarSenha = it
                    mensagem = ""
                }
            },
            label = {
                Text("Confirmar senha")
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
                        confirmarVisivel = !confirmarVisivel
                    },
                    enabled = !bloquearEdicao,
                    modifier = Modifier.sizeIn(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
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
                        }
                    )
                }
            },
            visualTransformation = if (confirmarVisivel) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    teclado?.hide()
                    foco.clearFocus()
                }
            ),
            readOnly = bloquearEdicao,
            singleLine = true,
            isError = confirmacaoInvalida,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (confirmacaoInvalida) {
                        Modifier.semantics {
                            error(
                                "A confirmação deve ser igual à senha."
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = coresCampo
        )

        if (
            confirmacaoInvalida &&
            !cadastroConcluido
        ) {
            TextoErroCadastro(
                "A confirmação deve ser igual à senha."
            )
        } else if (
            confirmarSenha.isNotBlank() &&
            confirmarSenha == senha &&
            !cadastroConcluido
        ) {
            Text(
                text = "As senhas coincidem.",
                color = verde,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )
        }

        Spacer(Modifier.height(24.dp))

        // CRIAR CONTA: MANTÉM O CADASTRO PELA API
        Button(
            onClick = {
                cadastrar()
            },
            enabled = !carregando && !cadastroConcluido,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 53.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = cores.primary,
                contentColor = cores.onPrimary,
                disabledContainerColor =
                    cores.primary.copy(alpha = 0.6f),
                disabledContentColor =
                    cores.onPrimary.copy(alpha = 0.8f)
            )
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = cores.onPrimary,
                    strokeWidth = 2.dp
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "Criando conta...",
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = if (cadastroConcluido) {
                        "Conta criada"
                    } else {
                        "Criar conta"
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        // MENSAGEM DO CADASTRO
        if (mensagem.isNotBlank()) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = mensagem,
                color = if (cadastroConcluido) {
                    verde
                } else {
                    cores.error
                },
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )
        }

        Spacer(Modifier.height(30.dp))

        // ACESSO AO LOGIN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (cadastroConcluido) {
                    "Sua conta está pronta. "
                } else {
                    "Já possui uma conta? "
                },
                color = cores.onSurfaceVariant,
                fontSize = 12.sp
            )

            TextButton(
                onClick = onLoginClick,
                enabled = !carregando,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Entrar",
                    color = cores.secondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(25.dp))
    }
}

@Composable
private fun TextoErroCadastro(
    mensagem: String
) {
    val cores = MaterialTheme.colorScheme

    Text(
        text = mensagem,
        color = cores.error,
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
