package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun OnboardingScreen() {

    var mostrarLogin by rememberSaveable {
        mutableStateOf(false)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    if (mostrarLogin) {

        BackHandler {
            mostrarLogin = false
        }

        LoginScreen(
            onBackClick = {
                mostrarLogin = false
            }
        )

    } else {

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
