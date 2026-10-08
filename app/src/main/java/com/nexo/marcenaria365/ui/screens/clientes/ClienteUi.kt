package com.nexo.marcenaria365.ui.screens.clientes

data class ClienteUi(
    val id: Long,
    val nome: String,
    val telefone: String,
    val email: String = "",
    val ativo: Boolean = true
)

fun clientesIniciais(): List<ClienteUi> = listOf(
    ClienteUi(1L, "Ana Silva", "(11) 91234-5678"),
    ClienteUi(2L, "Bruno Souza", "(11) 98765-4321"),
    ClienteUi(3L, "Carlos Fernandes", "(11) 99876-5432"),
    ClienteUi(4L, "Débora Martins", "(11) 97654-3210"),
    ClienteUi(5L, "Juliana Almeida", "(11) 96543-2109"),
    ClienteUi(6L, "Ricardo Lima", "(11) 95432-1098")
)
