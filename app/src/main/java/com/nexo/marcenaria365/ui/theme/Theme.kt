
package com.nexo.marcenaria365.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ModoAparencia {
    CLARO,
    ESCURO,
    AUTOMATICO
}

object PreferenciasAparencia {
    private const val ARQUIVO = "marcenaria365_aparencia"
    private const val CHAVE = "modo"

    var modo by mutableStateOf(ModoAparencia.CLARO)
        private set

    fun inicializar(context: Context) {
        val preferencias = context.getSharedPreferences(
            ARQUIVO,
            Context.MODE_PRIVATE
        )

        val valor = preferencias.getString(
            CHAVE,
            ModoAparencia.CLARO.name
        )

        modo = runCatching {
            ModoAparencia.valueOf(
                valor ?: ModoAparencia.CLARO.name
            )
        }.getOrDefault(ModoAparencia.CLARO)
    }

    fun alterar(
        context: Context,
        novoModo: ModoAparencia
    ) {
        context.getSharedPreferences(
            ARQUIVO,
            Context.MODE_PRIVATE
        ).edit()
            .putString(CHAVE, novoModo.name)
            .apply()

        modo = novoModo
    }
}

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF193447),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E4D7),
    onPrimaryContainer = Color(0xFF193447),
    secondary = Color(0xFF94613C),
    onSecondary = Color.White,
    background = Color(0xFFFAF6F0),
    onBackground = Color(0xFF193447),
    surface = Color.White,
    onSurface = Color(0xFF193447),
    surfaceVariant = Color(0xFFF1E8DE),
    onSurfaceVariant = Color(0xFF53616A),
    outline = Color(0xFF89969E),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val EsquemaEscuro = darkColorScheme(
    primary = Color(0xFFE9B47C),
    onPrimary = Color(0xFF1B252D),
    primaryContainer = Color(0xFF344755),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE9B47C),
    onSecondary = Color(0xFF1B252D),
    background = Color(0xFF0D1922),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF192934),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF263946),
    onSurfaceVariant = Color(0xFFD1DCE3),
    outline = Color(0xFF788F9D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun Marcenaria365Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquema = if (darkTheme) {
        EsquemaEscuro
    } else {
        EsquemaClaro
    }

    MaterialTheme(
        colorScheme = esquema,
        typography = Typography,
        content = content
    )
}
