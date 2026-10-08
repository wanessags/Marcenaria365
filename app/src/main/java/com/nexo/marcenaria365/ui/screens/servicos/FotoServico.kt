
package com.nexo.marcenaria365.ui.screens.servicos

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun FotoServico(
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
                        )?.asImageBitmap()
                    }
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEDE4DA)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto do serviço",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Chair,
                contentDescription = "Serviço sem foto",
                tint = Color(0xFFBD906E),
                modifier = Modifier.size(35.dp)
            )
        }
    }
}
