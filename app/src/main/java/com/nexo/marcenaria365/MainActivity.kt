
package com.nexo.marcenaria365

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nexo.marcenaria365.data.remote.ApiClient
import com.nexo.marcenaria365.ui.screens.OnboardingScreen
import com.nexo.marcenaria365.ui.theme.Marcenaria365Theme
import com.nexo.marcenaria365.ui.theme.ModoAparencia
import com.nexo.marcenaria365.ui.theme.PreferenciasAparencia

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        ApiClient.configurarRede(applicationContext)

        // Recupera a preferência de aparência salva no aparelho.
        // Não modifica dados de login nem outras preferências.
        PreferenciasAparencia.inicializar(applicationContext)

        enableEdgeToEdge()

        setContent {
            val modoAtual = PreferenciasAparencia.modo
            val sistemaEscuro = isSystemInDarkTheme()

            val usarModoEscuro = when (modoAtual) {
                ModoAparencia.CLARO -> false
                ModoAparencia.ESCURO -> true
                ModoAparencia.AUTOMATICO -> sistemaEscuro
            }

            Marcenaria365Theme(
                darkTheme = usarModoEscuro
            ) {
                OnboardingScreen()
            }
        }
    }
}
