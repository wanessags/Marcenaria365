
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.nexo.marcenaria365.ui.screens.caixa.CaixaScreen

import com.nexo.marcenaria365.ui.screens.clientes.ClienteCadastroScreen
import com.nexo.marcenaria365.ui.screens.clientes.ClienteDetalhesScreen
import com.nexo.marcenaria365.ui.screens.clientes.ClienteEdicaoScreen
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import com.nexo.marcenaria365.ui.screens.clientes.ClientesScreen
import com.nexo.marcenaria365.ui.screens.clientes.clientesIniciais

import com.nexo.marcenaria365.ui.screens.mais.MaisScreen

import com.nexo.marcenaria365.ui.screens.perfil.MeuPerfilScreen

import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import com.nexo.marcenaria365.ui.screens.servicos.ServicosFlowScreen
import com.nexo.marcenaria365.ui.screens.servicos.servicosIniciais

@Composable
fun OnboardingScreen() {

    val context = LocalContext.current

    val preferencias = remember(context) {
        context.getSharedPreferences(
            "marcenaria365_perfil",
            android.content.Context.MODE_PRIVATE
        )
    }

    var telaAtual by rememberSaveable {
        mutableStateOf("onboarding")
    }

    var nomeUsuario by rememberSaveable {
        mutableStateOf(
            preferencias.getString(
                "nome",
                "Mariana"
            ) ?: "Mariana"
        )
    }

    var emailUsuario by rememberSaveable {
        mutableStateOf(
            preferencias.getString(
                "email",
                ""
            ) ?: ""
        )
    }

    var fotoUsuario by rememberSaveable {
        mutableStateOf(
            preferencias.getString(
                "foto",
                null
            )
        )
    }

    var clienteSelecionadoId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var servicoVindoClienteId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val clientes = remember {
        mutableStateListOf<ClienteUi>().apply {
            addAll(clientesIniciais())
        }
    }

    val servicos = remember {
        mutableStateListOf<ServicoUi>().apply {
            addAll(servicosIniciais())
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    val clienteSelecionado = clientes.find {
        it.id == clienteSelecionadoId
    }

    val adicionarServico: (ServicoUi) -> Unit = { novo ->
        servicos.add(novo)
    }

    val atualizarServico: (ServicoUi) -> Unit = { atualizado ->
        val indice = servicos.indexOfFirst {
            it.id == atualizado.id
        }

        if (indice >= 0) {
            servicos[indice] = atualizado
        }
    }

    val excluirServico: (Long) -> Unit = { id ->
        servicos.removeAll {
            it.id == id
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
                    val nomeSalvo = preferencias.getString(
                        "nome",
                        null
                    )

                    nomeUsuario = nomeSalvo
                        ?.takeIf { it.isNotBlank() }
                        ?: nome

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

        // DASHBOARD
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
                onCaixaClick = {
                    telaAtual = "caixa"
                },
                onMaisClick = {
                    telaAtual = "mais"
                },
                onSair = {
                    telaAtual = "login"
                }
            )
        }

        // CAIXA
        "caixa" -> {
            CaixaScreen(
                onVoltar = {
                    telaAtual = "home"
                }
            )
        }

        // CLIENTES
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
                },
                onServicosClick = {
                    telaAtual = "servicos"
                },
                onMaisClick = {
                    telaAtual = "mais"
                }
            )
        }

        // CADASTRO DE CLIENTE
        "cliente_cadastro" -> {
            ClienteCadastroScreen(
                onVoltar = {
                    telaAtual = "clientes"
                },
                onSalvar = { novoCliente ->
                    val novoId =
                        (clientes.maxOfOrNull { it.id } ?: 0L) + 1L

                    clientes.add(
                        novoCliente.copy(id = novoId)
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

        // EDIÇÃO DE CLIENTE
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

        // SERVIÇO ABERTO PELO CLIENTE
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

        // MENU MAIS
        "mais" -> {
            MaisScreen(
                nomeUsuario = nomeUsuario,
                emailUsuario = emailUsuario,
                fotoUsuario = fotoUsuario,

                onPerfilClick = {
                    telaAtual = "meu_perfil"
                },

                onInicioClick = {
                    telaAtual = "home"
                },

                onClientesClick = {
                    telaAtual = "clientes"
                },

                onServicosClick = {
                    telaAtual = "servicos"
                },

                onPoliticaClick = {
                    telaAtual = "politica_mais"
                },

                onSair = {
                    telaAtual = "login"
                }
            )
        }

        // NOVA TELA: MEU PERFIL
        "meu_perfil" -> {
            MeuPerfilScreen(
                nomeAtual = nomeUsuario,
                emailAtual = emailUsuario,
                fotoAtual = fotoUsuario,

                onVoltar = {
                    telaAtual = "mais"
                },

                onSalvar = { novoNome, novoEmail, novaFoto ->

                    preferencias.edit()
                        .putString("nome", novoNome)
                        .putString("email", novoEmail)
                        .putString("foto", novaFoto)
                        .apply()

                    nomeUsuario = novoNome
                    emailUsuario = novoEmail
                    fotoUsuario = novaFoto

                    telaAtual = "mais"
                }
            )
        }

        // POLÍTICA PELO MENU MAIS
        "politica_mais" -> {
            TelaPoliticaIntegrada(
                onVoltar = {
                    telaAtual = "mais"
                }
            )
        }

        // POLÍTICA ANTES DO LOGIN
        "politica_entrada" -> {
            TelaPoliticaIntegrada(
                onVoltar = {
                    telaAtual = "onboarding"
                }
            )
        }

        // ABERTURA E ACEITE DE TERMOS
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
                            },
                            onReadPolicy = {
                                telaAtual = "politica_entrada"
                            }
                        )
                    }
                }
            }
        }
    }
}

// POLÍTICA DE PRIVACIDADE INTEGRADA

@Composable
private fun TelaPoliticaIntegrada(
    onVoltar: () -> Unit
) {
    val azul = Color(0xFF193447)
    val fundo = Color(0xFFFAF6F0)
    val cinza = Color(0xFF929292)

    BackHandler(onBack = onVoltar)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = fundo,

        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(azul)
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier.align(
                        Alignment.CenterStart
                    )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Política de Privacidade",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 55.dp),
                    textAlign = TextAlign.Center
                )
            }
        },

        bottomBar = {
            Button(
                onClick = onVoltar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 14.dp
                    )
                    .height(52.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = azul,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Entendi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 25.dp)
        ) {
            Spacer(Modifier.height(25.dp))

            SecaoPoliticaIntegrada(
                titulo = "1. Coleta de informações",
                descricao = "O aplicativo permite cadastrar " +
                        "informações de clientes, contatos, " +
                        "serviços, orçamentos e movimentações financeiras.",
                azul = azul,
                cinza = cinza
            )

            SecaoPoliticaIntegrada(
                titulo = "2. Uso dos dados",
                descricao = "As informações cadastradas auxiliam " +
                        "na organização das atividades da marcenaria, " +
                        "no acompanhamento de serviços e no " +
                        "controle financeiro.",
                azul = azul,
                cinza = cinza
            )

            SecaoPoliticaIntegrada(
                titulo = "3. Compartilhamento",
                descricao = "O usuário pode escolher compartilhar " +
                        "um orçamento por outros aplicativos. " +
                        "O conteúdo compartilhado depende da ação realizada.",
                azul = azul,
                cinza = cinza
            )

            SecaoPoliticaIntegrada(
                titulo = "4. Armazenamento e segurança",
                descricao = "O Marcenaria 365 está em desenvolvimento. " +
                        "Esta versão utiliza dados fictícios e " +
                        "armazenamento temporário em algumas funcionalidades. " +
                        "Medidas de segurança deverão ser implementadas " +
                        "antes do uso real.",
                azul = azul,
                cinza = cinza
            )

            SecaoPoliticaIntegrada(
                titulo = "5. Seus direitos",
                descricao = "A LGPD prevê direitos como acesso " +
                        "e correção dos dados pessoais e, " +
                        "nas hipóteses legais, sua exclusão. " +
                        "Os procedimentos para atender às solicitações " +
                        "serão definidos antes da disponibilização " +
                        "do aplicativo.",
                azul = azul,
                cinza = cinza
            )

            SecaoPoliticaIntegrada(
                titulo = "6. Contato",
                descricao = "Um canal oficial de contato sobre " +
                        "privacidade ainda será definido pelo " +
                        "responsável pelo aplicativo.",
                azul = azul,
                cinza = cinza
            )

            Text(
                text = "Documento preliminar do projeto acadêmico " +
                        "Marcenaria 365. Deve ser revisado antes " +
                        "de utilizar dados pessoais reais.",
                color = cinza,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(25.dp))
        }
    }
}

@Composable
private fun SecaoPoliticaIntegrada(
    titulo: String,
    descricao: String,
    azul: Color,
    cinza: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 26.dp)
    ) {
        Text(
            text = titulo,
            color = azul,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(9.dp))

        Text(
            text = descricao,
            color = cinza,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
