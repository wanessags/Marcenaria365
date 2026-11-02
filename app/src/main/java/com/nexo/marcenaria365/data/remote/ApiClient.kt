
package com.nexo.marcenaria365.data.remote

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    // API hospedada no Render e conectada ao Supabase.
    private const val BASE_URL = "https://marcenaria365-api.onrender.com"

    data class ApiResponse(
        val status: Int,
        val body: String
    ) {
        val sucesso: Boolean
            get() = status in 200..299
    }

    private fun requisicao(
        metodo: String,
        endpoint: String,
        dados: JSONObject? = null,
        token: String? = null
    ): ApiResponse {

        require(endpoint.startsWith("/") && !endpoint.startsWith("//")) {
            "Endpoint inválido."
        }

        val conexao = URL("$BASE_URL$endpoint")
            .openConnection() as HttpURLConnection

        try {
            conexao.requestMethod = metodo
            conexao.connectTimeout = 30000
            conexao.readTimeout = 30000

            conexao.setRequestProperty(
                "Accept",
                "application/json"
            )

            if (!token.isNullOrBlank()) {
                conexao.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )
            }

            if (dados != null) {
                conexao.doOutput = true

                conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                )

                conexao.outputStream.use { saida ->
                    saida.write(
                        dados.toString().toByteArray(Charsets.UTF_8)
                    )
                }
            }

            val status = conexao.responseCode

            val fluxo = if (status in 200..299) {
                conexao.inputStream
            } else {
                conexao.errorStream
            }

            val resposta = fluxo?.bufferedReader(
                Charsets.UTF_8
            )?.use { it.readText() } ?: ""

            return ApiResponse(
                status = status,
                body = resposta
            )

        } finally {
            conexao.disconnect()
        }
    }

    fun get(
        endpoint: String,
        token: String? = null
    ): ApiResponse {
        return requisicao("GET", endpoint, token = token)
    }

    fun post(
        endpoint: String,
        dados: JSONObject,
        token: String? = null
    ): ApiResponse {
        return requisicao("POST", endpoint, dados, token)
    }

    fun put(
        endpoint: String,
        dados: JSONObject,
        token: String? = null
    ): ApiResponse {
        return requisicao("PUT", endpoint, dados, token)
    }

    fun delete(
        endpoint: String,
        token: String? = null
    ): ApiResponse {
        return requisicao("DELETE", endpoint, token = token)
    }
}
