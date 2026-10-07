package br.com.nexo.marcenaria365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*

class MoreScreen(private val activity: Activity, private val onHome: () -> Unit, private val onLogout: () -> Unit, private val onPrivacy: () -> Unit, private val onProfile: () -> Unit, private val onClients: () -> Unit, private val onServices: () -> Unit) {
    private val navy = Color.rgb(26, 52, 67)
    private val ink = Color.rgb(42, 57, 66)
    private val cream = Color.rgb(250, 245, 239)
    private val white = Color.rgb(255, 253, 249)
    private val muted = Color.rgb(150, 148, 143)
    private fun dp(n: Int) = (n * activity.resources.displayMetrics.density + .5f).toInt()
    private fun box(color: Int, radius: Int = 12) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun text(value: String, size: Float, color: Int = ink, bold: Boolean = false) = TextView(activity).apply { text=value; textSize=size; setTextColor(color); includeFontPadding=false; if (bold) setTypeface(null, Typeface.BOLD) }
    private fun rowIcon(kind: String) = HomeIcon(activity, kind, muted).apply { layoutParams=LinearLayout.LayoutParams(dp(28),dp(28)); importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO }
    fun show() {
        val root=LinearLayout(activity).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(cream) }
        root.setOnApplyWindowInsetsListener { v,i -> if (android.os.Build.VERSION.SDK_INT>=30) { val s=i.getInsets(WindowInsets.Type.systemBars()); v.setPadding(s.left,s.top,s.right,s.bottom) }; i }
        val header=LinearLayout(activity).apply { gravity=Gravity.CENTER_VERTICAL; setPadding(dp(20),dp(13),dp(16),dp(13)); setBackgroundColor(navy) }
        header.addView(text("☰",27f,Color.WHITE),LinearLayout.LayoutParams(dp(45),dp(48)))
        header.addView(text("Mais",20f,Color.WHITE,true).apply { gravity=Gravity.CENTER },LinearLayout.LayoutParams(0,dp(48),1f))
        header.addView(text("⋮",28f,Color.WHITE).apply { gravity=Gravity.CENTER },LinearLayout.LayoutParams(dp(28),dp(48)))
        root.addView(header)
        val scroll=ScrollView(activity).apply { isFillViewport=true; isVerticalScrollBarEnabled=false }
        val content=LinearLayout(activity).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(20),dp(20),dp(20)) }
        val profile=LinearLayout(activity).apply { gravity=Gravity.CENTER_VERTICAL; setPadding(dp(16),dp(12),dp(12),dp(12)); background=box(white); addView(TextView(activity).apply{text="👩🏻";textSize=32f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(56),dp(56))); addView(LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;addView(text("Mariana Oliveira",18f,ink,true));addView(text("mariana@email.com",12f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})},LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(14)}); addView(text("›",30f,muted)); setOnClickListener{onProfile()} }
        content.addView(profile,LinearLayout.LayoutParams(-1,dp(88)))
        content.addView(text("Acesso rápido",18f,ink,true), LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6);bottomMargin=dp(10)})
        val options=listOf("box" to "Materiais","settings" to "Configurações","chat" to "Central de atendimento","help" to "Ajuda e suporte","document" to "Política de Privacidade")
        options.forEach { (kind,label) -> val r=LinearLayout(activity).apply { gravity=Gravity.CENTER_VERTICAL; minimumHeight=dp(64); setPadding(dp(10),0,dp(8),0); addView(rowIcon(kind)); addView(text(label,16f),LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(18)}); addView(text("›",28f,muted)); setOnClickListener { if (label == "Política de Privacidade") onPrivacy() else Toast.makeText(activity,"$label em breve",Toast.LENGTH_SHORT).show() }; contentDescription=label }; content.addView(r) }
        val exit=TextView(activity).apply { text="⇥   Sair da conta"; textSize=16f; setTextColor(0xFFE76D68.toInt()); gravity=Gravity.CENTER; setTypeface(null,Typeface.BOLD); background=box(white); minimumHeight=dp(52); setOnClickListener{onLogout()} }
        content.addView(exit,LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(12)})
        scroll.addView(content); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(activity).apply { setBackgroundColor(white); gravity=Gravity.CENTER; setPadding(dp(8),dp(7),dp(8),dp(5)) }
        listOf("home" to "Início","people" to "Clientes","clipboard" to "Serviços","menu" to "Mais").forEachIndexed { i,(kind,label) -> val item=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;minimumHeight=dp(56);contentDescription=if(label.isEmpty())"Cadastrar serviço" else label}; item.addView(rowIcon(kind),LinearLayout.LayoutParams(dp(28),dp(28))); if(label.isNotEmpty()) item.addView(text(label,10f,if(i==4)navy else muted)); item.setOnClickListener{when(i){0->onHome();1->onClients();2->onServices();3->onHome()}}; nav.addView(item,LinearLayout.LayoutParams(0,-2,1f)) }
        root.addView(nav); activity.setContentView(root); root.requestApplyInsets()
    }
}








