
package com.nexo.marcenaria365.ui.screens.caixa

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

enum class TipoMovimentacao {
    ENTRADA,
    SAIDA
}

data class MovimentacaoUi(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val valor: Double,
    val tipo: TipoMovimentacao,
    val dia: Int,
    val mes: Int,
    val ano: Int,
    val servicoId: Long? = null
)

fun moedaCaixa(valor: Double): String {
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR")
    ).format(valor)
}

fun dataCaixa(movimentacao: MovimentacaoUi): String {
    return "%02d/%02d/%04d".format(
        Locale.ROOT,
        movimentacao.dia,
        movimentacao.mes,
        movimentacao.ano
    )
}

// Dados fictícios para apresentação do aplicativo.
// Posteriormente, serão substituídos pelos registros reais.
fun movimentacoesIniciais(): List<MovimentacaoUi> {
    val calendario = Calendar.getInstance()
    val mes = calendario.get(Calendar.MONTH) + 1
    val ano = calendario.get(Calendar.YEAR)
    val hoje = calendario.get(Calendar.DAY_OF_MONTH)

    fun diaAnterior(dias: Int): Int {
        return (hoje - dias).coerceAtLeast(1)
    }

    return listOf(
        MovimentacaoUi(
            id = 1,
            titulo = "Pagamento recebido",
            descricao = "Ana Silva · Cozinha planejada",
            valor = 1500.0,
            tipo = TipoMovimentacao.ENTRADA,
            dia = hoje,
            mes = mes,
            ano = ano,
            servicoId = 1
        ),
        MovimentacaoUi(
            id = 2,
            titulo = "Compra de materiais",
            descricao = "MDF e ferragens · Cozinha planejada",
            valor = 850.0,
            tipo = TipoMovimentacao.SAIDA,
            dia = diaAnterior(1),
            mes = mes,
            ano = ano,
            servicoId = 1
        ),
        MovimentacaoUi(
            id = 3,
            titulo = "Pagamento recebido",
            descricao = "Bruno Souza · Rack para sala",
            valor = 1000.0,
            tipo = TipoMovimentacao.ENTRADA,
            dia = diaAnterior(2),
            mes = mes,
            ano = ano,
            servicoId = 2
        ),
        MovimentacaoUi(
            id = 4,
            titulo = "Pagamento recebido",
            descricao = "Carlos Fernandes · Guarda-roupa",
            valor = 2350.0,
            tipo = TipoMovimentacao.ENTRADA,
            dia = diaAnterior(3),
            mes = mes,
            ano = ano,
            servicoId = 3
        ),
        MovimentacaoUi(
            id = 5,
            titulo = "Pagamento recebido",
            descricao = "Serviços realizados",
            valor = 2000.0,
            tipo = TipoMovimentacao.ENTRADA,
            dia = diaAnterior(4),
            mes = mes,
            ano = ano
        ),
        MovimentacaoUi(
            id = 6,
            titulo = "Outras despesas",
            descricao = "Transporte e fornecedores",
            valor = 1150.0,
            tipo = TipoMovimentacao.SAIDA,
            dia = diaAnterior(5),
            mes = mes,
            ano = ano
        )
    )
}
