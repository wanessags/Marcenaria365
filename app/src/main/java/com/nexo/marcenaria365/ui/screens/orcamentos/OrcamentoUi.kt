
package com.nexo.marcenaria365.ui.screens.servicos.orcamentos

import java.text.NumberFormat
import java.util.Locale

data class ItemOrcamentoUi(
    val id: Long,
    val descricao: String,
    val detalhe: String,
    val quantidade: Int,
    val valorUnitario: Double
) {
    val subtotal: Double
        get() = quantidade * valorUnitario
}

data class OrcamentoUi(
    val id: Long,
    val servicoId: Long,
    val clienteNome: String,
    val itens: List<ItemOrcamentoUi>,
    val status: String = "Em aprovação",
    val percentualEntrada: Int = 50,
    val observacoes: String = ""
) {
    val total: Double
        get() = itens.sumOf { it.subtotal }

    val entrada: Double
        get() = total * percentualEntrada / 100.0

    val saldo: Double
        get() = total - entrada
}

object StatusOrcamento {
    const val EM_APROVACAO = "Em aprovação"
    const val APROVADO = "Aprovado"
    const val RECUSADO = "Recusado"
}

fun formatarMoedaOrcamento(valor: Double): String {
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR")
    ).format(valor)
}

fun formatarNumeroOrcamento(numero: Long): String {
    return numero.toString().padStart(4, '0')
}

fun orcamentosIniciais(): List<OrcamentoUi> {
    return listOf(
        OrcamentoUi(
            id = 3L,
            servicoId = 1L,
            clienteNome = "Ana Silva",
            status = StatusOrcamento.EM_APROVACAO,
            percentualEntrada = 50,
            itens = listOf(
                ItemOrcamentoUi(
                    id = 1L,
                    descricao = "Materiais",
                    detalhe = "MDF, ferragens e acabamentos",
                    quantidade = 1,
                    valorUnitario = 1200.0
                ),
                ItemOrcamentoUi(
                    id = 2L,
                    descricao = "Mão de obra",
                    detalhe = "Produção e montagem",
                    quantidade = 1,
                    valorUnitario = 1000.0
                ),
                ItemOrcamentoUi(
                    id = 3L,
                    descricao = "Outros custos",
                    detalhe = "Transporte e instalação",
                    quantidade = 1,
                    valorUnitario = 300.0
                )
            ),
            observacoes = "Entrada na aprovação do orçamento. " +
                    "Saldo na entrega do serviço."
        )
    )
}
