
package com.nexo.marcenaria365

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nexo.marcenaria365.data.remote.ApiClient
import com.nexo.marcenaria365.ui.screens.OnboardingScreen
import com.nexo.marcenaria365.ui.theme.Marcenaria365Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Mantém a configuração de rede do aplicativo.
        ApiClient.configurarRede(applicationContext)

        enableEdgeToEdge()

        setContent {
            // MVP: utiliza somente o modo claro.
            // O código do modo escuro permanece preservado
            // para uma possível versão futura.
            Marcenaria365Theme(
                darkTheme = false
            ) {
                OnboardingScreen()
            }
        }
    }
}
