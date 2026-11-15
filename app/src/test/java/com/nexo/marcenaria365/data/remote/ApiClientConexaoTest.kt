package com.nexo.marcenaria365.data.remote

import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ApiClientConexaoTest {

    private lateinit var servidor: HttpServer
    private val executor = Executors.newCachedThreadPool()
    private val tentativasLentas = AtomicInteger()

    @Before
    fun iniciar() {
        servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidor.executor = executor

        servidor.createContext("/ok") { troca ->
            val corpo = "[]".toByteArray()
            troca.sendResponseHeaders(200, corpo.size.toLong())
            troca.responseBody.use { it.write(corpo) }
        }

        servidor.createContext("/indisponivel") { troca ->
            val corpo = """{"erro":"iniciando"}""".toByteArray()
            troca.sendResponseHeaders(503, corpo.size.toLong())
            troca.responseBody.use { it.write(corpo) }
        }

        servidor.createContext("/lento") { troca ->
            tentativasLentas.incrementAndGet()
            Thread.sleep(2_000)
            val corpo = "[]".toByteArray()
            troca.sendResponseHeaders(200, corpo.size.toLong())
            troca.responseBody.use { it.write(corpo) }
        }

        servidor.start()
        ApiClient.configurarParaTeste(
            url = "http://127.0.0.1:${servidor.address.port}",
            tempoConexao = 2_000,
            tempoLeitura = 5_000
        )
    }

    @After
    fun encerrar() {
        ApiClient.restaurarConfiguracaoPadrao()
        servidor.stop(0)
        executor.shutdownNow()
    }

    @Test
    fun respostaNormalContinuaFuncionando() {
        val resposta = ApiClient.get("/ok")

        assertEquals(200, resposta.status)
        assertTrue(resposta.sucesso)
        assertEquals("[]", resposta.body)
    }

    @Test
    fun servidorIndisponivelRetorna503ComMensagemCompreensivel() {
        val resposta = ApiClient.get("/indisponivel")

        assertEquals(503, resposta.status)
        assertFalse(resposta.sucesso)
        assertEquals(
            MENSAGEM_SERVIDOR_INDISPONIVEL,
            mensagemHttpInesperada(resposta.status)
        )
    }

    @Test
    fun leituraLentaFalhaEmTempoLimitadoSemRepetirARequisicao() {
        // Aquece o servidor local antes de medir o timeout da leitura.
        assertEquals(200, ApiClient.get("/ok").status)
        ApiClient.configurarParaTeste(
            url = "http://127.0.0.1:${servidor.address.port}",
            tempoConexao = 2_000,
            tempoLeitura = 400
        )

        val inicio = System.nanoTime()

        val erro = assertThrows(IOException::class.java) {
            ApiClient.get("/lento")
        }

        val duracaoMs = (System.nanoTime() - inicio) / 1_000_000
        assertTrue("Espera excessiva: ${duracaoMs}ms", duracaoMs < 2_000)
        assertTrue(erro.message.orEmpty().contains("demorou para responder"))

        Thread.sleep(300)
        assertEquals(
            "A requisição não deve ser repetida automaticamente",
            1,
            tentativasLentas.get()
        )
    }

    @Test
    fun servidorForaDoArTemMensagemClara() {
        val portaFechada = ServerSocket(0).use { it.localPort }
        ApiClient.configurarParaTeste(
            url = "http://127.0.0.1:$portaFechada",
            tempoConexao = 500,
            tempoLeitura = 500
        )

        val erro = assertThrows(IOException::class.java) {
            ApiClient.get("/api/clientes")
        }

        assertTrue(erro.message.orEmpty().contains("Não foi possível conectar"))
    }
}
