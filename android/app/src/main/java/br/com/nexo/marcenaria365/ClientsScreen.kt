package br.com.nexo.marcenaria365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*

class ClientsScreen(
    private val activity: Activity,
    private val clients: List<Client>,
    private val onHome: () -> Unit,
    private val onServices: () -> Unit,
    private val onAdd: () -> Unit,
    private val onMore: () -> Unit
) {
    private val ink = Color.rgb(42, 57, 66)
    private val muted = Color.rgb(145, 143, 138)
    private val cream = Color.rgb(250, 246, 240)
    private val white = Color.rgb(255, 253, 249)
    private val navy = Color.rgb(26, 52, 67)
    private val tan = Color.rgb(190, 145, 106)
    private fun dp(n: Int) = (n * activity.resources.displayMetrics.density + .5f).toInt()
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun txt(value: String, size: Float, color: Int = ink, bold: Boolean = false) = TextView(activity).apply {
        text = value; textSize = size; setTextColor(color); includeFontPadding = false
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    fun show() {
        val root = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        root.setOnApplyWindowInsetsListener { v, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) { val s = insets.getInsets(WindowInsets.Type.systemBars()); v.setPadding(s.left,s.top,s.right,s.bottom) }
            else { @Suppress("DEPRECATION") val l=insets.systemWindowInsetLeft; @Suppress("DEPRECATION") val t=insets.systemWindowInsetTop; @Suppress("DEPRECATION") val r=insets.systemWindowInsetRight; @Suppress("DEPRECATION") val b=insets.systemWindowInsetBottom; v.setPadding(l,t,r,b) }; insets
        }
        val scroll = ScrollView(activity).apply { isFillViewport = true; isVerticalScrollBarEnabled = false }
        val content = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18),dp(16),dp(18),dp(18)) }
        val search = EditText(activity).apply {
            hint = "Buscar cliente..."; textSize = 14f; setTextColor(ink); setHintTextColor(muted); setSingleLine(true)
            setPadding(dp(44),0,dp(14),0); background = rounded(0xFFF0ECE7.toInt(),10); contentDescription = "Buscar cliente"
        }
        val searchFrame = FrameLayout(activity).apply { addView(search, FrameLayout.LayoutParams(-1,dp(52))) }
        searchFrame.addView(ClientsIcon(activity), FrameLayout.LayoutParams(dp(22),dp(22)).apply { leftMargin=dp(14); topMargin=dp(15) })
        content.addView(searchFrame)
        val tabRow = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0,dp(16),0,dp(12)) }
        val listBox = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        fun tab(label: String, selected: Boolean, action: () -> Unit) = TextView(activity).apply {
            text=label; textSize=12f; gravity=Gravity.CENTER; setTextColor(ink); setTypeface(null, if(selected) Typeface.BOLD else Typeface.NORMAL); minHeight=dp(38)
            if(selected) { background=rounded(0xFFF3E4D4.toInt(),24); setPadding(dp(6),0,dp(6),0) }
            isFocusable=true; setOnClickListener { action() }
        }
        var current = 0
        fun refreshTabs() {
            tabRow.removeAllViews()
            val labels = listOf("Todos (${clients.size})", "Ativos (${clients.size})", "Inativos (0)")
            labels.forEachIndexed { i,l -> tabRow.addView(tab(l,i==current) { current=i; refreshTabs(); search.setText(search.text) },LinearLayout.LayoutParams(0,dp(38),1f)) }
        }
        fun initials(name: String) = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        fun render() {
            listBox.removeAllViews(); val query=search.text.toString().trim().lowercase()
            clients.filter { it.name.lowercase().contains(query) || it.contact.lowercase().contains(query) }.forEach { client ->
                val row=LinearLayout(activity).apply { gravity=Gravity.CENTER_VERTICAL; minimumHeight=dp(82); setPadding(dp(6),dp(10),dp(3),dp(10)) }
                row.addView(TextView(activity).apply { text=initials(client.name); textSize=15f; gravity=Gravity.CENTER; setTextColor(Color.WHITE); background=rounded(tan,40) },LinearLayout.LayoutParams(dp(54),dp(54)))
                row.addView(LinearLayout(activity).apply { orientation=LinearLayout.VERTICAL; addView(txt(client.name,16f,bold=true)); addView(txt(client.contact,13f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)}) },LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(15)})
                row.addView(txt("›",30f,muted),LinearLayout.LayoutParams(dp(28),-1).apply{gravity=Gravity.CENTER_VERTICAL})
                row.contentDescription="${client.name}, ${client.contact}. Abrir cliente"; row.isFocusable=true
                row.setOnClickListener { Toast.makeText(activity,"Cliente: ${client.name}",Toast.LENGTH_SHORT).show() }
                listBox.addView(row); listBox.addView(View(activity).apply{setBackgroundColor(0xFFE6DED5.toInt())},LinearLayout.LayoutParams(-1,dp(1)))
            }
        }
        search.addTextChangedListener(object:TextWatcher { override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){render()};override fun afterTextChanged(e:Editable?){} })
        refreshTabs(); content.addView(tabRow); content.addView(listBox); render(); val add=Button(activity).apply{text="＋  Adicionar cliente";isAllCaps=false;textSize=15f;setTextColor(Color.WHITE);background=rounded(navy,10);setOnClickListener{onAdd()}}; content.addView(add,LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(18)}); scroll.addView(content); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(activity).apply{setBackgroundColor(white);gravity=Gravity.CENTER_VERTICAL;setPadding(dp(6),dp(6),dp(6),dp(5))}
        listOf("Início" to onHome,"Clientes" to {},"Serviços" to onServices,"Mais" to onMore).forEachIndexed { i,(label,action)->
            val v=TextView(activity).apply{text=label;textSize=11f;gravity=Gravity.CENTER;setTextColor(if(i==1)navy else muted);minimumHeight=dp(58); contentDescription=label;setOnClickListener{action()}}
            nav.addView(v,LinearLayout.LayoutParams(0,if(i==2)dp(52) else -2,1f).apply{leftMargin=dp(3);rightMargin=dp(3)})
        }; root.addView(nav); activity.setContentView(root); root.requestApplyInsets()
    }
}

private class ClientsIcon(context: android.content.Context): View(context){
    private val p=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=0xFF8E8C87.toInt();style=android.graphics.Paint.Style.STROKE;strokeWidth=1.8f}
    override fun onDraw(c:android.graphics.Canvas){c.drawCircle(10f,10f,6f,p);c.drawLine(14f,14f,20f,20f,p)}
}






