
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

    var telaAtual by rememberSaveable {
        mutableStateOf("onboarding")
    }

    var nomeDemonstracao by rememberSaveable {
        mutableStateOf("Mariana")
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    when (telaAtual) {

        "login" -> {
            BackHandler {
                telaAtual = "onboarding"
            }

            LoginScreen(
                onBackClick = {
                    telaAtual = "onboarding"
                },
                onRegisterClick = {
                    telaAtual = "cadastro"
                },
                onDemoLogin = { nome ->
                    nomeDemonstracao = nome
                    telaAtual = "home"
                }
            )
        }

        "cadastro" -> {
            BackHandler {
                telaAtual = "login"
            }

            RegisterScreen(
                onBackClick = {
                    telaAtual = "login"
                },
                onLoginClick = {
                    telaAtual = "login"
                }
            )
        }

        "home" -> {
            BackHandler {
                // Evita voltar ao login pelo botão físico
                // durante a demonstração.
            }

            HomeScreen(
                nomeUsuario = nomeDemonstracao,
                onSair = {
                    telaAtual = "login"
                }
            )
        }

        else -> {
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
                            telaAtual = "login"
                        }
                    )
                }
            }
        }
    }
}
