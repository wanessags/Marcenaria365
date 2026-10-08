
package com.nexo.marcenaria365.ui.screens.clientes

data class ClienteUi(
    val id: Long,
    val nome: String,
    val telefone: String,
    val email: String = "",
    val ativo: Boolean = true
)

fun clientesIniciais(): List<ClienteUi> = listOf(
    ClienteUi(
        id = 1L,
        nome = "Ana Silva",
        telefone = "(11) 91234-5678"
    ),
    ClienteUi(
        id = 2L,
        nome = "Bruno Souza",
        telefone = "(11) 98765-4321"
    ),
    ClienteUi(
        id = 3L,
        nome = "Carlos Fernandes",
        telefone = "(11) 99876-5432"
    ),
    ClienteUi(
        id = 4L,
        nome = "Débora Martins",
        telefone = "(11) 97654-3210"
    ),
    ClienteUi(
        id = 5L,
        nome = "Juliana Almeida",
        telefone = "(11) 96543-2109"
    ),
    ClienteUi(
        id = 6L,
        nome = "Ricardo Lima",
        telefone = "(11) 95432-1098"
    )
)
