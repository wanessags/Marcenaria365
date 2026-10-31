package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.ItemOrcamentoUi
import com.nexo.marcenaria365.ui.screens.servicos.orcamentos.OrcamentoUi
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object OrcamentoRepository {

    private fun token(): String =
        AuthRepository.sessaoAtual?.token
            ?: throw IOException("Sessão expirada. Faça login novamente.")

    private fun verificar(resposta: ApiClient.ApiResponse): String {
        if (resposta.sucesso) return resposta.body

        throw IOException(
            when (resposta.status) {
                400 -> "Confira os dados do orçamento."
                401 -> "Sessão expirada. Faça login novamente."
                403 -> "Operação não permitida."
                404 -> "Orçamento não encontrado."
                409 -> "O orçamento possui registros vinculados."
                in 500..599 -> "Erro no servidor."
                else -> "Erro HTTP ${resposta.status}."
            }
        )
    }

    private fun statusParaApi(status: String): String =
        when (status.trim()) {
            "Em aprovação" -> "EM_APROVACAO"
            "Aprovado" -> "APROVADO"
            "Recusado" -> "RECUSADO"
            else -> status
        }

    private fun statusParaTela(status: String): String =
        when (status) {
            "RASCUNHO", "EM_APROVACAO" -> "Em aprovação"
            "APROVADO" -> "Aprovado"
            "RECUSADO" -> "Recusado"
            else -> status
        }

    private fun converter(
        json: JSONObject,
        nomeCliente: (Long) -> String
    ): OrcamentoUi {
        val itensJson = json.getJSONArray("itens")

        val itens = (0 until itensJson.length()).map { indice ->
            val item = itensJson.getJSONObject(indice)

            ItemOrcamentoUi(
                id = item.getLong("id"),
                descricao = item.getString("descricao"),
                detalhe = item.optString("detalhe", "").takeUnless {
                    it == "null"
                } ?: "",
                quantidade = item.getDouble("quantidade").toInt(),
                valorUnitario = item.getDouble("valorUnitario")
            )
        }

        return OrcamentoUi(
            id = json.getLong("id"),
            servicoId = json.getLong("servicoId"),
            clienteNome = nomeCliente(json.getLong("clienteId")),
            itens = itens,
            status = statusParaTela(json.getString("status")),
            percentualEntrada = json.optInt("percentualEntrada", 50),
            observacoes = json.optString("observacoes", "")
        )
    }

    private fun montarJson(
        orcamento: OrcamentoUi,
        clienteId: Long
    ): JSONObject = JSONObject().apply {
        put("clienteId", clienteId)
        put("servicoId", orcamento.servicoId)
        put("status", statusParaApi(orcamento.status))
        put("percentualEntrada", orcamento.percentualEntrada)
        put("observacoes", orcamento.observacoes)
        put("desconto", 0)

        val itens = JSONArray()

        orcamento.itens.forEach { item ->
            itens.put(
                JSONObject().apply {
                    put("descricao", item.descricao)
                    put("detalhe", item.detalhe)
                    put("quantidade", item.quantidade)
                    put("valorUnitario", item.valorUnitario)
                }
            )
        }

        put("itens", itens)
    }

    fun listar(
        nomeCliente: (Long) -> String
    ): Result<List<OrcamentoUi>> = runCatching {
        val corpo = verificar(
            ApiClient.get("/api/orcamentos", token())
        )

        val lista = JSONArray(corpo)

        (0 until lista.length()).map { indice ->
            converter(lista.getJSONObject(indice), nomeCliente)
        }
    }

    fun cadastrar(
        orcamento: OrcamentoUi,
        clienteId: Long,
        nomeCliente: (Long) -> String
    ): Result<OrcamentoUi> = runCatching {
        val corpo = verificar(
            ApiClient.post(
                "/api/orcamentos",
                montarJson(orcamento, clienteId),
                token()
            )
        )

        converter(JSONObject(corpo), nomeCliente)
    }

    fun excluir(id: Long): Result<Unit> = runCatching {
        verificar(
            ApiClient.delete("/api/orcamentos/$id", token())
        )
        Unit
    }
}