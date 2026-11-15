package com.nexo.marcenaria365

import com.nexo.marcenaria365.data.mapper.paraRequest
import com.nexo.marcenaria365.data.mapper.paraUi
import com.nexo.marcenaria365.data.repository.ClienteRepository
import org.junit.Assert.*
import org.junit.Test

class ClienteMapperTest {
    @Test fun preservaInativoNaInterfaceENaRequisicao() {
        val cliente = ClienteRepository.Cliente(1, "Cliente", "", "", "Endereço", "Histórico", false)
        val ui = cliente.paraUi()
        assertFalse(ui.ativo)
        val request = ui.paraRequest(cliente.endereco, cliente.observacoes)
        assertFalse(request.ativo)
        assertEquals("Endereço", request.endereco)
        assertEquals("Histórico", request.observacoes)
    }

    @Test fun clienteSemStatusContinuaAtivoPorPadrao() {
        assertTrue(ClienteRepository.Cliente(1, "Cliente", "", "", "", "").paraUi().ativo)
    }
}
