package br.com.nexo.marcenaria365

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*

class ServicesScreen(private val activity: Activity, private val store: DemoStore, private val onHome:()->Unit, private val onClients:()->Unit, private val onAdd:()->Unit, private val onOpen:()->Unit, private val onMore:()->Unit) {
    private val ink=Color.rgb(42,57,66); private val muted=Color.rgb(145,143,138); private val cream=Color.rgb(250,246,240); private val white=Color.rgb(255,253,249); private val navy=Color.rgb(26,52,67); private val tan=Color.rgb(190,145,106); private val green=Color.rgb(40,155,125)
    private fun dp(n:Int)=(n*activity.resources.displayMetrics.density+.5f).toInt()
    private fun bg(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun t(s:String,z:Float,c:Int=ink,b:Boolean=false)=TextView(activity).apply{text=s;textSize=z.toFloat();setTextColor(c);includeFontPadding=false;if(b)setTypeface(null,Typeface.BOLD)}
    fun show(){
        val root=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(cream)}
        root.setOnApplyWindowInsetsListener{v,i->if(android.os.Build.VERSION.SDK_INT>=30){val s=i.getInsets(WindowInsets.Type.systemBars());v.setPadding(s.left,s.top,s.right,s.bottom)};i}
        val scroll=ScrollView(activity).apply{isFillViewport=true;isVerticalScrollBarEnabled=false}; val content=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(16),dp(18),dp(20))}
        val search=EditText(activity).apply{hint="Buscar serviço...";textSize=14f;setSingleLine(true);setTextColor(ink);setHintTextColor(muted);setPadding(dp(44),0,dp(12),0);background=bg(0xFFF0ECE7.toInt(),10);contentDescription="Buscar serviço"}
        val sf=FrameLayout(activity);sf.addView(search,FrameLayout.LayoutParams(-1,dp(52)));sf.addView(ServicesSearchIcon(activity),FrameLayout.LayoutParams(dp(22),dp(22)).apply{leftMargin=dp(14);topMargin=dp(15)});content.addView(sf)
        val tabs=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(16),0,dp(12))}; val all=t("Todos (8)",12f,b=true).apply{gravity=Gravity.CENTER;background=bg(0xFFF3E4D4.toInt(),24);minimumHeight=dp(38)};val running=t("Em andamento (4)",12f).apply{gravity=Gravity.CENTER;minimumHeight=dp(38)};tabs.addView(all,LinearLayout.LayoutParams(0,dp(38),1f));tabs.addView(running,LinearLayout.LayoutParams(0,dp(38),1f));content.addView(tabs)
        val list=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL}; val demo=listOf(arrayOf("Cozinha planejada","Ana Silva","Entrega: 15/10/2026","Em produção"), arrayOf("Rack para sala","Bruno Souza","Entrega: 18/10/2026","Orçamento"), arrayOf("Guarda-roupa","Carlos Fernandes","Entrega: 22/10/2026","Em produção"), arrayOf("Mesa de jantar","Débora Martins","Entrega: 25/10/2026","Concluído"), arrayOf("Painel de TV","Juliana Almeida","Entrega: 28/10/2026","Em produção"))
        fun render(){list.removeAllViews();val q=search.text.toString().lowercase();demo.filter{it[0].lowercase().contains(q)||it[1].lowercase().contains(q)}.forEach{item->val row=LinearLayout(activity).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(2),dp(10),dp(2),dp(10));minimumHeight=dp(112)};val image=ImageView(activity).apply{setImageResource(br.com.nexo.marcenaria365.R.drawable.background);scaleType=ImageView.ScaleType.CENTER_CROP;contentDescription="Imagem ilustrativa de ${item[0]}";background=bg(0xFFE6D6C5.toInt(),5)};row.addView(image,LinearLayout.LayoutParams(dp(82),dp(76)));row.addView(LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL;addView(t(item[0],16f,b=true));addView(t(item[1],13f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)});addView(t(item[2],12f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)});addView(t(item[3],11f,if(item[3]=="Concluído")green else tan),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})},LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(14)});row.contentDescription="${item[0]}, ${item[1]}, ${item[2]}, ${item[3]}";row.setOnClickListener{onOpen()};list.addView(row);list.addView(View(activity).apply{setBackgroundColor(0xFFE6DED5.toInt())},LinearLayout.LayoutParams(-1,dp(1)))} }
        search.addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){render()};override fun afterTextChanged(e:android.text.Editable?){} });render();content.addView(list); val add=Button(activity).apply{text="＋  Adicionar serviço";isAllCaps=false;textSize=15f;setTextColor(Color.WHITE);background=bg(navy,10);setOnClickListener{onAdd()}};content.addView(add,LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(18)});scroll.addView(content);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(activity).apply{setBackgroundColor(white);gravity=Gravity.CENTER_VERTICAL;setPadding(dp(6),dp(6),dp(6),dp(5))};listOf("Início" to onHome,"Clientes" to onClients,"Serviços" to {},"Mais" to onMore).forEachIndexed{i,p->nav.addView(TextView(activity).apply{text=p.first;textSize=11f;gravity=Gravity.CENTER;setTextColor(if(i==3)navy else muted);minimumHeight=dp(58);setOnClickListener{p.second()}},LinearLayout.LayoutParams(0,if(i==2)dp(52)else -2,1f))};root.addView(nav);activity.setContentView(root);root.requestApplyInsets()
    }
}
private class ServicesSearchIcon(c:android.content.Context):View(c){private val p=android.graphics.Paint(1).apply{color=0xFF8E8C87.toInt();style=android.graphics.Paint.Style.STROKE;strokeWidth=1.8f};override fun onDraw(c:android.graphics.Canvas){c.drawCircle(10f,10f,6f,p);c.drawLine(14f,14f,20f,20f,p)}}





