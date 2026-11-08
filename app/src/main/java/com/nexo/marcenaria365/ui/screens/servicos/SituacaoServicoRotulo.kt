
package com.nexo.marcenaria365.ui.screens.servicos

import java.util.Locale

/**
 * Retorna um rótulo legível para a situação do serviço.
 *
 * Esta função altera somente o texto apresentado
 * na interface e ao leitor de tela.
 *
 * Não modifica o status original, os filtros,
 * os recebimentos ou os dados do banco.
 */
fun rotuloSituacaoServico(status: String): String {
    val texto = status.trim()

    if (texto.isBlank()) {
        return "Não informado"
    }

    return when (texto.uppercase(Locale.ROOT)) {
        "ORÇAMENTO",
        "ORCAMENTO" -> "Orçamento"

        "AGUARDANDO" -> "Aguardando"

        "EM PRODUÇÃO",
        "EM PRODUCAO" -> "Em produção"

        "CONCLUÍDO",
        "CONCLUIDO" -> "Concluído"

        "CANCELADO" -> "Cancelado"

        else -> texto
    }
}
