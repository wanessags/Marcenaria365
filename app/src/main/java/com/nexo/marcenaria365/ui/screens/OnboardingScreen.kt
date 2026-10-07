
package com.nexo.marcenaria365.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen() {
    var mostrarLogin by rememberSaveable {
        mutableStateOf(false)
    }

    if (mostrarLogin) {
        // Tela temporária até recebermos o protótipo.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Tela de login em desenvolvimento")
        }
    } else {
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { 2 }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pagina ->
            when (pagina) {
                0 -> WelcomeScreen(
                    paginaAtual = pagerState.currentPage,
                    totalPaginas = 2
                )

                1 -> PrivacyScreen(
                    onAgreeClick = {
                        mostrarLogin = true
                    }
                )
            }
        }
    }
}
