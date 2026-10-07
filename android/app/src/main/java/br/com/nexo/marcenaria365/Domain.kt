package br.com.nexo.marcenaria365

import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

fun id(): String = UUID.randomUUID().toString()
fun money(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(BigDecimal.valueOf(cents, 2))
fun parseMoney(input: String): Long? = try {
    val value = input.trim()
    if (!Regex("[0-9]{1,9}(,[0-9]{1,2})?").matches(value)) null
    else BigDecimal(value.replace(',', '.')).movePointRight(2).longValueExact()
} catch (_: ArithmeticException) { null }

data class Client(val id: String = id(), var name: String, var contact: String) : Serializable
data class BudgetItem(val description: String, val quantity: BigDecimal, val unitCents: Long) : Serializable {
    val subtotal: Long get() = quantity.multiply(BigDecimal.valueOf(unitCents)).setScale(0, RoundingMode.HALF_UP).longValueExact()
}
enum class Decision(val label: String) { NEGOTIATING("Em negociação"), APPROVED("Aprovado"), REJECTED("Recusado"), REPLACED("Substituído") }
enum class Progress(val label: String) { PLANNED("Planejado"), STARTED("Em execução"), DONE("Concluído") }
data class Budget(val version: Int, val items: List<BudgetItem>, val conditions: String, var decision: Decision = Decision.NEGOTIATING) : Serializable {
    val total: Long get() = items.fold(0L) { sum, item -> Math.addExact(sum, item.subtotal) }
}
data class Payment(val id: String = id(), val date: LocalDate, val cents: Long, var cancelled: Boolean = false, var reason: String? = null) : Serializable
data class Job(
    val id: String = id(), val clientId: String, val description: String, val measures: String, val deadline: LocalDate?,
    var progress: Progress = Progress.PLANNED,
    val budgets: MutableList<Budget> = mutableListOf(), val payments: MutableList<Payment> = mutableListOf(),
    val history: MutableList<String> = mutableListOf("Serviço cadastrado na demonstração")
) : Serializable {
    val approved: Budget? get() = budgets.lastOrNull { it.decision == Decision.APPROVED }
    val received: Long get() = payments.filterNot { it.cancelled }.sumOf { it.cents }
    val balance: Long? get() = approved?.let { it.total - received }
    val financialLabel: String get() = when { balance == null -> "Valor acordado ainda não definido"; balance == 0L -> "Quitado"; balance!! < 0 -> "Pagamento excedente"; else -> "Pagamento pendente" }
    fun approve(budget: Budget) {
        require(budget in budgets && budget.items.isNotEmpty())
        budgets.filter { it != budget && it.decision == Decision.APPROVED }.forEach { it.decision = Decision.REPLACED }
        budget.decision = Decision.APPROVED
        history.add("Orçamento v${budget.version} aprovado • ${money(budget.total)}")
    }
    fun receive(date: LocalDate, cents: Long) {
        require(approved != null) { "Aprove um orçamento antes de registrar recebimentos." }
        require(cents > 0) { "Informe um valor maior que zero." }
        payments.add(Payment(date = date, cents = cents)); history.add("Recebimento registrado • ${money(cents)}")
    }
    fun advance() {
        require(approved != null) { "Aprove um orçamento antes de iniciar a execução." }
        progress = if (progress == Progress.PLANNED) Progress.STARTED else Progress.DONE
        history.add("Serviço ${progress.label.lowercase()}")
    }
}
class DemoStore : Serializable {
    val clients = mutableListOf(Client(name = "Ana Costa (exemplo)", contact = "ana@example.com"))
    val jobs = mutableListOf(Job(clientId = clients.first().id, description = "Armário de cozinha", measures = "3,20 m × 2,60 m", deadline = null).apply {
        budgets.add(Budget(1, listOf(BudgetItem("Materiais", BigDecimal.ONE, 150000), BudgetItem("Mão de obra", BigDecimal.ONE, 50000)), "Entrada e saldo na entrega", Decision.APPROVED))
        receive(LocalDate.now(), 60000)
    })
}
