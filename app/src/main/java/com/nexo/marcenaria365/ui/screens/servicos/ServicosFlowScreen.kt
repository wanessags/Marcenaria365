
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.NovoOrcamentoScreen
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoScreen
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoUi
import com.nexo.marcenaria365.data.repository.OrcamentoRepository
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var servicoSelecionadoId by rememberSaveable(initialServicoId) {
        mutableStateOf(initialServicoId)
    }

    val orcamentos = remember {
        mutableStateListOf<OrcamentoUi>()
    }

    val scope = rememberCoroutineScope()

    var erroOrcamento by remember {
        mutableStateOf<String?>(null)
    }

    var processandoOrcamento by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val nomes = clientes.associate { it.id to it.nome }

        val resultado = withContext(Dispatchers.IO) {
            OrcamentoRepository.listar { clienteId ->
                nomes[clienteId] ?: "Cliente"
            }
        }

        resultado.fold(
            onSuccess = { lista ->
                orcamentos.clear()
                orcamentos.addAll(lista)
                erroOrcamento = null
            },
            onFailure = { erro ->
                erroOrcamento =
                    erro.message ?: "Erro ao carregar orcamentos."
            }
        )
    }

    if (erroOrcamento != null) {
        AlertDialog(
            onDismissRequest = { erroOrcamento = null },
            title = { Text("Orcamentos") },
            text = {
                Text(
                    erroOrcamento
                        ?: "Nao foi possivel concluir a operacao."
                )
            },
            confirmButton = {
                TextButton(onClick = { erroOrcamento = null }) {
                    Text("Entendi")
                }
            }
        )
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

        "editar" -> {
            if (selecionado != null) {
                EditarServicoScreen(
                    servico = selecionado,

                    onVoltar = {
                        tela = "detalhes"
                    },

                    onSalvar = { atualizado ->
                        onAtualizarServico(atualizado)
                        tela = "detalhes"
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    tela = "lista"
                }
            }
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

                    onEditarServico = {
                        tela = "editar"
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
                        if (!processandoOrcamento) {
                            processandoOrcamento = true

                            val clienteId = selecionado.clienteId
                            val nomes = clientes.associate {
                                it.id to it.nome
                            }

                            scope.launch {
                                val resultado = withContext(Dispatchers.IO) {
                                    OrcamentoRepository.cadastrar(
                                        orcamento = novo,
                                        clienteId = clienteId,
                                        nomeCliente = { id ->
                                            nomes[id] ?: "Cliente"
                                        }
                                    )
                                }

                                processandoOrcamento = false

                                resultado.fold(
                                    onSuccess = { salvo ->
                                        orcamentos.removeAll {
                                            it.servicoId == salvo.servicoId
                                        }
                                        orcamentos.add(salvo)
                                        erroOrcamento = null
                                        tela = "ver_orcamento"
                                    },
                                    onFailure = { erro ->
                                        erroOrcamento =
                                            erro.message
                                                ?: "Erro ao salvar orcamento."
                                    }
                                )
                            }
                        }
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
                        if (!processandoOrcamento) {
                            processandoOrcamento = true

                            scope.launch {
                                val resultado = withContext(Dispatchers.IO) {
                                    OrcamentoRepository.excluir(id)
                                }

                                processandoOrcamento = false

                                resultado.fold(
                                    onSuccess = {
                                        orcamentos.removeAll {
                                            it.id == id
                                        }
                                        erroOrcamento = null
                                        tela = "detalhes"
                                    },
                                    onFailure = { erro ->
                                        erroOrcamento =
                                            erro.message
                                                ?: "Erro ao excluir orcamento."
                                    }
                                )
                            }
                        }
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
