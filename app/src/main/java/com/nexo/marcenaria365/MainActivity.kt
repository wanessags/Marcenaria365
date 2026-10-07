
package com.nexo.marcenaria365

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexo.marcenaria365.ui.screens.WelcomeScreen
import com.nexo.marcenaria365.ui.theme.Marcenaria365Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Marcenaria365Theme {
                WelcomeScreen()
            }
        }
    }
}
