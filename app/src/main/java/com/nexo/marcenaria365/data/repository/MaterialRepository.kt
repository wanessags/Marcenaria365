
package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class MaterialItem(
    val id: Long,
    val nome: String,
    val categoria: String,
    val unidade: String,
    val quantidadeEstoque: Double,
    val estoqueMinimo: Double,
    val custoUnitario: Double
)

data class MaterialDados(
    val nome: String,
    val categoria: String,
    val unidade: String,
    val quantidadeEstoque: Double,
    val estoqueMinimo: Double,
    val custoUnitario: Double
)

object MaterialRepository {

    private const val ENDPOINT = "/api/materiais"

    private fun token(): String {
        return AuthRepository.sessaoAtual?.token
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException(
                "Sessão expirada. Faça login novamente."
            )
    }

    private fun verificarErro(status: Int) {
        throw IOException(
            when (status) {
                400, 422 -> "Confira os dados do material."
                401 -> "Sessão expirada. Faça login novamente."
                403 -> "Você não tem permissão para esta operação."
                404 -> "Material não encontrado."
                in 500..599 -> "Erro no servidor. Tente novamente."
                else -> "Erro na API (HTTP $status)."
            }
        )
    }

    private fun converter(json: JSONObject): MaterialItem {
        return MaterialItem(
            id = json.getLong("id"),
            nome = json.getString("nome"),
            categoria = json.optString("categoria", ""),
            unidade = json.getString("unidade"),
            quantidadeEstoque = json.optDouble(
                "quantidadeEstoque", 0.0
            ),
            estoqueMinimo = json.optDouble(
                "estoqueMinimo", 0.0
            ),
            custoUnitario = json.optDouble(
                "custoUnitario", 0.0
            )
        )
    }

    private fun criarJson(dados: MaterialDados): JSONObject {
        return JSONObject().apply {
            put("nome", dados.nome.trim())
            put("categoria", dados.categoria.trim())
            put("unidade", dados.unidade.trim())
            put("quantidadeEstoque", dados.quantidadeEstoque)
            put("estoqueMinimo", dados.estoqueMinimo)
            put("custoUnitario", dados.custoUnitario)
        }
    }

    fun listar(): Result<List<MaterialItem>> {
        return runCatching {
            val resposta = ApiClient.get(
                endpoint = ENDPOINT,
                token = token()
            )

            if (!resposta.sucesso) {
                verificarErro(resposta.status)
            }

            val array = JSONArray(resposta.body)
            val materiais = mutableListOf<MaterialItem>()

            for (indice in 0 until array.length()) {
                materiais.add(
                    converter(array.getJSONObject(indice))
                )
            }

            materiais
        }
    }

    fun cadastrar(
        dados: MaterialDados
    ): Result<MaterialItem> {
        return runCatching {
            val resposta = ApiClient.post(
                endpoint = ENDPOINT,
                dados = criarJson(dados),
                token = token()
            )

            if (!resposta.sucesso) {
                verificarErro(resposta.status)
            }

            converter(JSONObject(resposta.body))
        }
    }

    fun editar(
        id: Long,
        dados: MaterialDados
    ): Result<MaterialItem> {
        return runCatching {
            val resposta = ApiClient.put(
                endpoint = "$ENDPOINT/$id",
                dados = criarJson(dados),
                token = token()
            )

            if (!resposta.sucesso) {
                verificarErro(resposta.status)
            }

            converter(JSONObject(resposta.body))
        }
    }

    fun excluir(id: Long): Result<Unit> {
        return runCatching {
            val resposta = ApiClient.delete(
                endpoint = "$ENDPOINT/$id",
                token = token()
            )

            if (!resposta.sucesso) {
                verificarErro(resposta.status)
            }

            Unit
        }
    }
}
