
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
    onExcluirServico: (Long) -> Unit,
    onInicioClick: () -> Unit,
    onClientesClick: () -> Unit,
    onMaisClick: () -> Unit = {},
    initialServicoId: Long? = null,
    onVoltarDoServicoInicial: () -> Unit = onInicioClick
) {
    var tela by rememberSaveable(initialServicoId) {
        mutableStateOf(
            if (initialServicoId == null) {
                "lista"
            } else {
                "detalhes"
            }
        )
    }

    var servicoSelecionadoId by rememberSaveable(
        initialServicoId
    ) {
        mutableStateOf(initialServicoId)
    }

    val orcamentos = remember {
        mutableStateListOf<OrcamentoUi>().apply {
            addAll(orcamentosIniciais())
        }
    }

    val selecionado = servicos.find {
        it.id == servicoSelecionadoId
    }

    val orcamentoSelecionado = orcamentos.find {
        it.servicoId == servicoSelecionadoId
    }

    when (tela) {

        "novo" -> {
            NovoServicoScreen(
                clientes = clientes,
                onVoltar = {
                    tela = "lista"
                },
                onSalvar = { novo ->
                    val novoId =
                        (servicos.maxOfOrNull { it.id } ?: 0L) + 1L

                    onAdicionarServico(
                        novo.copy(id = novoId)
                    )

                    tela = "lista"
                }
            )
        }

        "detalhes" -> {
            if (selecionado != null) {
                DetalhesServicoScreen(
                    servico = selecionado,
                    orcamento = orcamentoSelecionado,

                    onVoltar = {
                        if (initialServicoId != null) {
                            onVoltarDoServicoInicial()
                        } else {
                            tela = "lista"
                        }
                    },

                    onAbrirOrcamento = {
                        tela = if (orcamentoSelecionado == null) {
                            "novo_orcamento"
                        } else {
                            "ver_orcamento"
                        }
                    },

                    onRegistrarRecebimento = { valor ->
                        onAtualizarServico(
                            selecionado.copy(
                                valorRecebido =
                                    selecionado.valorRecebido + valor
                            )
                        )
                    },

                    onExcluirServico = { id ->
                        orcamentos.removeAll {
                            it.servicoId == id
                        }

                        onExcluirServico(id)

                        servicoSelecionadoId = null

                        if (initialServicoId != null) {
                            onVoltarDoServicoInicial()
                        } else {
                            tela = "lista"
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    if (initialServicoId != null) {
                        onVoltarDoServicoInicial()
                    } else {
                        tela = "lista"
                    }
                }
            }
        }

        "novo_orcamento" -> {
            if (selecionado != null) {
                NovoOrcamentoScreen(
                    servico = selecionado,

                    onVoltar = {
                        tela = "detalhes"
                    },

                    onSalvar = { novo ->
                        orcamentos.removeAll {
                            it.servicoId == novo.servicoId
                        }

                        val novoId =
                            (orcamentos.maxOfOrNull {
                                it.id
                            } ?: 0L) + 1L

                        orcamentos.add(
                            novo.copy(id = novoId)
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

        else -> {
            ServicosScreen(
                servicos = servicos,

                onInicioClick = onInicioClick,

                onClientesClick = onClientesClick,

                // Nova conexão com a aba Mais
                onMaisClick = onMaisClick,

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
