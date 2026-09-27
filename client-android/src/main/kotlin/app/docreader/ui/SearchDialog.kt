package app.docreader.ui

import android.app.AlertDialog

object SearchDialog {
    fun show(ui: UiKit, model: ReaderModel, id: String) {
        val column=ui.column(); val input=ui.field("Text to find"); val result=ui.text("Search this document",13f)
        column.addView(input); column.addView(result)
        var job: kotlinx.coroutines.Job?=null
        var after=0L; var last=""; var busy=false
        val dialog=AlertDialog.Builder(ui.context).setTitle("Find text").setView(column)
            .setPositiveButton("Find next",null).setNeutralButton("From beginning",null).setNegativeButton("Close",null).create()
        fun find(reset: Boolean) {
            val query=input.text.toString(); if (query.isBlank() || busy) return
            if (query!=last || reset) after=0; last=query; busy=true; result.text="Searching…"
            job=model.reading.search(id,query,after) { anchor,text ->
                busy=false
                if (!dialog.isShowing) return@search
                if (anchor!=null) {
                    model.reading.jumpAnchor(id,anchor); after=anchor+1
                    val start=text.indexOf(query,ignoreCase=true).coerceAtLeast(0)
                    result.text="Page ${(model.tab(id)?.position?.page ?: 0)+1}\n"+text.substring((start-30).coerceAtLeast(0),(start+query.length+100).coerceAtMost(text.length))
                } else result.text=text
            }
        }
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { find(false) }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { find(true) }
        }; dialog.setOnDismissListener { job?.cancel() }; dialog.show()
    }
}
