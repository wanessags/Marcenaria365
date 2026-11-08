package com.nexo.marcenaria365

import com.nexo.marcenaria365.data.remote.traduzirErroConexao
import java.io.IOException
import java.net.*
import javax.net.ssl.SSLException
import org.junit.Assert.*
import org.junit.Test

class ErroConexaoTest {
    @Test fun faltaDeRedeConfirmadaTemMensagemEspecifica() {
        assertEquals("Sem conexão com a internet. Verifique sua rede.",
            traduzirErroConexao(UnknownHostException(), true).message)
    }

    @Test fun dnsNaoAfirmaAusenciaDeInternet() {
        val mensagem = traduzirErroConexao(UnknownHostException(), false).message!!
        assertTrue(mensagem.contains("localizar o servidor"))
        assertFalse(mensagem.contains("Sem conexão"))
    }

    @Test fun diferenciaTimeoutServidorETlsSemExporDetalhesInternos() {
        assertTrue(traduzirErroConexao(SocketTimeoutException(), false).message!!.contains("demorou"))
        assertTrue(traduzirErroConexao(ConnectException(), false).message!!.contains("conectar ao servidor"))
        assertTrue(traduzirErroConexao(SSLException("detalhe sensível"), false).message!!.contains("conexão segura"))
        assertFalse(traduzirErroConexao(IOException("detalhe sensível"), false).message!!.contains("sensível"))
    }
}
