
package com.nexo.marcenaria365.ui.screens.perfil

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoPerfil = Color(0xFFFAF6F0)
private val AzulPerfil = Color(0xFF193447)
private val CobrePerfil = Color(0xFFBD906E)
private val CinzaPerfil = Color(0xFF929292)

@Composable
fun FotoPerfil(
    nome: String,
    fotoUri: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmap = remember(fotoUri) {
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
                contentDescription = "Foto do perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = iniciais,
                color = CobrePerfil,
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

    var nome by remember(nomeAtual) {
        mutableStateOf(nomeAtual)
    }

    var email by remember(emailAtual) {
        mutableStateOf(emailAtual)
    }

    var fotoUri by remember(fotoAtual) {
        mutableStateOf(fotoAtual)
    }

    var mostrarErro by remember {
        mutableStateOf(false)
    }

    var erroFoto by remember {
        mutableStateOf(false)
    }

    val seletorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val conseguiuPermissao = runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.isSuccess

            if (conseguiuPermissao) {
                fotoUri = uri.toString()
                erroFoto = false
            } else {
                erroFoto = true
            }
        }
    }

    val nomeValido = nome.trim().isNotEmpty()

    val emailValido = email.isBlank() ||
            Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()

    val dadosValidos = nomeValido && emailValido

    BackHandler(onBack = onVoltar)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = FundoPerfil,

        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(AzulPerfil)
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },

        bottomBar = {
            Button(
                onClick = {
                    mostrarErro = true

                    if (dadosValidos) {
                        onSalvar(
                            nome.trim(),
                            email.trim(),
                            fotoUri
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 10.dp,
                        bottom = 16.dp
                    )
                    .height(52.dp),
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 23.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))

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

            Row(
                modifier = Modifier.clickable {
                    seletorFoto.launch(
                        arrayOf("image/*")
                    )
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = CobrePerfil,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(7.dp))

                Text(
                    text = if (fotoUri.isNullOrBlank()) {
                        "Adicionar foto"
                    } else {
                        "Alterar foto"
                    },
                    color = CobrePerfil,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!fotoUri.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Remover foto",
                    color = CinzaPerfil,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        fotoUri = null
                    }
                )
            }

            if (erroFoto) {
                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Não foi possível acessar essa imagem. " +
                            "Escolha outra foto.",
                    color = Color(0xFFB94A48),
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(32.dp))

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
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Informações pessoais",
                        color = AzulPerfil,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(23.dp))

                    Text(
                        text = "Nome completo",
                        color = AzulPerfil,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(7.dp))

                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            mostrarErro = false
                        },
                        placeholder = {
                            Text(
                                "Digite seu nome",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = CinzaPerfil
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        isError = mostrarErro && !nomeValido,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CobrePerfil,
                            unfocusedBorderColor =
                                Color(0xFFE5E0DA),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    if (mostrarErro && !nomeValido) {
                        Text(
                            text = "Informe seu nome.",
                            color = Color(0xFFB94A48),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(21.dp))

                    Text(
                        text = "E-mail",
                        color = AzulPerfil,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(7.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            mostrarErro = false
                        },
                        placeholder = {
                            Text(
                                "seuemail@exemplo.com",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        isError = mostrarErro && !emailValido,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CobrePerfil,
                            unfocusedBorderColor =
                                Color(0xFFE5E0DA),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    if (mostrarErro && !emailValido) {
                        Text(
                            text = "Digite um e-mail válido.",
                            color = Color(0xFFB94A48),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(17.dp))

            Text(
                text = "Suas alterações serão salvas " +
                        "neste dispositivo.",
                color = CinzaPerfil,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(25.dp))
        }
    }
}
