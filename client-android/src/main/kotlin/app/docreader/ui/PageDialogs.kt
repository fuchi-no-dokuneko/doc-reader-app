package app.docreader.ui

import android.app.AlertDialog
import android.text.InputType

class PageDialogs(private val ui: UiKit, private val model: ReaderModel, private val id: String) {
    fun jump() {
        val tab=model.tab(id) ?: return
        if (tab.count==0) return
        val field=ui.field("Page 1–${tab.count}",(tab.position.page+1).toString()).apply { inputType=InputType.TYPE_CLASS_NUMBER; selectAll() }
        val dialog=AlertDialog.Builder(ui.context).setTitle("Go to page").setView(field)
            .setPositiveButton("Go",null).setNegativeButton("Cancel",null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val number=field.text.toString().toIntOrNull()
            if (number==null || number !in 1..tab.count) field.error="Enter 1–${tab.count}"
            else { model.reading.move(id,number-1); dialog.dismiss() }
        } }; dialog.show()
    }
    fun password() {
        val field=ui.field("PDF password").apply { inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }
        AlertDialog.Builder(ui.context).setTitle("Unlock PDF").setView(field)
            .setPositiveButton("Open") { _,_ -> model.loading.start(id,field.text.toString()) }
            .setNegativeButton("Cancel",null).show()
    }
}
