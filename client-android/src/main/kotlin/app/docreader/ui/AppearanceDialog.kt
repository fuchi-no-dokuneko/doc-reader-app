package app.docreader.ui

import android.app.AlertDialog
import android.text.InputType
import android.widget.*
import app.docreader.domain.*

object AppearanceDialog {
    fun show(ui: UiKit, model: ReaderModel) {
        val value=model.state.value.settings; val column=ui.column()
        fun spinner(title: String, values: List<String>, selected: Int): Spinner {
            column.addView(ui.text(title,14f,true))
            return Spinner(ui.context).apply {
                adapter=ArrayAdapter(ui.context,android.R.layout.simple_spinner_dropdown_item,values)
                setSelection(selected); column.addView(this)
            }
        }
        fun field(title: String, initial: String): EditText {
            column.addView(ui.text(title,14f,true))
            return ui.field(title,initial).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; column.addView(this) }
        }
        val theme=spinner("Theme",listOf("System","Light","Dark","Sepia"),value.theme.ordinal)
        val fonts=listOf("serif","sans-serif","monospace")
        val font=spinner("Font",fonts,fonts.indexOf(value.font).coerceAtLeast(0))
        val size=field("Font size · 12–40",value.size.toString())
        val line=field("Line spacing · 1.0–2.5",value.lineHeight.toString())
        val margin=field("Page margin · 0–64",value.margin.toString())
        val jump=field("Pages per jump (K)",value.jump.toString())
        val dialog=AlertDialog.Builder(ui.context).setTitle("Make it comfortable")
            .setView(ui.scroll(column)).setPositiveButton("Apply",null).setNegativeButton("Cancel",null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val s=size.text.toString().toFloatOrNull(); val l=line.text.toString().toFloatOrNull()
            val m=margin.text.toString().toIntOrNull(); val k=jump.text.toString().toIntOrNull()
            if (s==null || s !in 12f..40f) { size.error="Use 12–40"; return@setOnClickListener }
            if (l==null || l !in 1f..2.5f) { line.error="Use 1.0–2.5"; return@setOnClickListener }
            if (m==null || m !in 0..64) { margin.error="Use 0–64"; return@setOnClickListener }
            if (k==null || k<1) { jump.error="Enter a positive number"; return@setOnClickListener }
            model.settings(value.copy(theme=Theme.entries[theme.selectedItemPosition],font=fonts[font.selectedItemPosition],size=s,lineHeight=l,margin=m,jump=k))
            dialog.dismiss()
        } }; dialog.show()
    }
}
