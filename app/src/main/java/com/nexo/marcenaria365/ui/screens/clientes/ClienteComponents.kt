
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mantemos as cores originais para preservar a compatibilidade
// com outras telas que já utilizam ClienteCores.
object ClienteCores {
    val Fundo = Color(0xFFFAF6F0)
    val Azul = Color(0xFF193447)
    val Cobre = Color(0xFFB98C69)
    val Cinza = Color(0xFF85888A)
    val Borda = Color(0xFFE9E3DD)
    val Branco = Color.White
    val Vermelho = Color(0xFFB34D4D)
    val Verde = Color(0xFF27845D)
}

@Composable
fun ClienteCabecalho(
    titulo: String,
    subtitulo: String,
    onVoltar: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = cores.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = titulo,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = cores.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = subtitulo,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = cores.onSurfaceVariant,
            modifier = Modifier.padding(
                start = 52.dp,
                top = 3.dp
            )
        )
    }
}

@Composable
fun ClienteAvatar(
    nome: String,
    modifier: Modifier = Modifier,
    tamanho: Dp = 52.dp
) {
    val iniciais = nome.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(ClienteCores.Cobre),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciais,
            color = Color(0xFF193447),
            fontSize = if (tamanho >= 70.dp) 24.sp else 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ClienteTituloSecao(
    titulo: String
) {
    val cores = MaterialTheme.colorScheme

    Text(
        text = titulo.uppercase(),
        color = cores.secondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun ClienteInformacao(
    titulo: String,
    valor: String
) {
    val cores = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = cores.onSurfaceVariant
        )

        Text(
            text = valor.ifBlank { "Não informado" },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = cores.onSurface
        )
    }
}

@Composable
fun ClienteStatus(ativo: Boolean) {
    val cores = MaterialTheme.colorScheme

    // Consulta o tema aplicado pelo aplicativo, não a
    // configuração automática do Android.
    val temaEscuro = cores.background.luminance() < 0.5f

    val fundoStatus = when {
        ativo && temaEscuro -> Color(0xFF173E30)
        ativo -> Color(0xFFE5F4EC)
        temaEscuro -> cores.surfaceVariant
        else -> Color(0xFFF1EEEE)
    }

    val textoStatus = when {
        ativo && temaEscuro -> Color(0xFF9BE5B6)
        ativo -> ClienteCores.Verde
        temaEscuro -> cores.onSurfaceVariant
        else -> Color(0xFF555F66)
    }

    Surface(
        color = fundoStatus,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = if (ativo) "Cliente ativo" else "Cliente inativo",
            color = textoStatus,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 7.dp
            )
        )
    }
}
