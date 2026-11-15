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
            429 -> "Muitas tentativas. Aguarde um pouco e tente novamente."
            504 -> "A conexão demorou mais que o esperado. Tente novamente."
            in 500..599 -> "O servidor está temporariamente indisponível. Tente novamente em instantes."
            else -> "Erro na comunicação com a API (HTTP $status)."
        }
    }

    fun solicitarRecuperacao(email: String): Result<String> = recuperacao(
        "/api/auth/recuperacao", JSONObject().put("email", email.trim())
    )

    fun redefinirSenha(codigo: String, senha: String): Result<String> = recuperacao(
        "/api/auth/redefinicao", JSONObject().put("codigo", codigo.trim()).put("senha", senha)
    )

    private fun recuperacao(endpoint: String, dados: JSONObject): Result<String> = runCatching {
        val resposta = ApiClient.post(endpoint, dados)
        if (!resposta.sucesso) {
            throw IOException(when (resposta.status) {
                400, 422 -> if (endpoint.endsWith("redefinicao")) {
                    "Confira os requisitos da senha e o código. Ele pode estar inválido ou expirado."
                } else "Informe um e-mail válido."
                429 -> "Muitas tentativas. Aguarde um minuto e tente novamente."
                in 500..599 -> "Recuperação temporariamente indisponível. Tente novamente mais tarde."
                else -> "Não foi possível concluir a recuperação. Tente novamente."
            })
        }
        JSONObject(resposta.body).getString("mensagem")
    }
}
