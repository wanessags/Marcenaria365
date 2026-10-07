package br.com.nexo.marcenaria365

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*
import kotlin.math.*

/** Tela de apresentação: não envia, salva ou autentica credenciais. */
class LoginScreen(private val activity: Activity, private val onPreview: () -> Unit) {
    private val brown = Color.rgb(108, 55, 27)
    private val ink = Color.rgb(49, 37, 28)
    private fun dp(n: Int) = (n * activity.resources.displayMetrics.density).roundToInt()
    private fun box(color: Int, radius: Int, border: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); if (border != null) setStroke(dp(1), border)
    }
    private fun text(value: CharSequence, size: Float, color: Int = ink, bold: Boolean = false) = TextView(activity).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun message(title: String, value: String) {
        AlertDialog.Builder(activity).setTitle(title).setMessage(value).setPositiveButton("Entendi", null).show()
    }
    fun show() {
        val root = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(250,247,242)) }
        root.setOnApplyWindowInsetsListener { v, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val safe = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                v.setPadding(safe.left,safe.top,safe.right,safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
            }; insets
        }
        val scroll = ScrollView(activity).apply { isFillViewport = true; clipToPadding = false }
        val column = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(column); root.addView(scroll,LinearLayout.LayoutParams(-1,-1))

        val hero = FrameLayout(activity)
        hero.background = WoodBackdrop()
        val brand = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(24),dp(24),dp(24),dp(22))
        }
        brand.addView(CarpentryMark(activity).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO },LinearLayout.LayoutParams(dp(86),dp(86)))
        val name = SpannableString("Marcenaria 365").apply { setSpan(ForegroundColorSpan(Color.rgb(237,167,82)),11,14,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        brand.addView(text(name,30f,Color.WHITE,true).apply {
            gravity = Gravity.CENTER; setPadding(0,dp(8),0,dp(3))
            if (android.os.Build.VERSION.SDK_INT >= 28) isAccessibilityHeading = true
        })
        brand.addView(text("Seu trabalho organizado,\ndo orçamento à entrega.",16f,Color.rgb(255,239,216)).apply { gravity = Gravity.CENTER; setLineSpacing(dp(2).toFloat(),1f) })
        hero.addView(brand,FrameLayout.LayoutParams(-1,-2)); column.addView(hero,LinearLayout.LayoutParams(-1,-2))

        val form = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(28),dp(22),dp(28),dp(20)) }
        column.addView(form,LinearLayout.LayoutParams(-1,-2))
        fun label(value: String): TextView = text(value,14f,ink,true).also { form.addView(it,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin = dp(7) }) }
        val emailLabel = label("E-mail")
        val email = EditText(activity).apply {
            id = View.generateViewId(); hint = "seu@email.com"; textSize = 16f; setTextColor(ink); setHintTextColor(Color.rgb(113,107,101))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setSingleLine(true); setPadding(dp(14),dp(12),dp(14),dp(12)); minimumHeight = dp(52)
            background = box(Color.WHITE,8,Color.rgb(184,167,149)); isSaveEnabled = false
        }
        emailLabel.labelFor = email.id
        form.addView(email,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin = dp(18) })
        val passwordLabel = label("Senha")
        val passwordRow = LinearLayout(activity).apply { gravity = Gravity.CENTER_VERTICAL; background = box(Color.WHITE,8,Color.rgb(184,167,149)) }
        val password = EditText(activity).apply {
            id = View.generateViewId(); hint = "Digite sua senha"; textSize = 16f; setTextColor(ink); setHintTextColor(Color.rgb(113,107,101))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            transformationMethod = PasswordTransformationMethod.getInstance(); setSingleLine(true)
            background = null; setPadding(dp(14),dp(12),0,dp(12)); minimumHeight = dp(52); isSaveEnabled = false
        }
        passwordLabel.labelFor = password.id
        passwordRow.addView(password,LinearLayout.LayoutParams(0,-2,1f))
        val eye = ImageButton(activity).apply {
            setImageDrawable(SymbolDrawable("eye",brown)); background = RippleDrawable(ColorStateList.valueOf(0x22784322),null,null)
            contentDescription = "Mostrar senha"; setPadding(dp(13),dp(13),dp(13),dp(13))
            setOnClickListener {
                val showing = password.transformationMethod == null
                password.transformationMethod = if(showing) PasswordTransformationMethod.getInstance() else null
                contentDescription = if(showing) "Mostrar senha" else "Ocultar senha"
                password.setSelection(password.text.length)
            }
        }
        passwordRow.addView(eye,LinearLayout.LayoutParams(dp(52),dp(52)))
        form.addView(passwordRow,LinearLayout.LayoutParams(-1,-2))
        val forgot = text("Esqueci minha senha",14f,brown).apply {
            gravity = Gravity.CENTER; minimumHeight = dp(48); paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            isFocusable = true; setOnClickListener { message("Recuperação de senha","A recuperação estará disponível quando conectarmos o aplicativo ao servidor.") }
        }
        form.addView(forgot,LinearLayout.LayoutParams(-1,-2))
        fun link(value: String,symbol: String,action: () -> Unit) {
            val row = LinearLayout(activity).apply {
                gravity = Gravity.CENTER_VERTICAL; minimumHeight = dp(52); setPadding(dp(12),0,dp(12),0)
                background = RippleDrawable(ColorStateList.valueOf(0x22784322),null,null)
                isFocusable = true; contentDescription = value; setOnClickListener { action() }
            }
            row.addView(ImageView(activity).apply { setImageDrawable(SymbolDrawable(symbol,brown)); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO },LinearLayout.LayoutParams(dp(21),dp(21)))
            row.addView(text(value,15f).apply { setPadding(dp(12),0,0,0); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO })
            form.addView(row,LinearLayout.LayoutParams(-1,-2))
        }
        link("Aviso de privacidade","shield") { message("Privacidade nesta prévia","Esta versão demonstra as telas. Não envia nem armazena e-mail ou senha e não está conectada ao servidor. Use somente dados fictícios. O aviso definitivo será apresentado antes do uso real.") }
        form.addView(View(activity).apply { setBackgroundColor(Color.rgb(225,214,203)) },LinearLayout.LayoutParams(-1,dp(1)))
        link("Sair do app","exit") { activity.finish() }
        form.addView(text("Protótipo visual • login ainda não conectado",12f,Color.rgb(103,87,73)).apply { gravity = Gravity.CENTER; setPadding(0,dp(18),0,0) })
        var downY = 0f
        scroll.setOnTouchListener { _, event -> when (event.action) { android.view.MotionEvent.ACTION_DOWN -> { downY = event.y; true }; android.view.MotionEvent.ACTION_UP -> { if (downY - event.y > dp(90)) onPreview(); true }; else -> true } }
        activity.setContentView(root)
        root.isFocusableInTouchMode = true; root.requestFocus()
    }
}

/** Arte vetorial escalável: madeira e marca de marcenaria, sem imagem esticada. */
private class WoodBackdrop : android.graphics.drawable.Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        val w = bounds.width().toFloat(); val h = bounds.height().toFloat()
        p.shader = LinearGradient(0f,0f,w,h,intArrayOf(0xFF211710.toInt(),0xFF563722.toInt(),0xFF241810.toInt()),null,Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w,h,p); p.shader = null
        for(i in 0..5) {
            val y = h*i/5f
            p.color = 0x880E0906.toInt(); p.strokeWidth = 3f; c.drawLine(0f,y,w,y-10f,p)
        }
        p.style = Paint.Style.STROKE
        for(i in 0..64) {
            val y = h*i/64f; val path = Path(); path.moveTo(0f,y)
            path.cubicTo(w*.25f,y+sin(i.toFloat())*12,w*.65f,y-10,w,y+5)
            p.color = if(i%3==0) 0x188F643D else 0x18000000; p.strokeWidth = 1.4f; c.drawPath(path,p)
        }
        p.style = Paint.Style.FILL
        p.shader = LinearGradient(0f,0f,0f,h,intArrayOf(0x11000000,0x55000000),null,Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w,h,p); p.shader = null
    }
    override fun setAlpha(alpha: Int) { p.alpha = alpha }
    override fun setColorFilter(filter: ColorFilter?) { p.colorFilter = filter }
    @Deprecated("Deprecated in Java") override fun getOpacity() = PixelFormat.OPAQUE
}
private class CarpentryMark(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        canvas.save(); canvas.scale(width/100f,height/100f)
        p.color = 0xFFAA6734.toInt(); canvas.drawRoundRect(2f,2f,98f,98f,12f,12f,p)
        p.color = 0xFF402619.toInt(); canvas.drawRoundRect(8f,8f,92f,92f,8f,8f,p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 2f; p.color = 0xFFDBA36C.toInt(); canvas.drawRoundRect(5f,5f,95f,95f,10f,10f,p); p.style = Paint.Style.FILL
        val teeth = Path()
        for(i in 0 until 72) {
            val a = i*Math.PI*2/72; val r = if(i%3==0) 35f else 30f
            val x = 51f+cos(a).toFloat()*r; val y = 49f+sin(a).toFloat()*r
            if(i==0) teeth.moveTo(x,y) else teeth.lineTo(x,y)
        }
        teeth.close(); p.color = 0xFFF9ECD4.toInt(); canvas.drawPath(teeth,p)
        p.color = 0xFF402619.toInt(); canvas.drawCircle(51f,49f,20f,p)
        canvas.save(); canvas.rotate(-35f,50f,51f)
        p.color = 0xFFF9ECD4.toInt(); canvas.drawRoundRect(27f,42f,77f,56f,3f,3f,p)
        p.color = 0xFF402619.toInt(); canvas.drawRoundRect(60f,45f,71f,51f,2f,2f,p); canvas.restore()
        p.color = 0xFFCE8E50.toInt(); canvas.drawRoundRect(14f,75f,86f,88f,2f,2f,p)
        p.color = 0xFF402619.toInt(); p.strokeWidth = 2f
        for(i in 0..6) canvas.drawLine(21f+i*9,77f,21f+i*9,if(i%2==0) 85f else 81f,p)
        canvas.restore()
    }
}
private class SymbolDrawable(private val kind: String,private val color: Int) : android.graphics.drawable.Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        c.save(); c.translate(bounds.left.toFloat(),bounds.top.toFloat()); c.scale(bounds.width()/24f,bounds.height()/24f)
        p.color = color; p.style = Paint.Style.STROKE; p.strokeWidth = 1.8f; p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        val path = Path()
        when(kind) {
            "eye" -> { path.moveTo(2f,12f);path.cubicTo(7f,4f,17f,4f,22f,12f);path.cubicTo(17f,20f,7f,20f,2f,12f);c.drawPath(path,p);c.drawCircle(12f,12f,3f,p) }
            "shield" -> { path.moveTo(12f,2f);path.lineTo(21f,6f);path.lineTo(19f,15f);path.quadTo(17f,20f,12f,22f);path.quadTo(7f,20f,5f,15f);path.lineTo(3f,6f);path.close();c.drawPath(path,p);c.drawLine(8f,12f,11f,15f,p);c.drawLine(11f,15f,17f,9f,p) }
            else -> { path.moveTo(11f,3f);path.lineTo(4f,3f);path.lineTo(4f,21f);path.lineTo(11f,21f);c.drawPath(path,p);c.drawLine(10f,12f,22f,12f,p);c.drawLine(17f,7f,22f,12f,p);c.drawLine(17f,17f,22f,12f,p) }
        }; c.restore()
    }
    override fun setAlpha(alpha: Int) { p.alpha = alpha }
    override fun setColorFilter(filter: ColorFilter?) { p.colorFilter = filter }
    @Deprecated("Deprecated in Java") override fun getOpacity() = PixelFormat.TRANSLUCENT
}


