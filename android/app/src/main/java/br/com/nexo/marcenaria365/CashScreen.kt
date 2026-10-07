package br.com.nexo.marcenaria365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowInsets
import android.widget.*

class CashScreen(private val activity: Activity, private val back: () -> Unit) {
    private val ink=Color.rgb(42,57,66); private val muted=Color.rgb(150,146,140); private val cream=Color.rgb(250,246,240); private val white=Color.rgb(255,253,249); private val navy=Color.rgb(26,52,67); private val green=Color.rgb(35,159,126); private val orange=Color.rgb(242,169,38); private val blue=Color.rgb(61,143,193)
    private fun dp(n:Int)=(n*activity.resources.displayMetrics.density+.5f).toInt()
    private fun bg(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun txt(s:String,z:Float,c:Int=ink,b:Boolean=false)=TextView(activity).apply{text=s;textSize=z;setTextColor(c);includeFontPadding=false;if(b)setTypeface(null,Typeface.BOLD)}
    fun show(){
        val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(cream)}
        root.setOnApplyWindowInsetsListener{v,i->if(android.os.Build.VERSION.SDK_INT>=30){val s=i.getInsets(WindowInsets.Type.systemBars());v.setPadding(s.left,s.top,s.right,s.bottom)};i}
        val head=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),0,dp(12),0);setBackgroundColor(navy)}
        head.addView(TextView(activity).apply{text="‹";textSize=38f;setTextColor(Color.WHITE);setOnClickListener{back()}},LinearLayout.LayoutParams(dp(45),dp(64)))
        head.addView(txt("Caixa",20f,Color.WHITE,true),LinearLayout.LayoutParams(0,dp(64),1f).apply{gravity=Gravity.CENTER})
        head.addView(txt("⋮",30f,Color.WHITE),LinearLayout.LayoutParams(dp(30),dp(64)));root.addView(head)
        val scroll=ScrollView(activity).apply{isFillViewport=true;isVerticalScrollBarEnabled=false};val c=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(22),dp(20),dp(24))}
        val title=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL};title.addView(txt("Resumo financeiro",20f,b=true),LinearLayout.LayoutParams(0,-2,1f));title.addView(txt("Este mês  ⌄",12f,muted));c.addView(title)
        val cards=LinearLayout(activity).apply{setPadding(0,dp(22),0,dp(12))};listOf("↙\nEntradas\nR$ 6.850,00" to 0xFFE0F3EA.toInt(),"↗\nSaídas\nR$ 2.000,00" to 0xFFF7E8D7.toInt(),"▣\nSaldo\nR$ 4.850,00" to 0xFFF0EFEC.toInt()).forEach{(v,col)->cards.addView(txt(v,14f,if(v.contains("6.850"))green else if(v.contains("2.000"))0xFFB5845D.toInt() else ink,true).apply{gravity=Gravity.CENTER;setLineSpacing(dp(8).toFloat(),1f);background=bg(col,9);setPadding(dp(4),dp(18),dp(4),dp(18))},LinearLayout.LayoutParams(0,dp(142),1f).apply{if(cards.childCount>0)leftMargin=dp(9)})};c.addView(cards)
        c.addView(txt("ⓘ   Acompanhe suas entradas e saídas para manter\n      as finanças sempre em dia.",12f,muted).apply{background=bg(0xFFF7E7D3.toInt(),8);setPadding(dp(14),dp(14),dp(10),dp(14))})
        val ht=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(26),0,dp(10))};ht.addView(txt("Últimas movimentações",18f,b=true),LinearLayout.LayoutParams(0,-2,1f));ht.addView(txt("Ver todas",11f,0xFF4485A5.toInt()));c.addView(ht)
        val rows=listOf(arrayOf("Pagamento recebido","Ana Silva · Cozinha planejada","R$ 1.500,00","15/10/2026",green),arrayOf("Compra de materiais","MDF e ferragens · Cozinha planejada","R$ 850,00","14/10/2026",orange),arrayOf("Pagamento recebido","Bruno Souza · Rack para sala","R$ 1.000,00","13/10/2026",blue),arrayOf("Pagamento recebido","Carlos Fernandes · Guarda-roupa","R$ 2.350,00","12/10/2026",green))
        rows.forEach{item->val row=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(12),0,dp(12))};row.addView(txt("↙",21f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=bg(item[4] as Int,30)},LinearLayout.LayoutParams(dp(38),dp(38)));row.addView(LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;addView(txt(item[0] as String,13f,b=true));addView(txt(item[1] as String,10f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)});addView(txt(item[2] as String,16f,item[4] as Int,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})},LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(12)});row.addView(txt(item[3] as String,10f,muted));c.addView(row)}
        scroll.addView(c);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));activity.setContentView(root);root.requestApplyInsets()
    }
}
