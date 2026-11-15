package com.nexo.marcenaria365.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class ErroConexaoTest {

    private fun mensagem(erro: IOException, semRede: Boolean = false): String =
        traduzirErroConexao(erro, semRede).message.orEmpty()

    @Test
    fun ausenciaDeRedeTemMensagemPropria() {
        assertTrue(mensagem(IOException("falha"), semRede = true).contains("Sem conexão com a internet"))
    }

    @Test
    fun timeoutExplicaQueOServidorDemorou() {
        assertTrue(mensagem(SocketTimeoutException()).contains("demorou para responder"))
    }

    @Test
    fun hostDesconhecidoOrientaVerificarARede() {
        assertTrue(mensagem(UnknownHostException("host")).contains("localizar o servidor"))
    }

    @Test
    fun conexaoRecusadaTemMensagemClara() {
        assertTrue(mensagem(ConnectException()).contains("Não foi possível conectar"))
        assertTrue(mensagem(NoRouteToHostException()).contains("Não foi possível conectar"))
    }

    @Test
    fun tlsInvalidoNaoExpoeDetalhesDoHandshake() {
        val texto = mensagem(SSLHandshakeException("certificate failed"))
        assertTrue(texto.contains("conexão segura"))
        assertFalse(texto.contains("certificate", ignoreCase = true))
    }

    @Test
    fun conexaoInterrompidaTemMensagemClara() {
        assertTrue(mensagem(SocketException("reset")).contains("interrompida"))
        assertTrue(mensagem(IOException("inesperado")).contains("interrompida"))
    }

    @Test
    fun indisponibilidadeCobre502a504() {
        listOf(500, 502, 503, 504).forEach { status ->
            assertTrue(
                "HTTP $status deve indicar indisponibilidade",
                mensagemHttpInesperada(status) == MENSAGEM_SERVIDOR_INDISPONIVEL
            )
        }
    }

    @Test
    fun mensagensNaoExpoemCodigosOuNomesTecnicos() {
        val mensagens = listOf(
            mensagem(SocketTimeoutException()),
            mensagem(ConnectException()),
            mensagem(UnknownHostException("host")),
            mensagem(SSLHandshakeException("falha")),
            mensagem(SocketException("reset")),
            mensagem(IOException("falha")),
            mensagemHttpInesperada(418),
            mensagemHttpInesperada(503)
        )

        mensagens.forEach { texto ->
            assertTrue(texto.isNotBlank())
            assertFalse("Mensagem técnica: $texto", texto.contains("HTTP", ignoreCase = true))
            assertFalse("Mensagem técnica: $texto", texto.contains("Exception", ignoreCase = true))
            assertFalse("Mensagem técnica: $texto", texto.contains("Socket", ignoreCase = true))
            assertFalse("Mensagem técnica: $texto", texto.contains("certificate", ignoreCase = true))
        }
    }

    @Test
    fun tempoDeLeituraPadraoCobreInicializacaoLentaDoRender() {
        assertTrue(ApiClient.TEMPO_LEITURA_PADRAO_MS >= 120_000)
        assertTrue(ApiClient.TEMPO_CONEXAO_PADRAO_MS in 1..20_000)
    }
}
