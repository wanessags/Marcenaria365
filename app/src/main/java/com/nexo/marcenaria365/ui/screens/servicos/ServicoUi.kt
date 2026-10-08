
package com.nexo.marcenaria365.ui.screens.servicos

data class ServicoUi(
    val id: Long,
    val clienteId: Long,
    val clienteNome: String,
    val titulo: String,
    val descricao: String,
    val entrega: String,
    val valorEstimado: Double,
    val valorRecebido: Double = 0.0,
    val status: String = "Orçamento",
    val fotoUri: String? = null
)

val tiposServico = listOf(
    "Cozinha planejada",
    "Rack para sala",
    "Guarda-roupa",
    "Mesa de jantar",
    "Painel de TV",
    "Armário",
    "Estante",
    "Outros"
)

fun servicosIniciais(): List<ServicoUi> = listOf(
    ServicoUi(
        id = 1L,
        clienteId = 1L,
        clienteNome = "Ana Silva",
        titulo = "Cozinha planejada",
        descricao = "Cozinha planejada em MDF amadeirado, com bancada e armários superiores.",
        entrega = "15/10/2026",
        valorEstimado = 4500.0,
        valorRecebido = 3000.0,
        status = "Em produção"
    ),
    ServicoUi(
        id = 2L,
        clienteId = 2L,
        clienteNome = "Bruno Souza",
        titulo = "Rack para sala",
        descricao = "Rack de madeira para sala, com nichos e espaço para televisão.",
        entrega = "18/10/2026",
        valorEstimado = 1800.0,
        status = "Orçamento"
    ),
    ServicoUi(
        id = 3L,
        clienteId = 3L,
        clienteNome = "Carlos Fernandes",
        titulo = "Guarda-roupa",
        descricao = "Guarda-roupa planejado com portas e divisórias internas.",
        entrega = "22/10/2026",
        valorEstimado = 3800.0,
        status = "Em produção"
    ),
    ServicoUi(
        id = 4L,
        clienteId = 4L,
        clienteNome = "Débora Martins",
        titulo = "Mesa de jantar",
        descricao = "Mesa de jantar em madeira, com acabamento natural.",
        entrega = "25/10/2026",
        valorEstimado = 2200.0,
        valorRecebido = 2200.0,
        status = "Concluído"
    ),
    ServicoUi(
        id = 5L,
        clienteId = 5L,
        clienteNome = "Juliana Almeida",
        titulo = "Painel de TV",
        descricao = "Painel de TV com acabamento amadeirado e iluminação.",
        entrega = "28/10/2026",
        valorEstimado = 1600.0,
        status = "Em produção"
    ),
    ServicoUi(
        id = 6L,
        clienteId = 6L,
        clienteNome = "Ricardo Lima",
        titulo = "Armário planejado",
        descricao = "Armário planejado para área de serviço.",
        entrega = "30/10/2026",
        valorEstimado = 2700.0,
        status = "Em produção"
    ),
    ServicoUi(
        id = 7L,
        clienteId = 1L,
        clienteNome = "Ana Silva",
        titulo = "Estante de madeira",
        descricao = "Estante com prateleiras em MDF.",
        entrega = "05/11/2026",
        valorEstimado = 1200.0,
        status = "Orçamento"
    ),
    ServicoUi(
        id = 8L,
        clienteId = 2L,
        clienteNome = "Armário de banheiro",
        titulo = "Armário de banheiro",
        descricao = "Gabinete suspenso para banheiro.",
        entrega = "08/11/2026",
        valorEstimado = 950.0,
        status = "Concluído"
    )
)

fun moedaServico(valor: Double): String =
    java.text.NumberFormat.getCurrencyInstance(
        java.util.Locale.forLanguageTag("pt-BR")
    ).format(valor)
