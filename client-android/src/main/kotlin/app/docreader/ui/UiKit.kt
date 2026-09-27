package app.docreader.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import app.docreader.render.Palette

class UiKit(val context: Context, val colors: Palette) {
    fun dp(value: Int) = (value*context.resources.displayMetrics.density+.5f).toInt()
    fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    fun row() = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun text(value: String, size: Float = 16f, bold: Boolean = false) = TextView(context).apply {
        text = value; textSize = size; setTextColor(colors.ink)
        if (bold) setTypeface(typeface,Typeface.BOLD)
        setPadding(dp(12),dp(8),dp(12),dp(8))
    }
    fun button(label: String, action: () -> Unit) = Button(context).apply {
        text = label; isAllCaps = false; textSize = 13f; setTextColor(colors.accent)
        minWidth = dp(48); minimumWidth = dp(48); minHeight = dp(48)
        background = rounded(colors.surface); setPadding(dp(10),0,dp(10),0)
        contentDescription = label; setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-2,dp(48)).apply { setMargins(dp(3),dp(3),dp(3),dp(3)) }
    }
    fun rounded(color: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(12).toFloat() }
    fun field(hint: String, value: String = "") = EditText(context).apply {
        this.hint = hint; setText(value); setTextColor(colors.ink); setHintTextColor(colors.muted)
        setSingleLine(true); minHeight = dp(48); setPadding(dp(16),dp(8),dp(16),dp(8))
    }
    fun scroll(view: android.view.View) = ScrollView(context).apply { addView(view); isFillViewport = true }
}
