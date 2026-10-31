package com.nexo.marcenaria365.data.mapper

import com.nexo.marcenaria365.data.repository.ClienteRepository
import com.nexo.marcenaria365.ui.screens.clientes.ClienteUi

fun ClienteRepository.Cliente.paraUi(): ClienteUi {
    return ClienteUi(
        id = id,
        nome = nome,
        telefone = telefone,
        email = email,
        ativo = true
    )
}

fun ClienteUi.paraRequest(
    endereco: String = "",
    observacoes: String = ""
): ClienteRepository.ClienteRequest {
    return ClienteRepository.ClienteRequest(
        nome = nome,
        telefone = telefone,
        email = email,
        endereco = endereco,
        observacoes = observacoes
    )
}