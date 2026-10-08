
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import com.nexo.marcenaria365.ui.screens.clientes.ClientesScreen
import com.nexo.marcenaria365.ui.screens.clientes.clientesIniciais

@Composable
fun OnboardingScreen() {

    var telaAtual by rememberSaveable {
        mutableStateOf("onboarding")
    }

    var nomeUsuario by rememberSaveable {
        mutableStateOf("Mariana")
    }

    val clientes = remember {
        mutableStateListOf<ClienteUi>().apply {
            addAll(clientesIniciais())
        }
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
                    nomeUsuario = nome
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
                // A saída é feita pelo menu Mais.
            }

            HomeScreen(
                nomeUsuario = nomeUsuario,
                quantidadeClientes = clientes.size,
                onClientesClick = {
                    telaAtual = "clientes"
                },
                onSair = {
                    telaAtual = "login"
                }
            )
        }

        "clientes" -> {
            BackHandler {
                telaAtual = "home"
            }

            ClientesScreen(
                clientes = clientes,
                onAdicionarCliente = { novoCliente ->
                    clientes.add(novoCliente)
                },
                onAtualizarCliente = { clienteAtualizado ->
                    val indice = clientes.indexOfFirst {
                        it.id == clienteAtualizado.id
                    }
                    if (indice >= 0) {
                        clientes[indice] = clienteAtualizado
                    }
                },
                onInicioClick = {
                    telaAtual = "home"
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
