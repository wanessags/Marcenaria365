package com.nexo.marcenaria365

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nexo.marcenaria365.ui.screens.OnboardingScreen
import com.nexo.marcenaria365.ui.theme.Marcenaria365Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Marcenaria365Theme {
                OnboardingScreen()
            }
        }
    }
}
