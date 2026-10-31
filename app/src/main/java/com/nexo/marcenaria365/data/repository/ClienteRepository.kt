package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ClienteRepository {

    data class Cliente(
        val id: Long,
        val nome: String,
        val telefone: String,
        val email: String,
        val endereco: String,
        val observacoes: String
    )

    data class ClienteRequest(
        val nome: String,
        val telefone: String,
        val email: String,
        val endereco: String,
        val observacoes: String
    )

    private fun token(): String {
        return AuthRepository.sessaoAtual?.token
            ?: throw IOException(
                "Sua sessão não está ativa. Faça login novamente."
            )
    }

    private fun JSONObject.texto(campo: String): String {
        return optString(campo, "").takeIf {
            it != "null"
        } ?: ""
    }

    private fun converter(json: JSONObject): Cliente {
        return Cliente(
            id = json.getLong("id"),
            nome = json.texto("nome"),
            telefone = json.texto("telefone"),
            email = json.texto("email"),
            endereco = json.texto("endereco"),
            observacoes = json.texto("observacoes")
        )
    }

    private fun montarJson(dados: ClienteRequest): JSONObject {
        return JSONObject().apply {
            put("nome", dados.nome.trim())
            put("telefone", dados.telefone)
            put("email", dados.email)
            put("endereco", dados.endereco)
            put("observacoes", dados.observacoes)
        }
    }

    private fun verificarErro(status: Int): IOException {
        val mensagem = when (status) {
            400 -> "Confira os dados do cliente."
            401 -> "Sua sessão expirou. Faça login novamente."
            403 -> "Você não tem permissão para esta operação."
            404 -> "Cliente não encontrado."
            409 -> "Este cliente possui registros vinculados e não pode ser excluído."
            in 500..599 -> "O servidor está indisponível."
            else -> "Não foi possível concluir a operação (HTTP $status)."
        }

        return IOException(mensagem)
    }

    fun listar(): Result<List<Cliente>> {
        return runCatching {
            val resposta = ApiClient.get(
                endpoint = "/api/clientes",
                token = token()
            )

            if (!resposta.sucesso) {
                throw verificarErro(resposta.status)
            }

            val array = JSONArray(resposta.body)

            (0 until array.length()).map { indice ->
                converter(array.getJSONObject(indice))
            }
        }
    }

    fun buscar(id: Long): Result<Cliente> {
        return runCatching {
            val resposta = ApiClient.get(
                endpoint = "/api/clientes/$id",
                token = token()
            )

            if (!resposta.sucesso) {
                throw verificarErro(resposta.status)
            }

            converter(JSONObject(resposta.body))
        }
    }

    fun cadastrar(dados: ClienteRequest): Result<Cliente> {
        return runCatching {
            val resposta = ApiClient.post(
                endpoint = "/api/clientes",
                dados = montarJson(dados),
                token = token()
            )

            if (!resposta.sucesso) {
                throw verificarErro(resposta.status)
            }

            converter(JSONObject(resposta.body))
        }
    }

    fun editar(
        id: Long,
        dados: ClienteRequest
    ): Result<Cliente> {
        return runCatching {
            val resposta = ApiClient.put(
                endpoint = "/api/clientes/$id",
                dados = montarJson(dados),
                token = token()
            )

            if (!resposta.sucesso) {
                throw verificarErro(resposta.status)
            }

            converter(JSONObject(resposta.body))
        }
    }

    fun excluir(id: Long): Result<Unit> {
        return runCatching {
            val resposta = ApiClient.delete(
                endpoint = "/api/clientes/$id",
                token = token()
            )

            if (!resposta.sucesso) {
                throw verificarErro(resposta.status)
            }

            Unit
        }
    }
}