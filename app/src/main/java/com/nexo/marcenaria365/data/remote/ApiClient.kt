package com.nexo.marcenaria365.data.remote

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    // Endereco da API local acessivel pelo emulador Android.
    // Na publicacao final, usaremos HTTPS do Render.
    private const val BASE_URL = "http://10.0.2.2:8080"

    data class ApiResponse(
        val status: Int,
        val body: String
    ) {
        val sucesso: Boolean
            get() = status in 200..299
    }

    fun post(
        endpoint: String,
        dados: JSONObject,
        token: String? = null
    ): ApiResponse {
        val conexao = URL("$BASE_URL$endpoint")
            .openConnection() as HttpURLConnection

        try {
            conexao.requestMethod = "POST"
            conexao.connectTimeout = 15000
            conexao.readTimeout = 15000
            conexao.doOutput = true
            conexao.setRequestProperty(
                "Content-Type",
                "application/json; charset=UTF-8"
            )
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

            conexao.outputStream.use { saida ->
                saida.write(
                    dados.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val fluxo = if (conexao.responseCode in 200..299) {
                conexao.inputStream
            } else {
                conexao.errorStream
            }

            val resposta = fluxo?.bufferedReader(
                Charsets.UTF_8
            )?.use { it.readText() } ?: ""

            return ApiResponse(
                status = conexao.responseCode,
                body = resposta
            )
        } finally {
            conexao.disconnect()
        }
    }
}