package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate

data class MovimentacaoCaixa(
    val id: Long,
    val tipo: String,
    val categoria: String,
    val descricao: String,
    val valor: Double,
    val dataMovimentacao: String,
    val recebimentoId: Long?
)

object MovimentacaoCaixaRepository {

    private fun token(): String =
        AuthRepository.sessaoAtual?.token
            ?: throw IOException("Sessão expirada. Faça login novamente.")

    private fun verificar(resposta: ApiClient.ApiResponse): String {
        if (resposta.sucesso) return resposta.body

        throw IOException(
            when (resposta.status) {
                400 -> "Confira os dados da movimentação."
                401 -> "Sessão expirada. Faça login novamente."
                403 -> "Operação não permitida."
                404 -> "Movimentação não encontrada."
                409 -> "Não foi possível concluir a operação."
                in 500..599 -> "Erro no servidor."
                else -> "Erro HTTP ${resposta.status}."
            }
        )
    }

    private fun converter(json: JSONObject): MovimentacaoCaixa =
        MovimentacaoCaixa(
            id = json.getLong("id"),
            tipo = json.getString("tipo"),
            categoria = json.getString("categoria"),
            descricao = json.getString("descricao"),
            valor = json.getDouble("valor"),
            dataMovimentacao = json.getString("dataMovimentacao"),
            recebimentoId = if (json.isNull("recebimentoId")) {
                null
            } else {
                json.getLong("recebimentoId")
            }
        )

    fun listar(): Result<List<MovimentacaoCaixa>> = runCatching {
        val corpo = verificar(
            ApiClient.get("/api/caixa", token())
        )

        val lista = JSONArray(corpo)

        (0 until lista.length()).map { indice ->
            converter(lista.getJSONObject(indice))
        }
    }

    fun cadastrar(
        tipo: String,
        categoria: String,
        descricao: String,
        valor: Double
    ): Result<MovimentacaoCaixa> = runCatching {
        require(tipo == "ENTRADA" || tipo == "SAIDA") {
            "Tipo de movimentação inválido."
        }

        require(categoria.isNotBlank() && categoria.length <= 80) {
            "Informe uma categoria com até 80 caracteres."
        }

        require(descricao.isNotBlank() && descricao.length <= 200) {
            "Informe uma descrição com até 200 caracteres."
        }

        require(valor.isFinite() && valor > 0.0) {
            "Informe um valor positivo."
        }

        val dados = JSONObject().apply {
            put("tipo", tipo)
            put("categoria", categoria.trim())
            put("descricao", descricao.trim())
            put("valor", valor)
            put("dataMovimentacao", LocalDate.now().toString())
        }

        val corpo = verificar(
            ApiClient.post("/api/caixa", dados, token())
        )

        converter(JSONObject(corpo))
    }

    fun excluir(id: Long): Result<Unit> = runCatching {
        verificar(
            ApiClient.delete("/api/caixa/$id", token())
        )
        Unit
    }
}