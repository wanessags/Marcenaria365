package br.com.nexo.marcenaria365

import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*

/** Telas da referência; nenhuma credencial é enviada ou salva. */
class ReferenceEntry(private val activity: Activity, private val onPreview: () -> Unit) {
    private val navy = Color.rgb(23,47,63)
    private val ink = Color.rgb(24,37,49)
    private val cream = Color.rgb(250,247,242)
    private val muted = Color.rgb(86,88,88)
    private fun dp(n: Int) = (n * activity.resources.displayMetrics.density).toInt()
    private fun shape(color: Int, radius: Int = 12, border: Boolean = false) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); if(border) setStroke(dp(1),0xFFDAD2C9.toInt())
    }
    private fun text(value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) = TextView(activity).apply {
        text = value; textSize = size; setTextColor(color); if(bold) setTypeface(null,Typeface.BOLD)
    }
    private fun message(title: String, body: String) { AlertDialog.Builder(activity).setTitle(title).setMessage(body).setPositiveButton("Entendi",null).show() }
    private fun column(photo: Boolean = false): LinearLayout {
        val root = FrameLayout(activity).apply { setBackgroundColor(cream) }
        if(photo) root.addView(ImageView(activity).apply {
            setImageResource(R.drawable.background); scaleType = ImageView.ScaleType.CENTER_CROP
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val safe = FrameLayout(activity)
        safe.setOnApplyWindowInsetsListener { v,insets ->
            if(android.os.Build.VERSION.SDK_INT >= 30) {
                val b = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime()); v.setPadding(b.left,b.top,b.right,b.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
            }; insets
        }
        val scroll = ScrollView(activity).apply { isFillViewport = true }
        val content = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(26),dp(12),dp(26),dp(20)) }
        scroll.addView(content); safe.addView(scroll,FrameLayout.LayoutParams(-1,-1)); root.addView(safe,FrameLayout.LayoutParams(-1,-1)); activity.setContentView(root)
        content.isFocusableInTouchMode = true; content.requestFocus(); return content
    }
    private fun logo(parent: LinearLayout,height: Int) {
        parent.addView(ImageView(activity).apply { setImageResource(R.drawable.logo); scaleType = ImageView.ScaleType.FIT_CENTER; contentDescription = "Marcenaria 365" },LinearLayout.LayoutParams(-1,dp(height)))
    }
    private fun button(parent: LinearLayout,title: String,secondary: Boolean = false,rounded: Boolean = false,action: () -> Unit) {
        parent.addView(Button(activity).apply {
            text = title; textSize = 16f; isAllCaps = false; minHeight = dp(52); setTextColor(if(secondary) ink else Color.WHITE)
            background = RippleDrawable(ColorStateList.valueOf(0x228EACBD),shape(if(secondary) Color.TRANSPARENT else navy,if(rounded) 28 else 11,secondary),null)
            setOnClickListener { action() }
        },LinearLayout.LayoutParams(-1,-2).apply { topMargin = dp(12) })
    }
    private fun back(parent: LinearLayout,action: () -> Unit) {
        parent.addView(Button(activity).apply { text = "‹"; textSize = 32f; setTextColor(navy); contentDescription = "Voltar"; background = RippleDrawable(ColorStateList.valueOf(0x228EACBD),null,null); setOnClickListener { action() } },LinearLayout.LayoutParams(dp(48),dp(48)))
    }
    fun show() {
        val content = column(true)
        val heading = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(0,dp(44),0,0) }
        logo(heading,210)
        heading.addView(text("Seu trabalho organizado,\ndo orçamento ao recebimento.",17f).apply { gravity = Gravity.CENTER; setLineSpacing(dp(2).toFloat(),1f) })
        content.addView(heading)
        content.addView(View(activity),LinearLayout.LayoutParams(1,dp(140),1f))
        content.addView(text("Arraste para a esquerda para continuar",15f,navy).apply { gravity = Gravity.CENTER; minimumHeight = dp(58); setOnClickListener { privacy() } })
        var startX = 0f
        content.setOnTouchListener { _, event -> when (event.action) { android.view.MotionEvent.ACTION_DOWN -> { startX = event.x; true }; android.view.MotionEvent.ACTION_UP -> { if (startX - event.x > dp(70)) privacy(); true }; else -> true } }
        content.addView(text("●  ○  ○  ○",15f,Color.WHITE).apply { gravity = Gravity.CENTER; setPadding(0,dp(16),0,0); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO })
    }
    private fun privacy() {
        val content = column(); back(content) { show() }
        content.addView(ImageView(activity).apply { setImageResource(R.drawable.ic_shield); setPadding(dp(19),dp(19),dp(19),dp(19)); background = shape(0xFFF3E6D7.toInt(),40); contentDescription = "Privacidade" },LinearLayout.LayoutParams(dp(72),dp(72)).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(12); bottomMargin = dp(22) })
        content.addView(text("Seus dados merecem proteção",20f,ink,true).apply { gravity = Gravity.CENTER })
        content.addView(text("A Marcenaria 365 foi pensada para organizar clientes, serviços, orçamentos e recebimentos.\n\nNesta prévia, use somente dados fictícios. O login ainda não está conectado ao servidor.",16f,muted).apply { setLineSpacing(dp(4).toFloat(),1f); setPadding(dp(8),dp(20),dp(8),dp(18)) })
        listOf("Seus dados de acesso não são enviados nesta prévia","E-mail e senha não são armazenados","Os registros de exemplo ficam apenas na sessão","A proteção da versão final será implementada e testada").forEach { content.addView(text("•  $it",15f).apply { setPadding(dp(10),dp(14),dp(10),dp(14)) }) }
        content.addView(View(activity),LinearLayout.LayoutParams(1,dp(20),1f))
        content.addView(text("Política de Privacidade e Termos de Uso",14f,navy).apply { gravity = Gravity.CENTER; minimumHeight = dp(48); isFocusable = true; setOnClickListener { message("Privacidade da demonstração","Não há autenticação real ou envio de credenciais. Os dados de exemplo são temporários. A política e os termos definitivos serão apresentados antes do uso real.") } })
        button(content,"Continuar") { login() }
        var privacyStartX = 0f
        content.setOnTouchListener { _, event -> when (event.action) { android.view.MotionEvent.ACTION_DOWN -> { privacyStartX = event.x; true }; android.view.MotionEvent.ACTION_UP -> { if (privacyStartX - event.x > dp(70)) login(); true }; else -> true } }
    }
    private fun login() {
        val content = column(); back(content) { privacy() }; logo(content,140)
        content.addView(text("Bem-vindo de volta!",18f,muted).apply { gravity = Gravity.CENTER })
        content.addView(text("Acesse sua conta para continuar.",15f,muted).apply { gravity = Gravity.CENTER; setPadding(0,dp(5),0,dp(24)) })
        fun field(title: String,password: Boolean = false): EditText {
            val caption = text(title,13f,muted); content.addView(caption,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin = dp(5) })
            val row = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL; background = shape(0xFFFCFAF7.toInt(),10,true); setPadding(dp(12),0,dp(4),0) }
            row.addView(ImageView(activity).apply { setImageResource(if(password) R.drawable.ic_lock else R.drawable.ic_mail); setPadding(dp(3),dp(12),dp(3),dp(12)); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO },LinearLayout.LayoutParams(dp(28),dp(48)))
            val input = EditText(activity).apply {
                id = View.generateViewId(); hint = if(password) "Sua senha" else "seu@email.com"; textSize = 16f; setTextColor(ink); setHintTextColor(muted)
                minHeight = dp(52); background = null; setSingleLine(true); isSaveEnabled = false; setPadding(dp(8),dp(8),dp(4),dp(8))
                inputType = InputType.TYPE_CLASS_TEXT or if(password) InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                if(password) transformationMethod = PasswordTransformationMethod.getInstance()
            }; caption.labelFor = input.id
            row.addView(input,LinearLayout.LayoutParams(0,-2,1f))
            if(password) row.addView(ImageButton(activity).apply {
                setImageResource(R.drawable.ic_eye); setPadding(dp(12),dp(14),dp(12),dp(14)); contentDescription = "Mostrar senha"; background = RippleDrawable(ColorStateList.valueOf(0x228EACBD),null,null)
                setOnClickListener { val visible = input.transformationMethod == null; input.transformationMethod = if(visible) PasswordTransformationMethod.getInstance() else null; contentDescription = if(visible) "Mostrar senha" else "Ocultar senha"; input.setSelection(input.text.length) }
            },LinearLayout.LayoutParams(dp(48),dp(52)))
            content.addView(row,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin = dp(10) }); return input
        }
        val email = field("E-mail"); val password = field("Senha",true)
        content.addView(text("Esqueceu sua senha?",14f,navy).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL; minimumHeight = dp(48); isFocusable = true; setOnClickListener { message("Recuperação de senha","Este botão faz parte do protótipo. A recuperação será conectada ao servidor na próxima etapa.") } },LinearLayout.LayoutParams(-1,-2))
        content.addView(View(activity),LinearLayout.LayoutParams(1,dp(20),1f))
        button(content,"Entrar") { AlertDialog.Builder(activity).setTitle("Login demonstrativo").setMessage("A autenticação ainda não está conectada. Abrir as telas de exemplo?").setNegativeButton("Voltar",null).setPositiveButton("Ver telas") { _,_ -> onPreview() }.show() }
        content.addView(text("Ainda não tem uma conta? Criar conta",14f,navy).apply { gravity = Gravity.CENTER; minimumHeight = dp(48); paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG; isFocusable = true; setOnClickListener { RegisterScreen(activity) { login() }.show() } },LinearLayout.LayoutParams(-1,-2))
    }
}







