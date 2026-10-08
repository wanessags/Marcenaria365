
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

import com.nexo.marcenaria365.ui.screens.caixa.CaixaScreen

import com.nexo.marcenaria365.ui.screens.clientes.ClienteCadastroScreen
import com.nexo.marcenaria365.ui.screens.clientes.ClienteDetalhesScreen
import com.nexo.marcenaria365.ui.screens.clientes.ClienteEdicaoScreen
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import com.nexo.marcenaria365.ui.screens.clientes.ClientesScreen
import com.nexo.marcenaria365.ui.screens.clientes.clientesIniciais

import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import com.nexo.marcenaria365.ui.screens.servicos.ServicosFlowScreen
import com.nexo.marcenaria365.ui.screens.servicos.servicosIniciais

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen() {

    // Controle de navegação
    var telaAtual by rememberSaveable {
        mutableStateOf("onboarding")
    }

    // Nome exibido no Dashboard
    var nomeUsuario by rememberSaveable {
        mutableStateOf("Mariana")
    }

    // Cliente selecionado
    var clienteSelecionadoId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    // Serviço aberto pelos detalhes de um cliente
    var servicoVindoClienteId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    // Lista de clientes
    val clientes = remember {
        mutableStateListOf<ClienteUi>().apply {
            addAll(clientesIniciais())
        }
    }

    // Lista de serviços
    val servicos = remember {
        mutableStateListOf<ServicoUi>().apply {
            addAll(servicosIniciais())
        }
    }

    // Páginas de boas-vindas
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    val clienteSelecionado = clientes.find {
        it.id == clienteSelecionadoId
    }

    // Função para excluir serviços
    val excluirServico: (Long) -> Unit = { id ->
        servicos.removeAll {
            it.id == id
        }
    }

    // Função para adicionar serviços
    val adicionarServico: (ServicoUi) -> Unit = { novo ->
        servicos.add(novo)
    }

    // Função para atualizar serviços
    val atualizarServico: (ServicoUi) -> Unit = { atualizado ->
        val indice = servicos.indexOfFirst {
            it.id == atualizado.id
        }

        if (indice >= 0) {
            servicos[indice] = atualizado
        }
    }

    when (telaAtual) {

        // LOGIN
        "login" -> {
            BackHandler {
                telaAtual = "onboarding"
            }

            LoginScreen(
                onBackClick = {
                    telaAtual = "onboarding"
                },
                onRegisterClick = {
                    telaAtual = "cadastro_usuario"
                },
                onDemoLogin = { nome ->
                    nomeUsuario = nome
                    telaAtual = "home"
                }
            )
        }

        // CADASTRO DO USUÁRIO
        "cadastro_usuario" -> {
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

        // DASHBOARD - INÍCIO
        "home" -> {
            HomeScreen(
                nomeUsuario = nomeUsuario,

                quantidadeClientes = clientes.size,

                onClientesClick = {
                    telaAtual = "clientes"
                },

                onServicosClick = {
                    telaAtual = "servicos"
                },

                // CORREÇÃO: ABRIR A TELA CAIXA
                onCaixaClick = {
                    telaAtual = "caixa"
                },

                onSair = {
                    telaAtual = "login"
                }
            )
        }

        // CAIXA - RESUMO FINANCEIRO
        "caixa" -> {
            CaixaScreen(
                onVoltar = {
                    telaAtual = "home"
                }
            )
        }

        // LISTA DE CLIENTES
        "clientes" -> {
            ClientesScreen(
                clientes = clientes,
                servicos = servicos,

                onInicioClick = {
                    telaAtual = "home"
                },

                onNovoClienteClick = {
                    telaAtual = "cliente_cadastro"
                },

                onClienteClick = { cliente ->
                    clienteSelecionadoId = cliente.id
                    telaAtual = "cliente_detalhes"
                }
            )
        }

        // CADASTRAR CLIENTE
        "cliente_cadastro" -> {
            ClienteCadastroScreen(
                onVoltar = {
                    telaAtual = "clientes"
                },

                onSalvar = { novoCliente ->
                    val novoId =
                        (clientes.maxOfOrNull { it.id } ?: 0L) + 1L

                    clientes.add(
                        novoCliente.copy(
                            id = novoId
                        )
                    )

                    telaAtual = "clientes"
                }
            )
        }

        // DETALHES DO CLIENTE
        "cliente_detalhes" -> {
            if (clienteSelecionado != null) {
                ClienteDetalhesScreen(
                    cliente = clienteSelecionado,
                    servicos = servicos,

                    onVoltar = {
                        telaAtual = "clientes"
                    },

                    onEditar = {
                        telaAtual = "cliente_edicao"
                    },

                    onExcluir = { id ->
                        // Impede excluir clientes que possuem
                        // serviços vinculados.
                        val possuiServicos = servicos.any {
                            it.clienteId == id
                        }

                        if (!possuiServicos) {
                            clientes.removeAll {
                                it.id == id
                            }

                            clienteSelecionadoId = null
                            telaAtual = "clientes"
                        }
                    },

                    onAbrirServico = { servico ->
                        servicoVindoClienteId = servico.id
                        telaAtual = "servico_cliente"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    telaAtual = "clientes"
                }
            }
        }

        // EDITAR CLIENTE
        "cliente_edicao" -> {
            if (clienteSelecionado != null) {
                ClienteEdicaoScreen(
                    cliente = clienteSelecionado,

                    onVoltar = {
                        telaAtual = "cliente_detalhes"
                    },

                    onSalvar = { atualizado ->
                        val indice = clientes.indexOfFirst {
                            it.id == atualizado.id
                        }

                        if (indice >= 0) {
                            clientes[indice] = atualizado
                        }

                        telaAtual = "cliente_detalhes"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    telaAtual = "clientes"
                }
            }
        }

        // SERVIÇOS
        "servicos" -> {
            ServicosFlowScreen(
                clientes = clientes,
                servicos = servicos,

                onAdicionarServico = adicionarServico,

                onAtualizarServico = atualizarServico,

                onExcluirServico = excluirServico,

                onInicioClick = {
                    telaAtual = "home"
                },

                onClientesClick = {
                    telaAtual = "clientes"
                }
            )
        }

        // SERVIÇO ABERTO A PARTIR DE UM CLIENTE
        "servico_cliente" -> {
            ServicosFlowScreen(
                clientes = clientes,
                servicos = servicos,

                initialServicoId = servicoVindoClienteId,

                onAdicionarServico = adicionarServico,

                onAtualizarServico = atualizarServico,

                onExcluirServico = excluirServico,

                onInicioClick = {
                    telaAtual = "home"
                },

                onClientesClick = {
                    telaAtual = "clientes"
                },

                onVoltarDoServicoInicial = {
                    servicoVindoClienteId = null
                    telaAtual = "cliente_detalhes"
                }
            )
        }

        // BOAS-VINDAS E POLÍTICA DE PRIVACIDADE
        else -> {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pagina ->
                when (pagina) {

                    0 -> {
                        WelcomeScreen(
                            paginaAtual = pagerState.currentPage,
                            totalPaginas = 2
                        )
                    }

                    1 -> {
                        PrivacyScreen(
                            onAgreeClick = {
                                telaAtual = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}
