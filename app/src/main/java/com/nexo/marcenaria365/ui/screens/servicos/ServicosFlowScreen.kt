
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.NovoOrcamentoScreen
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoScreen
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.orcamentosIniciais

@Composable
fun ServicosFlowScreen(
    clientes: List<ClienteUi>,
    servicos: List<ServicoUi>,
    onAdicionarServico: (ServicoUi) -> Unit,
    onAtualizarServico: (ServicoUi) -> Unit,
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit
) {
    var tela by rememberSaveable {
        mutableStateOf("lista")
    }

    var servicoSelecionadoId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    // Orçamentos associados aos serviços
    val orcamentos = remember {
        mutableStateListOf<OrcamentoUi>().apply {
            addAll(orcamentosIniciais())
        }
    }

    val servicoSelecionado = servicos.find {
        it.id == servicoSelecionadoId
    }

    val orcamentoSelecionado = orcamentos.find {
        it.servicoId == servicoSelecionadoId
    }

    when (tela) {

        // NOVO SERVIÇO
        "novo" -> {
            NovoServicoScreen(
                clientes = clientes,
                onVoltar = {
                    tela = "lista"
                },
                onSalvar = { novo ->
                    val proximoId = (
                            (servicos.maxOfOrNull { it.id } ?: 0L) + 1L
                            )

                    onAdicionarServico(
                        novo.copy(id = proximoId)
                    )

                    tela = "lista"
                }
            )
        }

        // DETALHES DO SERVIÇO
        "detalhes" -> {
            if (servicoSelecionado != null) {
                DetalhesServicoScreen(
                    servico = servicoSelecionado,
                    orcamento = orcamentoSelecionado,

                    onVoltar = {
                        tela = "lista"
                    },

                    onAbrirOrcamento = {
                        tela = if (orcamentoSelecionado == null) {
                            "novo_orcamento"
                        } else {
                            "ver_orcamento"
                        }
                    },

                    onRegistrarRecebimento = { valor ->
                        val atualizado = servicoSelecionado.copy(
                            valorRecebido =
                                servicoSelecionado.valorRecebido + valor
                        )

                        onAtualizarServico(atualizado)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    tela = "lista"
                }
            }
        }

        // CADASTRAR ORÇAMENTO
        "novo_orcamento" -> {
            if (servicoSelecionado != null) {
                NovoOrcamentoScreen(
                    servico = servicoSelecionado,

                    onVoltar = {
                        tela = "detalhes"
                    },

                    onSalvar = { novoOrcamento ->
                        // Evita dois orçamentos ativos no mesmo
                        // serviço nesta primeira versão.
                        orcamentos.removeAll {
                            it.servicoId == novoOrcamento.servicoId
                        }

                        val proximoId = (
                                (orcamentos.maxOfOrNull { it.id }
                                    ?: 0L) + 1L
                                )

                        orcamentos.add(
                            novoOrcamento.copy(
                                id = proximoId
                            )
                        )

                        tela = "ver_orcamento"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    tela = "lista"
                }
            }
        }

        // VISUALIZAR ORÇAMENTO
        "ver_orcamento" -> {
            if (orcamentoSelecionado != null) {
                OrcamentoScreen(
                    orcamento = orcamentoSelecionado,

                    onVoltar = {
                        tela = "detalhes"
                    },

                    onExcluir = { id ->
                        orcamentos.removeAll {
                            it.id == id
                        }

                        tela = "detalhes"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    tela = "detalhes"
                }
            }
        }

        // LISTA DE SERVIÇOS
        else -> {
            ServicosScreen(
                servicos = servicos,

                onInicioClick = onInicioClick,

                onClientesClick = onClientesClick,

                onNovoServicoClick = {
                    tela = "novo"
                },

                onServicoClick = { servico ->
                    servicoSelecionadoId = servico.id
                    tela = "detalhes"
                }
            )
        }
    }
}
