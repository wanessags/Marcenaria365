
package com.nexo.marcenaria365.ui.screens.servicos

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi

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

    val selecionado = servicos.find {
        it.id == servicoSelecionadoId
    }

    when (tela) {
        "novo" -> {
            NovoServicoScreen(
                clientes = clientes,
                onVoltar = {
                    tela = "lista"
                },
                onSalvar = { novo ->
                    val proximoId =
                        (servicos.maxOfOrNull { it.id } ?: 0L) + 1L

                    onAdicionarServico(
                        novo.copy(id = proximoId)
                    )

                    tela = "lista"
                }
            )
        }

        "detalhes" -> {
            if (selecionado != null) {
                DetalhesServicoScreen(
                    servico = selecionado,
                    onVoltar = {
                        tela = "lista"
                    },
                    onRegistrarRecebimento = { valor ->
                        val atualizado = selecionado.copy(
                            valorRecebido =
                                selecionado.valorRecebido + valor
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
