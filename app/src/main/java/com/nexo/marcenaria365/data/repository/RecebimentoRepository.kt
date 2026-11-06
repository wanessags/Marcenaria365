
package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate

data class Recebimento(
    val id: Long,
    val servicoId: Long,
    val valor: Double,
    val formaPagamento: String?,
    val status: String,
    val dataPrevista: String?,
    val dataRecebimento: String?,
    val observacoes: String?
)

object RecebimentoRepository {

    private fun token(): String =
        AuthRepository.sessaoAtual?.token
            ?: throw IOException(
                "Sessão expirada. Faça login novamente."
            )

    private fun verificar(
        resposta: ApiClient.ApiResponse
    ): String {
        if (resposta.sucesso) return resposta.body

        val mensagemServidor = runCatching {
            JSONObject(resposta.body)
                .optString("erro")
                .ifBlank { null }
        }.getOrNull()

        throw IOException(
            when (resposta.status) {
                400 -> mensagemServidor
                    ?: "Confira os dados do recebimento."

                401 -> "Sessão expirada. Faça login novamente."

                403 -> "Operação não permitida."

                404 -> "Recebimento não encontrado."

                409 -> mensagemServidor
                    ?: "O recebimento possui movimentações vinculadas."

                in 500..599 -> "Erro no servidor."

                else -> mensagemServidor
                    ?: "Erro HTTP ${resposta.status}."
            }
        )
    }

    private fun textoOpcional(
        json: JSONObject,
        campo: String
    ): String? =
        if (json.isNull(campo)) {
            null
        } else {
            json.optString(campo).ifBlank { null }
        }

    private fun converter(json: JSONObject): Recebimento =
        Recebimento(
            id = json.getLong("id"),
            servicoId = json.getLong("servicoId"),
            valor = json.getDouble("valor"),
            formaPagamento = textoOpcional(
                json,
                "formaPagamento"
            ),
            status = json.getString("status"),
            dataPrevista = textoOpcional(
                json,
                "dataPrevista"
            ),
            dataRecebimento = textoOpcional(
                json,
                "dataRecebimento"
            ),
            observacoes = textoOpcional(
                json,
                "observacoes"
            )
        )

    fun listar(): Result<List<Recebimento>> =
        runCatching {
            val corpo = verificar(
                ApiClient.get(
                    "/api/recebimentos",
                    token()
                )
            )

            val lista = JSONArray(corpo)

            (0 until lista.length()).map { indice ->
                converter(lista.getJSONObject(indice))
            }
        }

    fun listarPorServico(
        servicoId: Long
    ): Result<List<Recebimento>> =
        runCatching {
            require(servicoId > 0) {
                "Identificador do serviço inválido."
            }

            val corpo = verificar(
                ApiClient.get(
                    "/api/recebimentos/servico/$servicoId",
                    token()
                )
            )

            val lista = JSONArray(corpo)

            (0 until lista.length()).map { indice ->
                converter(lista.getJSONObject(indice))
            }
        }

    fun cadastrar(
        servicoId: Long,
        valor: Double,
        formaPagamento: String? = null,
        observacoes: String? = null
    ): Result<Recebimento> =
        runCatching {
            require(servicoId > 0) {
                "Identificador do serviço inválido."
            }

            require(valor.isFinite() && valor > 0.0) {
                "Informe um valor positivo e válido."
            }

            val dados = JSONObject().apply {
                put("servicoId", servicoId)
                put("valor", valor)
                put("status", "RECEBIDO")
                put(
                    "dataRecebimento",
                    LocalDate.now().toString()
                )
                put(
                    "formaPagamento",
                    formaPagamento ?: JSONObject.NULL
                )
                put(
                    "observacoes",
                    observacoes ?: JSONObject.NULL
                )
            }

            val corpo = verificar(
                ApiClient.post(
                    "/api/recebimentos",
                    dados,
                    token()
                )
            )

            converter(JSONObject(corpo))
        }

    fun excluir(
        recebimentoId: Long
    ): Result<Unit> =
        runCatching {
            require(recebimentoId > 0) {
                "Identificador do recebimento inválido."
            }

            verificar(
                ApiClient.delete(
                    "/api/recebimentos/$recebimentoId",
                    token()
                )
            )

            Unit
        }

    fun totalRecebido(
        recebimentos: List<Recebimento>
    ): Double =
        recebimentos
            .filter {
                it.status.equals(
                    "RECEBIDO",
                    ignoreCase = true
                )
            }
            .sumOf { it.valor }
}
