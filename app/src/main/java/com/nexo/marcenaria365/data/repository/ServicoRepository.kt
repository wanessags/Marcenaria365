package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import com.nexo.marcenaria365.ui.screens.servicos.ServicoUi
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ServicoRepository {

    private val formatoBR =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")

    private fun token(): String {
        return AuthRepository.sessaoAtual?.token
            ?: throw IOException(
                "Sua sessão expirou. Faça login novamente."
            )
    }

    private fun paraDataApi(data: String): String? {
        if (data.isBlank()) return null

        return LocalDate.parse(data, formatoBR).toString()
    }

    private fun paraDataTela(data: String?): String {
        if (data.isNullOrBlank() || data == "null") {
            return ""
        }

        return LocalDate.parse(data).format(formatoBR)
    }

    private fun statusParaApi(status: String): String {
        return when (status.trim()) {
            "Orçamento" -> "AGUARDANDO"
            "Em produção" -> "EM_ANDAMENTO"
            "Concluído" -> "CONCLUIDO"
            "Cancelado" -> "CANCELADO"
            else -> status
        }
    }

    private fun statusParaTela(status: String): String {
        return when (status) {
            "AGUARDANDO" -> "Orçamento"
            "EM_ANDAMENTO" -> "Em produção"
            "CONCLUIDO" -> "Concluído"
            "CANCELADO" -> "Cancelado"
            else -> status
        }
    }

    private fun converter(
        json: JSONObject,
        clientes: List<ClienteRepository.Cliente>
    ): ServicoUi {
        val clienteId = json.getLong("clienteId")

        return ServicoUi(
            id = json.getLong("id"),
            clienteId = clienteId,
            clienteNome = clientes.find {
                it.id == clienteId
            }?.nome ?: "Cliente",
            titulo = json.optString("titulo", ""),
            descricao = json.optString("descricao", ""),
            entrega = paraDataTela(
                json.optString("prazo", "")
            ),
            valorEstimado = json.optDouble(
                "valorCobrado",
                0.0
            ),
            valorRecebido = 0.0,
            status = statusParaTela(
                json.optString("status", "AGUARDANDO")
            )
        )
    }

    private fun montarJson(
        servico: ServicoUi,
        existente: JSONObject? = null
    ): JSONObject {
        return JSONObject().apply {
            put("clienteId", servico.clienteId)
            put("titulo", servico.titulo.trim())
            put("descricao", servico.descricao)
            put("status", statusParaApi(servico.status))

            put(
                "prazo",
                paraDataApi(servico.entrega)
                    ?: JSONObject.NULL
            )

            put(
                "valorCobrado",
                servico.valorEstimado
            )

            put(
                "custoPrevisto",
                existente?.optDouble("custoPrevisto", 0.0)
                    ?: 0.0
            )

            put(
                "dataInicio",
                existente?.opt("dataInicio")
                    ?: JSONObject.NULL
            )

            put(
                "dataConclusao",
                existente?.opt("dataConclusao")
                    ?: JSONObject.NULL
            )

            put(
                "observacoes",
                existente?.opt("observacoes")
                    ?: JSONObject.NULL
            )
        }
    }

    private fun verificar(
        resposta: ApiClient.ApiResponse
    ): String {
        if (resposta.sucesso) {
            return resposta.body
        }

        throw IOException(
            when (resposta.status) {
                400 -> "Confira os dados do serviço."
                401 -> "Sessão expirada. Entre novamente."
                403 -> "Operação não permitida."
                404 -> "Serviço não encontrado."
                409 -> "O serviço possui registros vinculados."
                in 500..599 -> "Erro no servidor."
                else -> "Erro HTTP ${resposta.status}."
            }
        )
    }

    fun listar(
        clientes: List<ClienteRepository.Cliente>
    ): Result<List<ServicoUi>> = runCatching {

        val corpo = verificar(
            ApiClient.get("/api/servicos", token())
        )

        val lista = JSONArray(corpo)

        (0 until lista.length()).map { indice ->
            converter(
                lista.getJSONObject(indice),
                clientes
            )
        }
    }

    fun cadastrar(
        servico: ServicoUi,
        clientes: List<ClienteRepository.Cliente>
    ): Result<ServicoUi> = runCatching {

        val corpo = verificar(
            ApiClient.post(
                "/api/servicos",
                montarJson(servico),
                token()
            )
        )

        converter(JSONObject(corpo), clientes)
    }

    fun editar(
        servico: ServicoUi,
        clientes: List<ClienteRepository.Cliente>
    ): Result<ServicoUi> = runCatching {

        val atual = JSONObject(
            verificar(
                ApiClient.get(
                    "/api/servicos/${servico.id}",
                    token()
                )
            )
        )

        val corpo = verificar(
            ApiClient.put(
                "/api/servicos/${servico.id}",
                montarJson(servico, atual),
                token()
            )
        )

        converter(JSONObject(corpo), clientes)
    }

    fun excluir(id: Long): Result<Unit> = runCatching {
        verificar(
            ApiClient.delete(
                "/api/servicos/$id",
                token()
            )
        )

        Unit
    }
}