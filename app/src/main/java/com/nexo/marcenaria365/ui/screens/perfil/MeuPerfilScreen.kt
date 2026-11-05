
package com.nexo.marcenaria365.ui.screens.perfil

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoPerfil = Color(0xFFFAF6F0)
private val AzulPerfil = Color(0xFF193447)
private val CobrePerfil = Color(0xFF94613C)
private val CinzaPerfil = Color(0xFF53616A)
private val BordaPerfil = Color(0xFF89969E)
private val VermelhoPerfil = Color(0xFFB3261E)

@Composable
fun FotoPerfil(
    nome: String,
    fotoUri: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmap = remember(context, fotoUri) {
        if (fotoUri.isNullOrBlank()) {
            null
        } else {
            runCatching {
                context.contentResolver
                    .openInputStream(Uri.parse(fotoUri))
                    ?.use { stream ->
                        BitmapFactory.decodeStream(
                            stream,
                            null,
                            BitmapFactory.Options().apply {
                                inSampleSize = 2
                            }
                        )
                    }
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }

    val iniciais = nome
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "M" }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFEFE4D9)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto de perfil de $nome",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = iniciais,
                color = AzulPerfil,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MeuPerfilScreen(
    nomeAtual: String,
    emailAtual: String,
    fotoAtual: String?,
    onVoltar: () -> Unit,
    onSalvar: (String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    var nome by rememberSaveable(nomeAtual) {
        mutableStateOf(nomeAtual)
    }

    var email by rememberSaveable(emailAtual) {
        mutableStateOf(emailAtual)
    }

    var fotoUri by rememberSaveable(fotoAtual) {
        mutableStateOf(fotoAtual)
    }

    var tentouSalvar by rememberSaveable {
        mutableStateOf(false)
    }

    var erroFoto by remember {
        mutableStateOf("")
    }

    var confirmarRemocaoFoto by remember {
        mutableStateOf(false)
    }

    val seletorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val conseguiuPermissao = runCatching {
                context.contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
            }.isSuccess

            if (conseguiuPermissao) {
                fotoUri = uri.toString()
                erroFoto = ""
            } else {
                erroFoto =
                    "Não foi possível acessar essa imagem. Escolha outra foto."
            }
        }
    }

    val nomeValido = nome.trim().isNotEmpty()

    val emailValido = email.isBlank() ||
            Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()

    val nomeInvalido = tentouSalvar && !nomeValido
    val emailInvalido = tentouSalvar && !emailValido

    fun salvarPerfil() {
        tentouSalvar = true

        if (!nomeValido || !emailValido) {
            return
        }

        teclado?.hide()
        foco.clearFocus()

        onSalvar(
            nome.trim(),
            email.trim(),
            fotoUri
        )
    }

    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = CobrePerfil,
        unfocusedBorderColor = BordaPerfil,
        focusedTextColor = AzulPerfil,
        unfocusedTextColor = AzulPerfil,
        focusedLabelColor = AzulPerfil,
        unfocusedLabelColor = CinzaPerfil,
        cursorColor = AzulPerfil,
        errorBorderColor = VermelhoPerfil,
        errorLabelColor = VermelhoPerfil
    )

    BackHandler(
        enabled = !confirmarRemocaoFoto,
        onBack = onVoltar
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoPerfil,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp)
                    .background(AzulPerfil)
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .sizeIn(
                            minWidth = 48.dp,
                            minHeight = 48.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Meu perfil",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics {
                            heading()
                        }
                )
            }
        },
        bottomBar = {
            Surface(
                color = FundoPerfil
            ) {
                Button(
                    onClick = {
                        salvarPerfil()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 10.dp,
                            bottom = 16.dp
                        )
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPerfil,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        text = "Salvar alterações",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 23.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(26.dp))

            FotoPerfil(
                nome = nome,
                fotoUri = fotoUri,
                modifier = Modifier
                    .size(106.dp)
                    .border(
                        width = 3.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
            )

            Spacer(Modifier.height(15.dp))

            OutlinedButton(
                onClick = {
                    seletorFoto.launch(arrayOf("image/*"))
                },
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BordaPerfil),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = AzulPerfil
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = CobrePerfil,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (fotoUri.isNullOrBlank()) {
                        "Adicionar foto"
                    } else {
                        "Alterar foto"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!fotoUri.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))

                TextButton(
                    onClick = {
                        confirmarRemocaoFoto = true
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = VermelhoPerfil,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "Remover foto",
                        color = VermelhoPerfil,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (erroFoto.isNotBlank()) {
                Spacer(Modifier.height(10.dp))

                MensagemErroPerfil(erroFoto)
            }

            Spacer(Modifier.height(25.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(17.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Informações pessoais",
                        color = AzulPerfil,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )

                    Spacer(Modifier.height(22.dp))

                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                        },
                        label = {
                            Text("Nome completo *")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = CinzaPerfil
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (nomeInvalido) {
                                    Modifier.semantics {
                                        error("Informe seu nome.")
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
                        shape = RoundedCornerShape(11.dp),
                        colors = coresCampo
                    )

                    if (nomeInvalido) {
                        Spacer(Modifier.height(6.dp))
                        MensagemErroPerfil(
                            "Informe seu nome."
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Text("E-mail (opcional)")
                        },
                        supportingText = {
                            Text(
                                text = "Se informado, o e-mail deve ser válido.",
                                color = CinzaPerfil,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (emailInvalido) {
                                    Modifier.semantics {
                                        error(
                                            "Digite um e-mail válido."
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
                        shape = RoundedCornerShape(11.dp),
                        colors = coresCampo
                    )

                    if (emailInvalido) {
                        MensagemErroPerfil(
                            "Digite um e-mail válido."
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "* Campo obrigatório",
                        color = CinzaPerfil,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Suas alterações serão salvas neste dispositivo.",
                color = CinzaPerfil,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(25.dp))
        }
    }

    if (confirmarRemocaoFoto) {
        AlertDialog(
            onDismissRequest = {
                confirmarRemocaoFoto = false
            },
            title = {
                Text(
                    text = "Remover foto?",
                    color = AzulPerfil,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "A foto será removida do perfil quando você salvar as alterações.",
                    color = CinzaPerfil,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        fotoUri = null
                        erroFoto = ""
                        confirmarRemocaoFoto = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Remover",
                        color = VermelhoPerfil,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarRemocaoFoto = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = AzulPerfil
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun MensagemErroPerfil(
    mensagem: String
) {
    Text(
        text = mensagem,
        color = VermelhoPerfil,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                liveRegion = LiveRegionMode.Polite
            }
    )
}
