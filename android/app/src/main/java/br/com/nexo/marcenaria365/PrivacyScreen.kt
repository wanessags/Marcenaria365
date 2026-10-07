package br.com.nexo.marcenaria365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*

class PrivacyScreen(private val activity: Activity, private val onBack: () -> Unit) {
    private val navy = Color.rgb(26,52,67)
    private val ink = Color.rgb(42,57,66)
    private val muted = Color.rgb(150,148,143)
    private val cream = Color.rgb(250,245,239)
    private fun dp(n:Int)=(n*activity.resources.displayMetrics.density+.5f).toInt()
    private fun text(v:String,s:Float,c:Int=ink,b:Boolean=false)=TextView(activity).apply{text=v;textSize=s;setTextColor(c);includeFontPadding=false;if(b)setTypeface(null,Typeface.BOLD)}
    fun show(){
        val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(cream)}
        root.setOnApplyWindowInsetsListener{v,i->if(android.os.Build.VERSION.SDK_INT>=30){val x=i.getInsets(WindowInsets.Type.systemBars());v.setPadding(x.left,x.top,x.right,x.bottom)};i}
        val head=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),dp(12),dp(16),dp(12));setBackgroundColor(navy)}
        val back=text("‹",38f,Color.WHITE).apply{gravity=Gravity.CENTER;setOnClickListener{onBack()};contentDescription="Voltar"}
        head.addView(back,LinearLayout.LayoutParams(dp(45),dp(48))); head.addView(text("Política de Privacidade",20f,Color.WHITE,true),LinearLayout.LayoutParams(0,dp(48),1f)); head.addView(text("⋮",28f,Color.WHITE).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(28),dp(48)))
        root.addView(head)
        val scroll=ScrollView(activity).apply{isFillViewport=true;isVerticalScrollBarEnabled=false}; val content=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(25),dp(25),dp(25),dp(18))}
        val sections=listOf(
            "1. Coleta de informações" to "Coletamos informações que você fornece ao criar sua conta e utilizar o aplicativo, como nome, e-mail e dados dos clientes e serviços cadastrados.",
            "2. Uso dos dados" to "Os dados são utilizados para organizar sua marcenaria, gerenciar serviços e melhorar sua experiência no aplicativo.",
            "3. Compartilhamento" to "Não vendemos seus dados pessoais. As informações são compartilhadas apenas quando necessário para a prestação dos serviços ou por obrigação legal.",
            "4. Armazenamento e segurança" to "Adotamos medidas de segurança para proteger seus dados contra acesso não autorizado, perda ou alteração.",
            "5. Seus direitos" to "Você pode solicitar acesso, correção ou exclusão dos seus dados pessoais a qualquer momento, conforme a LGPD.",
            "6. Contato" to "Em caso de dúvidas sobre esta política, entre em contato com nossa equipe pelo e-mail privacidade@marcenaria365.com."
        )
        sections.forEach{(title,body)->content.addView(text(title,15f,ink,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)});content.addView(text(body,13f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10);bottomMargin=dp(21)})}
        scroll.addView(content);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val understood=Button(activity).apply{text="Entendi";textSize=16f;isAllCaps=false;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);background=GradientDrawable().apply{setColor(navy);cornerRadius=dp(10).toFloat()};setOnClickListener{onBack()}}
        root.addView(understood,LinearLayout.LayoutParams(-1,dp(52)).apply{leftMargin=dp(25);rightMargin=dp(25);bottomMargin=dp(18)})
        activity.setContentView(root);root.requestApplyInsets()
    }
}
