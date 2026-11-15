package com.nexo.marcenaria365.ui.screens

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.nexo.marcenaria365.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun RecuperacaoSenhaScreen(
    onVoltar: () -> Unit,
    solicitar: (String) -> Result<String> = AuthRepository::solicitarRecuperacao,
    redefinir: (String, String) -> Result<String> = AuthRepository::redefinirSenha
) {
    var email by rememberSaveable { mutableStateOf("") }
    var solicitado by rememberSaveable { mutableStateOf(false) }
    // Código e senhas ficam somente em memória, nunca no estado salvo do Android.
    var codigo by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var visivel by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }
    var concluido by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val regras = avaliarSenhaCadastro(senha)
    BackHandler { if (!carregando) onVoltar() }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF193447), secondary = Color(0xFF94613C),
        background = Color(0xFFFAF6F0), surface = Color.White,
        onSurface = Color(0xFF193447), onBackground = Color(0xFF193447),
        error = Color(0xFFB3261E)
    )) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing).imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 25.dp, vertical = 10.dp)) {
            IconButton(onClick = onVoltar, enabled = !carregando) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar ao login")
            }
            Text("Recuperar senha", style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(20.dp))
            if (!concluido) {
                Text(if (solicitado) "Informe o código recebido por e-mail e sua nova senha."
                    else "Informe seu e-mail para solicitar um código temporário.")
                Spacer(Modifier.height(12.dp))
                if (!solicitado) {
                    OutlinedTextField(value = email, onValueChange = { email = it },
                        label = { Text("E-mail") }, singleLine = true, enabled = !carregando,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = codigo, onValueChange = { codigo = it },
                        label = { Text("Código de recuperação") }, singleLine = true, enabled = !carregando,
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = senha, onValueChange = { senha = it },
                        label = { Text("Nova senha") }, singleLine = true, enabled = !carregando,
                        visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { visivel = !visivel }) {
                                Icon(if (visivel) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha")
                            }
                        }, modifier = Modifier.fillMaxWidth())
                    ForcaSenhaCadastro(senha, regras)
                    OutlinedTextField(value = confirmacao, onValueChange = { confirmacao = it },
                        label = { Text("Confirmar nova senha") }, singleLine = true, enabled = !carregando,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(20.dp))
                Button(enabled = !carregando, modifier = Modifier.fillMaxWidth().heightIn(min = 53.dp),
                    onClick = {
                        mensagem = when {
                            !solicitado && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Informe um e-mail válido."
                            solicitado && !codigo.trim().matches(Regex("[a-fA-F0-9]{64}")) -> "Confira o código recebido por e-mail."
                            solicitado && !regras.valida -> "Confira os cinco requisitos da senha."
                            solicitado && senha.toByteArray(Charsets.UTF_8).size > 72 -> "A senha deve ter no máximo 72 bytes. Use uma senha mais curta."
                            solicitado && senha != confirmacao -> "A confirmação deve ser igual à senha."
                            else -> ""
                        }
                        erro = mensagem.isNotEmpty()
                        if (!erro) {
                            carregando = true
                            scope.launch {
                                try {
                                    val resultado = withContext(Dispatchers.IO) {
                                        if (solicitado) redefinir(codigo, senha)
                                        else solicitar(email)
                                    }
                                    resultado.fold(onSuccess = {
                                        mensagem = it
                                        if (solicitado) {
                                            concluido = true
                                            senha = ""
                                            confirmacao = ""
                                            codigo = ""
                                        } else solicitado = true
                                    }, onFailure = {
                                        erro = true
                                        mensagem = it.message ?: "Não foi possível concluir. Tente novamente."
                                    })
                                } finally { carregando = false }
                            }
                        }
                    }) {
                    Text(if (carregando) "Conectando ao servidor, aguarde..."
                        else if (solicitado) "Atualizar senha" else "Solicitar código")
                }
                if (solicitado) {
                    TextButton(enabled = !carregando, onClick = {
                        solicitado = false
                        codigo = ""
                        senha = ""
                        confirmacao = ""
                        mensagem = ""
                    }) { Text("Solicitar outro código") }
                }
            }
            if (mensagem.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(mensagem, color = if (erro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        if (erro) error(mensagem)
                    })
            }
            if (concluido) Button(onClick = onVoltar) { Text("Voltar ao login") }
        }
    }
}
