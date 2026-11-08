package com.nexo.marcenaria365.data.remote

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

internal fun traduzirErroConexao(erro: IOException, semRede: Boolean): IOException {
    val mensagem = when {
        semRede -> "Sem conexão com a internet. Verifique sua rede."
        erro is SocketTimeoutException -> "A conexão demorou mais que o esperado. Tente novamente."
        erro is UnknownHostException -> "Não foi possível localizar o servidor. Verifique sua rede e tente novamente."
        erro is ConnectException -> "Não foi possível conectar ao servidor. Tente novamente em instantes."
        erro is SSLException -> "Não foi possível estabelecer uma conexão segura com o servidor."
        else -> "A comunicação com o servidor foi interrompida. Tente novamente."
    }
    return IOException(mensagem)
}
