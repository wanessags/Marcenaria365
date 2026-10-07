package br.com.nexo.marcenaria365

import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowInsets
import android.widget.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class MainActivity : Activity() {
    private var store = DemoStore()
    private lateinit var page: LinearLayout
    private var screen = "welcome"
    private var selectedJob: String? = null
    private val ink = Color.rgb(43, 34, 28)
    private val brown = Color.rgb(112, 61, 30)
    private val paper = Color.rgb(247, 243, 237)
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = savedInstanceState?.getSerializable("store") as? DemoStore ?: DemoStore()
        screen = savedInstanceState?.getString("screen") ?: "welcome"
        selectedJob = savedInstanceState?.getString("job")
        when (screen) { "home" -> home(); "more" -> more(); "jobs" -> jobs(); "clients" -> clients(); "detail" -> store.jobs.find { it.id == selectedJob }?.let { detail(it) } ?: jobs(); else -> welcome() }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("store", store); outState.putString("screen", screen); outState.putString("job", selectedJob)
        super.onSaveInstanceState(outState)
    }
    private fun base(title: String, destination: String, back: (() -> Unit)? = null) {
        screen = destination
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(paper) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val safe = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(12), dp(20), dp(24)) }
        scroll.addView(page); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)
        if (back != null) button("← Voltar", false, back)
        label("MARCENARIA 365", 14, true)
        label(title, 28, true)
        label("Demonstração • dados fictícios nesta sessão", 13)
        if (destination in listOf("jobs", "clients", "detail")) {
            val nav = LinearLayout(this).apply { setPadding(dp(12), dp(4), dp(12), dp(4)) }
            listOf("Início" to { home() }, "Serviços" to { jobs() }, "Clientes" to { clients() }, "Sair" to { welcome() }).forEach { (name, action) ->
                nav.addView(Button(this).apply { text = name; isAllCaps = false; minHeight = dp(52); setTextColor(brown); setOnClickListener { action() } }, LinearLayout.LayoutParams(0, -2, 1f))
            }
            root.addView(nav)
        }
    }
    private fun label(value: String, size: Int = 16, bold: Boolean = false, target: LinearLayout = page): TextView = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(ink); setPadding(0, dp(8), 0, dp(8))
        if (bold) setTypeface(null, Typeface.BOLD)
        if (bold && android.os.Build.VERSION.SDK_INT >= 28) isAccessibilityHeading = true
        target.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    private fun button(title: String, primary: Boolean = true, action: () -> Unit) {
        page.addView(Button(this).apply {
            text = title; textSize = 16f; isAllCaps = false; minHeight = dp(52)
            setTextColor(if (primary) Color.WHITE else brown)
            backgroundTintList = ColorStateList.valueOf(if (primary) brown else Color.rgb(238, 224, 210))
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
    }
    private fun card(title: String, body: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(14).toFloat(); setStroke(dp(1), Color.rgb(218, 204, 191)) }
        }
        label(title, 18, true, box); label(body, 16, false, box)
        page.addView(box, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
    }
    private fun field(title: String, initial: String = "", numeric: Boolean = false, multiline: Boolean = false): EditText {
        val caption = label(title)
        return EditText(this).apply {
            id = View.generateViewId(); caption.labelFor = id; setText(initial); textSize = 18f; minHeight = dp(52)
            inputType = if (numeric) InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_TEXT or if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setTextColor(ink); setSingleLine(!multiline); page.addView(this, LinearLayout.LayoutParams(-1, -2))
        }
    }
    private fun error(field: EditText, message: String) { field.error = message; field.requestFocus() }
    private fun confirm(title: String, message: String, action: () -> Unit) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setNegativeButton("Voltar", null).setPositiveButton("Confirmar") { _, _ -> action() }.show()
    }
    private fun notice(message: String) { AlertDialog.Builder(this).setTitle("Marcenaria 365").setMessage(message).setPositiveButton("Entendi", null).show() }
    private fun welcome() {
        screen = "welcome"
        ReferenceEntry(this) { home() }.show()
    }
    private fun home() { screen = "home"; HomeScreen(this, { clients() }, { jobs() }, { newJob() }, { welcome() }, { CashScreen(this) { home() }.show() }, { more() }).show() }
    private fun more() { screen = "more"; MoreScreen(this, { home() }, { welcome() }, { PrivacyScreen(this) { more() }.show() }, { ProfileScreen(this) { more() }.show() }, { clients() }, { jobs() }).show() }
    private fun jobs() {
        screen = "jobs"
        ServicesScreen(this, store, { home() }, { clients() }, { newJob() }, { store.jobs.firstOrNull()?.let { detail(it) } }, { more() }).show()
    }
    private fun clients() {
        screen = "clients"
        ClientsScreen(this, store.clients, { home() }, { jobs() }, { clientForm() }, { more() }).show()
    }
    private fun clientForm(client: Client? = null) { NewClientScreen(this, { clients() }) { name, contact -> if (client == null) store.clients.add(Client(name = name, contact = contact)) else { client.name = name; client.contact = contact }; clients() }.show() }
    private fun newJob() {
        NewServiceScreen(this, store, { jobs() }) { job -> detail(job) }.show()
        return
        /*
        if (store.clients.isEmpty()) { clientForm(); return }
        base("Novo serviço", "form", { jobs() })
        val caption = label("Cliente *")
        val client = Spinner(this).apply { id = View.generateViewId(); minimumHeight = dp(52); adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, store.clients.map { it.name }); page.addView(this) }
        caption.labelFor = client.id
        val description = field("Descrição do serviço *", multiline = true)
        val measures = field("Medidas (opcional)")
        val deadline = field("Prazo (opcional, dd/mm/aaaa)")
        button("Salvar serviço") {
            if (description.text.isBlank()) { error(description, "Descreva o serviço."); return@button }
            if (description.text.length > 2000 || measures.text.length > 1000) { notice("Use até 2.000 caracteres na descrição e 1.000 nas medidas."); return@button }
            val parsed = if (deadline.text.isBlank()) null else try { LocalDate.parse(deadline.text.toString(), dateFormat) } catch (_: Exception) { error(deadline, "Informe uma data válida, como 02/10/2026."); return@button }
            val job = Job(clientId = store.clients[client.selectedItemPosition].id, description = description.text.toString().trim(), measures = measures.text.toString(), deadline = parsed)
            store.jobs.add(job); detail(job)
        }
        */
    }
    private fun detail(job: Job) {
        selectedJob = job.id
        ServiceDetailScreen(this, job, store.clients.first { it.id == job.clientId }.name, { jobs() }, { payments(job) }).show()
        return
        /*
        base(job.description, "detail", { jobs() })
        label(store.clients.first { it.id == job.clientId }.name, 18)
        label("${job.progress.label} • Prazo: ${job.deadline?.format(dateFormat) ?: "Não definido"}")
        if (job.measures.isNotBlank()) label("Medidas: ${job.measures}")
        card("Resumo financeiro", "Valor acordado: ${job.approved?.let { money(it.total) } ?: "Não definido"}\nRecebido: ${money(job.received)}\nSaldo: ${job.balance?.let { money(it) } ?: "Não definido"}\n${job.financialLabel}")
        button("Orçamentos") { budgets(job) }
        button("Recebimentos", false) { payments(job) }
        if (job.progress != Progress.DONE) button(if (job.progress == Progress.PLANNED) "Iniciar execução" else "Concluir serviço", false) {
            if (job.approved == null) notice("Aprove um orçamento antes de iniciar a execução.")
            else confirm("Atualizar andamento", "A conclusão do serviço não altera o saldo ou os pagamentos.") { job.advance(); detail(job) }
        }
        label("Histórico", 20, true); job.history.reversed().forEach { label(it, 15) }*/
    }
    private fun budgets(job: Job) {
        base("Orçamentos", "form", { detail(job) })
        button("+ Nova versão do orçamento") { budgetForm(job) }
        if (job.budgets.isEmpty()) label("Ainda não há orçamento para este serviço.")
        job.budgets.reversed().forEach { budget ->
            card("Versão ${budget.version} • ${budget.decision.label}", budget.items.joinToString("\n") { "${it.description}: ${it.quantity.toPlainString()} × ${money(it.unitCents)} = ${money(it.subtotal)}" } + "\nTotal: ${money(budget.total)}\n${budget.conditions}")
            if (budget == job.budgets.last() && budget.decision == Decision.NEGOTIATING) {
                button("Registrar aprovação da versão ${budget.version}") {
                    confirm("Registrar aprovação", "Confirme que o cliente aprovou ${money(budget.total)} e as condições fora do aplicativo. Uma aprovação anterior será substituída; seu histórico será preservado.") { job.approve(budget); budgets(job) }
                }
                button("Registrar recusa", false) { confirm("Recusar orçamento", "Registrar que o cliente recusou esta proposta?") { budget.decision = Decision.REJECTED; job.history.add("Orçamento v${budget.version} recusado"); budgets(job) } }
            }
        }
    }
    private fun budgetForm(job: Job, items: MutableList<BudgetItem> = mutableListOf(), savedConditions: String = "") {
        BudgetScreen(this, job, { budgets(job) }, { budgets(job) }).show()
        return
        /*
        base("Novo orçamento", "form", { budgets(job) })
        label("Cada salvamento cria uma versão, preservando as anteriores.")
        items.forEach { label("${it.description} • ${money(it.subtotal)}") }
        val description = field("Descrição do item")
        val quantity = field("Quantidade (ex.: 1 ou 1,5)", "1", true)
        val value = field("Valor unitário em R$ (ex.: 150,00)", numeric = true)
        val conditions = field("Condições de pagamento *", savedConditions, multiline = true)
        button("Adicionar item", false) {
            if (description.text.isBlank()) { error(description, "Descreva o material, a mão de obra ou o equipamento."); return@button }
            val q = quantity.text.toString().replace(',', '.').toBigDecimalOrNull()
            if (q == null || q <= BigDecimal.ZERO || q > BigDecimal("1000000") || q.scale() > 3) { error(quantity, "Use uma quantidade positiva com até três casas decimais, no máximo 1.000.000."); return@button }
            val cents = parseMoney(value.text.toString())
            if (cents == null) { error(value, "Informe reais sem pontos, como 1500,00."); return@button }
            items.add(BudgetItem(description.text.toString(), q, cents)); budgetForm(job, items, conditions.text.toString())
        }
        button("Salvar nova versão") {
            if (items.isEmpty()) { error(description, "Adicione ao menos um item antes de salvar."); return@button }
            if (conditions.text.isBlank()) { error(conditions, "Informe as condições de pagamento."); return@button }
            job.budgets.add(Budget(job.budgets.size + 1, items.toList(), conditions.text.toString()))
            job.history.add("Orçamento v${job.budgets.size} criado"); budgets(job)
        }
        */
    }
    private fun payments(job: Job) {
        base("Recebimentos", "form", { detail(job) })
        card(job.financialLabel, "Recebido: ${money(job.received)}\nSaldo: ${job.balance?.let { money(it) } ?: "Não definido"}")
        if (job.approved != null) button("+ Registrar pagamento") { paymentForm(job) }
        else label("Aprove um orçamento antes de registrar pagamentos.")
        job.payments.reversed().forEach { payment ->
            card("${money(payment.cents)} • ${payment.date.format(dateFormat)}", if (payment.cancelled) "Cancelado • ${payment.reason}" else "Recebimento registrado manualmente")
            if (!payment.cancelled) button("Cancelar lançamento de ${money(payment.cents)}", false) {
                val reason = EditText(this).apply { hint = "Motivo do cancelamento"; contentDescription = "Motivo do cancelamento"; minHeight = dp(52) }
                val dialog = AlertDialog.Builder(this).setTitle("Cancelar lançamento?").setMessage("${money(payment.cents)} em ${payment.date.format(dateFormat)}. O saldo será recalculado; o registro será preservado.").setView(reason).setNegativeButton("Voltar", null).setPositiveButton("Confirmar", null).create()
                dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (reason.text.isBlank()) error(reason, "Informe o motivo.")
                    else { payment.cancelled = true; payment.reason = reason.text.toString(); job.history.add("Pagamento cancelado • ${money(payment.cents)} • ${payment.reason}"); dialog.dismiss(); payments(job) }
                } }; dialog.show()
            }
        }
    }
    private fun paymentForm(job: Job) {
        base("Registrar pagamento", "form", { payments(job) })
        label(job.description, 20, true)
        val date = field("Data do recebimento * (dd/mm/aaaa)", LocalDate.now().format(dateFormat))
        val value = field("Valor recebido em R$ *", numeric = true)
        button("Revisar pagamento") {
            val parsed = try { LocalDate.parse(date.text.toString(), dateFormat) } catch (_: Exception) { error(date, "Informe uma data válida."); return@button }
            val cents = parseMoney(value.text.toString())
            if (cents == null || cents <= 0) { error(value, "Informe um valor maior que zero, como 600,00."); return@button }
            val extra = if (cents > (job.balance ?: 0)) "\nAtenção: o valor excede o saldo atual." else ""
            confirm("Confirmar pagamento", "${job.description}\n${parsed.format(dateFormat)} • ${money(cents)}$extra") { job.receive(parsed, cents); payments(job) }
        }
    }
    @Deprecated("Compatibilidade com navegação clássica")
    override fun onBackPressed() { when (screen) { "welcome" -> super.onBackPressed(); "home" -> welcome(); else -> home() } }
}













