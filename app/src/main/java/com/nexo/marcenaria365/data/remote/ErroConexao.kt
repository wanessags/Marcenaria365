package com.nexo.marcenaria365.data.remote

import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

internal const val MENSAGEM_SERVIDOR_INDISPONIVEL =
    "O servidor está iniciando ou temporariamente indisponível. Tente novamente em instantes."

/**
 * Converte falhas de rede em mensagens que o usuário compreende.
 * Relatórios técnicos (nome da exceção, HTTP) não são exibidos na interface.
 */
internal fun traduzirErroConexao(erro: IOException, semRede: Boolean): IOException {
    val mensagem = when {
        semRede -> "Sem conexão com a internet. Verifique sua rede."
        erro is SocketTimeoutException ->
            "O servidor demorou para responder. Verifique sua conexão e tente novamente."
        erro is UnknownHostException ->
            "Não foi possível localizar o servidor. Verifique sua rede e tente novamente."
        erro is SSLException ->
            "Não foi possível estabelecer uma conexão segura com o servidor."
        erro is NoRouteToHostException || erro is ConnectException ->
            "Não foi possível conectar ao servidor. Verifique sua internet e tente novamente."
        erro is SocketException ->
            "A conexão com o servidor foi interrompida. Tente novamente."
        else ->
            "A comunicação com o servidor foi interrompida. Tente novamente."
    }
    return IOException(mensagem)
}

/**
 * Mensagem para respostas HTTP sem tratamento específico no repositório.
 * Mantém 5xx como indisponibilidade temporária, sem expor códigos ao usuário.
 */
internal fun mensagemHttpInesperada(status: Int): String {
    return when (status) {
        401 -> "Sua sessão expirou. Faça login novamente."
        403 -> "Você não tem permissão para esta ação."
        404 -> "O item não foi encontrado."
        429 -> "Muitas tentativas. Aguarde um instante e tente novamente."
        in 500..599 -> MENSAGEM_SERVIDOR_INDISPONIVEL
        else -> "Não foi possível concluir a operação. Tente novamente."
    }
}
