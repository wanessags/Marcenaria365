package br.com.nexo.marcenaria365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowInsets
import android.widget.*

class ProfileScreen(private val activity: Activity, private val onBack: () -> Unit) {
    private val navy=Color.rgb(26,52,67); private val cream=Color.rgb(250,245,239); private val ink=Color.rgb(42,57,66); private val muted=Color.rgb(150,148,143)
    private fun dp(n:Int)=(n*activity.resources.displayMetrics.density+.5f).toInt()
    fun show(){ val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(cream)}; root.setOnApplyWindowInsetsListener{v,i->if(android.os.Build.VERSION.SDK_INT>=30){val x=i.getInsets(WindowInsets.Type.systemBars());v.setPadding(x.left,x.top,x.right,x.bottom)};i}; val h=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),dp(12),dp(16),dp(12));setBackgroundColor(navy)}; h.addView(TextView(activity).apply{text="‹";textSize=38f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;setOnClickListener{onBack()}},LinearLayout.LayoutParams(dp(45),dp(48))); h.addView(TextView(activity).apply{text="Perfil";textSize=20f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD)},LinearLayout.LayoutParams(0,dp(48),1f)); root.addView(h); val c=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(28),dp(24),dp(24))}; c.addView(TextView(activity).apply{text="👩🏻";textSize=64f;gravity=Gravity.CENTER;setBackgroundColor(0xFFE9E0D7.toInt())},LinearLayout.LayoutParams(-1,dp(130))); c.addView(TextView(activity).apply{text="Mariana Oliveira";textSize=22f;setTextColor(ink);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(18)}); c.addView(TextView(activity).apply{text="mariana@email.com";textSize=14f;setTextColor(muted);gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)}); root.addView(c,LinearLayout.LayoutParams(-1,0,1f)); val b=Button(activity).apply{text="Voltar";isAllCaps=false;textSize=16f;setTextColor(Color.WHITE);background=GradientDrawable().apply{setColor(navy);cornerRadius=dp(10).toFloat()};setOnClickListener{onBack()}}; root.addView(b,LinearLayout.LayoutParams(-1,dp(52)).apply{leftMargin=dp(24);rightMargin=dp(24);bottomMargin=dp(18)}); activity.setContentView(root);root.requestApplyInsets() }
}
