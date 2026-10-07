package br.com.nexo.marcenaria365

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*

/** Visual preview: the figures below intentionally reproduce the supplied mockup. */
class HomeScreen(
    private val activity: Activity,
    private val onClients: () -> Unit,
    private val onJobs: () -> Unit,
    private val onNewJob: () -> Unit,
    private val onLogout: () -> Unit,
    private val onCash: () -> Unit,
    private val onMore: () -> Unit
) {
    private val ink = Color.rgb(42, 57, 66)
    private val cream = Color.rgb(250, 245, 239)
    private val white = Color.rgb(255, 253, 249)
    private val muted = Color.rgb(144, 144, 138)
    private val wood = Color.rgb(184, 142, 105)
    private val green = Color.rgb(37, 156, 126)
    private val yellow = Color.rgb(244, 176, 48)
    private val navy = Color.rgb(26, 52, 67)
    private fun dp(n: Int) = (n * activity.resources.displayMetrics.density + .5f).toInt()
    private fun box(color: Int, radius: Int = 13) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun text(value: String, size: Float, color: Int = ink, bold: Boolean = false) = TextView(activity).apply {
        text = value; textSize = size; setTextColor(color); includeFontPadding = false
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun column() = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    private fun icon(kind: String, color: Int, size: Int = 26) = HomeIcon(activity, kind, color).apply {
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private fun clickable(view: View, action: () -> Unit) {
        val background = view.background ?: box(Color.TRANSPARENT)
        view.background = RippleDrawable(ColorStateList.valueOf(0x18304C5D), background, null)
        view.isFocusable = true
        view.setOnClickListener { action() }
    }
    private fun info(title: String, message: String) {
        AlertDialog.Builder(activity).setTitle(title).setMessage(message).setPositiveButton("Entendi", null).show()
    }
    private fun card(parent: LinearLayout, padding: Int = 16): LinearLayout {
        return column().apply {
            background = box(white); setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        }
    }
    fun show() {
        val root = column().apply { setBackgroundColor(cream) }
        root.setOnApplyWindowInsetsListener { v, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val safe = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        val scroll = ScrollView(activity).apply { isFillViewport = true; isVerticalScrollBarEnabled = false }
        val content = column().apply { setPadding(dp(16), dp(18), dp(16), dp(24)) }
        content.addView(text("Olá, Mariana!", 26f, bold = true).apply {
            if (android.os.Build.VERSION.SDK_INT >= 28) isAccessibilityHeading = true
        })
        content.addView(text("Confira o resumo da sua marcenaria hoje.", 13f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7); bottomMargin = dp(2) })

        val balance = card(content)
        balance.orientation = LinearLayout.HORIZONTAL; balance.gravity = Gravity.CENTER_VERTICAL
        balance.addView(FrameLayout(activity).apply {
            background = box(wood)
            addView(HomeIcon(activity, "wallet", Color.WHITE), FrameLayout.LayoutParams(dp(38), dp(38), Gravity.CENTER))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }, LinearLayout.LayoutParams(dp(78), dp(82)))
        balance.addView(column().apply {
            addView(text("SALDO EM CAIXA", 12f, muted, true))
            addView(text("R$ 4.850,00", 29f, bold = true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
            addView(text("Ver detalhes  ›", 12f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(18) })
        clickable(balance) { onCash() }
        balance.contentDescription = "Saldo em caixa, 4.850 reais, valor de demonstração. Ver detalhes"

        val stats = LinearLayout(activity)
        val items = listOf(Triple("person", "6", "Clientes\ncadastrados"), Triple("clipboard", "8", "Serviços em\nandamento"), Triple("shield", "4", "Serviços\nconcluídos"))
        items.forEachIndexed { index, (symbol, number, caption) ->
            val tile = column().apply {
                background = box(white); setPadding(dp(12), dp(14), dp(8), dp(14))
                addView(icon(symbol, if (index == 2) green else wood, 24))
                addView(text(number, 22f, bold = true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
                addView(text(caption, 11f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
            }
            clickable(tile) { if (index == 0) onClients() else onJobs() }
            stats.addView(tile, LinearLayout.LayoutParams(0, -2, 1f).apply { if (index > 0) leftMargin = dp(9) })
        }
        content.addView(stats, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val status = card(content)
        status.addView(text("Serviços por status", 18f, bold = true))
        val chartRow = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL }
        val donut = FrameLayout(activity)
        donut.addView(StatusDonut(activity), FrameLayout.LayoutParams(-1, -1))
        donut.addView(column().apply {
            gravity = Gravity.CENTER
            addView(text("16", 28f, bold = true).apply { gravity = Gravity.CENTER })
            addView(text("Total", 10f, muted).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(3) })
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }, FrameLayout.LayoutParams(-1, -1))
        chartRow.addView(donut, LinearLayout.LayoutParams(0, dp(150), 1f))
        val legend = column()
        val labels = listOf(Triple("Orçamentos", 4, yellow), Triple("Produção", 4, 0xFFC9CAC5.toInt()), Triple("Concluídos", 7, green), Triple("Pendentes", 1, 0xFFDA7378.toInt()))
        labels.forEach { (name, count, color) ->
            val row = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(9), 0, dp(9)) }
            row.addView(View(activity).apply { background = box(color, 10); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }, LinearLayout.LayoutParams(dp(7), dp(7)))
            row.addView(text(name, 11f), LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(8) })
            row.addView(text(count.toString(), 12f, bold = true))
            legend.addView(row)
        }
        chartRow.addView(legend, LinearLayout.LayoutParams(0, -2, 1.15f).apply { leftMargin = dp(17) })
        status.addView(chartRow, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })

        val deliveries = card(content, 12)
        val deliveryTitle = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL }
        deliveryTitle.addView(text("Próximas entregas", 17f, bold = true), LinearLayout.LayoutParams(0, -2, 1f))
        deliveryTitle.addView(text("Ver todas", 11f, 0xFF4C88A7.toInt()).apply {
            gravity = Gravity.CENTER; minimumHeight = dp(48); minimumWidth = dp(60); clickable(this, onJobs)
        })
        deliveries.addView(deliveryTitle)
        listOf("Cozinha planejada" to "Ana Silva · 15/10", "Rack para sala" to "Bruno Souza · 18/10").forEach { (title, subtitle) ->
            val row = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL; minimumHeight = dp(48) }
            row.addView(FrameLayout(activity).apply {
                background = box(0xFFF3E7D8.toInt(), 8)
                addView(HomeIcon(activity, "clipboard", wood), FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER))
            }, LinearLayout.LayoutParams(dp(32), dp(34)))
            row.addView(column().apply {
                addView(text(title, 12f, bold = true))
                addView(text(subtitle, 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
            }, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(10) })
            row.addView(text("● Em produção", 10f, 0xFFC89530.toInt()))
            clickable(row) { info(title, "$subtitle\nEm produção\n\nEntrega ilustrativa do protótipo. Os serviços cadastrados estão na aba Serviços.") }
            deliveries.addView(row)
        }
        scroll.addView(content); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val navigation = LinearLayout(activity).apply { setBackgroundColor(white); gravity = Gravity.CENTER_VERTICAL; setPadding(dp(6), dp(7), dp(6), dp(5)) }
        val tabs = listOf("home" to "Início", "people" to "Clientes", "clipboard" to "Serviços", "menu" to "Mais")
        tabs.forEachIndexed { i, (symbol, label) ->
            val item = column().apply { gravity = Gravity.CENTER; minimumHeight = dp(56) }
            if (false) {
                item.addView(FrameLayout(activity).apply {
                    background = box(navy, 30)
                    addView(HomeIcon(activity, "plus", Color.WHITE), FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER))
                }, LinearLayout.LayoutParams(dp(50), dp(50)))
                item.contentDescription = "Cadastrar serviço"
            } else {
                item.addView(icon(symbol, if (i == 0) navy else muted, 24))
                item.addView(text(label, 10f, if (i == 0) navy else muted), LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(6) })
                item.contentDescription = label
            }
            item.isSelected = i == 0
            clickable(item) { when (i) {
                0 -> scroll.smoothScrollTo(0, 0)
                1 -> onClients()
                2 -> onJobs()
                else -> onMore()
            } }
            navigation.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(navigation)
        activity.setContentView(root); root.requestApplyInsets()
    }
}

private class StatusDonut(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    init { contentDescription = "16 serviços no total: 4 orçamentos, 4 em produção, 7 concluídos e 1 pendente. Dados de demonstração." }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val diameter = minOf(width, height).toFloat(); val stroke = diameter * .155f
        val bounds = RectF((width-diameter)/2+stroke/2, (height-diameter)/2+stroke/2, (width+diameter)/2-stroke/2, (height+diameter)/2-stroke/2)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = stroke
        var angle = -90f
        listOf(4 to 0xFFF4B030.toInt(), 4 to 0xFFC9CAC5.toInt(), 1 to 0xFFDA7378.toInt(), 7 to 0xFF259C7E.toInt()).forEach { (count,color) ->
            paint.color = color; val sweep = count * 360f / 16
            canvas.drawArc(bounds, angle, sweep, false, paint); angle += sweep
        }
    }
}

/** Small line icons drawn in a 24-unit coordinate system, without font glyphs. */
class HomeIcon(context: Context, private val kind: String, private val color: Int) : View(context) {
    override fun onDraw(c: Canvas) {
        super.onDraw(c); val save = c.save(); c.scale(width/24f, height/24f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = this@HomeIcon.color; style = Paint.Style.STROKE; strokeWidth = 1.5f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        fun line(vararg points: Float) { val path=Path();path.moveTo(points[0],points[1]);var i=2;while(i<points.size){path.lineTo(points[i],points[i+1]);i+=2};c.drawPath(path,p) }
        when(kind) {
            "wallet" -> { c.drawRoundRect(3f,3f,21f,21f,2f,2f,p);line(3f,7f,19f,7f);c.drawRoundRect(16f,11f,23f,16f,1f,1f,p) }
            "person", "people" -> { c.drawCircle(10f,7f,4f,p);c.drawArc(3f,13f,17f,27f,180f,180f,false,p); if(kind=="people"){c.drawArc(15f,3f,22f,11f,-90f,180f,false,p);c.drawArc(15f,13f,23f,26f,200f,150f,false,p)} }
            "clipboard" -> { c.drawRoundRect(5f,4f,20f,22f,2f,2f,p);c.drawRoundRect(9f,2f,16f,6f,1f,1f,p);line(11f,11f,16f,11f);line(11f,15f,16f,15f);c.drawPoint(8f,11f,p);c.drawPoint(8f,15f,p) }
            "shield" -> {line(12f,2f,20f,6f,20f,14f,18f,19f,12f,23f,6f,19f,4f,14f,4f,6f,12f,2f);line(8f,12f,11f,15f,16f,9f)}
            "home" -> {line(3f,10f,12f,3f,21f,10f,21f,21f,14f,21f,14f,14f,10f,14f,10f,21f,3f,21f,3f,10f)}
            "plus" -> {line(5f,12f,19f,12f);line(12f,5f,12f,19f)}
            "menu" -> {line(5f,6f,19f,6f);line(5f,12f,19f,12f);line(5f,18f,19f,18f)}
        }
        c.restoreToCount(save)
    }
}










