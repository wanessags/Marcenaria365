package br.com.nexo.marcenaria365

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class DomainTest {
    @Test fun exampleAndCancellationRecomputeBalance() {
        val job = DemoStore().jobs.first()
        assertEquals(140000L, job.balance)
        job.receive(LocalDate.now(), 140000)
        assertEquals("Quitado", job.financialLabel)
        job.payments.last().cancelled = true
        assertEquals(140000L, job.balance)
    }
    @Test fun unapprovedServiceIsNotPaid() {
        val job = Job(clientId = "client", description = "Mesa", measures = "", deadline = null)
        assertNull(job.balance)
        assertEquals("Valor acordado ainda não definido", job.financialLabel)
        try { job.receive(LocalDate.now(), 100); fail("Pagamento não autorizado") } catch (_: IllegalArgumentException) { }
    }
    @Test fun completingDoesNotEraseDebt() {
        val job = DemoStore().jobs.first(); job.advance(); job.advance()
        assertEquals(Progress.DONE, job.progress); assertEquals(140000L, job.balance)
    }
    @Test fun decimalMoneyIsExact() {
        assertEquals(123456L, parseMoney("1234,56")); assertNull(parseMoney("1.234,56"))
        assertNull(parseMoney("-10")); assertNull(parseMoney("10,001"))
        assertEquals(152L, BudgetItem("Teste", BigDecimal("1.5"), 101).subtotal)
    }
    @Test fun excessIsNotPaidStatus() {
        val job = DemoStore().jobs.first(); job.receive(LocalDate.now(), 150000)
        assertEquals(-10000L, job.balance); assertEquals("Pagamento excedente", job.financialLabel)
    }
}
