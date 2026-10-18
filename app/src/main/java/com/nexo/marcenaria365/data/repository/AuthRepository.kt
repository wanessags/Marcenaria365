package com.nexo.marcenaria365.data.repository

import com.nexo.marcenaria365.data.remote.ApiClient
import org.json.JSONObject
import java.io.IOException

object AuthRepository {

    @Volatile
    var sessaoAtual: Sessao? = null
        private set

    fun encerrarSessao() {
        sessaoAtual = null
    }

    data class Usuario(
        val id: Long,
        val nome: String,
        val email: String
    )

    data class Sessao(
        val token: String,
        val usuario: Usuario
    )

    fun cadastrar(
        nome: String,
        email: String,
        senha: String
    ): Result<Usuario> {
        return try {
            val dados = JSONObject().apply {
                put("nome", nome.trim())
                put("email", email.trim())
                put("senha", senha)
            }

            val resposta = ApiClient.post(
                endpoint = "/api/auth/cadastro",
                dados = dados
            )

            if (!resposta.sucesso) {
                throw IOException(
                    mensagemErro(resposta.status)
                )
            }

            val json = JSONObject(resposta.body)

            Result.success(
                Usuario(
                    id = json.getLong("id"),
                    nome = json.getString("nome"),
                    email = json.getString("email")
                )
            )
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    fun login(
        email: String,
        senha: String
    ): Result<Sessao> {
        return try {
            val dados = JSONObject().apply {
                put("email", email.trim())
                put("senha", senha)
            }

            val resposta = ApiClient.post(
                endpoint = "/api/auth/login",
                dados = dados
            )

            if (!resposta.sucesso) {
                throw IOException(
                    mensagemErro(resposta.status)
                )
            }

            val json = JSONObject(resposta.body)
            val usuarioJson = json.getJSONObject("usuario")

            val sessao = Sessao(
                token = json.getString("token"),
                usuario = Usuario(
                    id = usuarioJson.getLong("id"),
                    nome = usuarioJson.getString("nome"),
                    email = usuarioJson.getString("email")
                )
            )

            sessaoAtual = sessao
            Result.success(sessao)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    private fun mensagemErro(status: Int): String {
        return when (status) {
            400 -> "Confira os dados informados."
            401 -> "E-mail ou senha incorretos."
            409 -> "Este e-mail já está cadastrado."
            422 -> "Não foi possível validar os dados."
            in 500..599 -> "O servidor está indisponível."
            else -> "Erro na comunicação com a API (HTTP $status)."
        }
    }
}