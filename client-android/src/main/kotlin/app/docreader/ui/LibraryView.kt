package app.docreader.ui

import android.app.AlertDialog
import android.view.*
import android.widget.*
import app.docreader.domain.DocumentInfo
import kotlinx.coroutines.*

object LibraryView {
    fun create(ui: UiKit, model: ReaderModel, pick: () -> Unit): View {
        val column = ui.column()
        column.addView(ui.text("Your reading desk",28f,true))
        column.addView(ui.text("Books, documents, notebooks & code",16f))
        column.addView(ui.button("＋ Open a file",pick))
        column.addView(ui.text("PDF · EPUB · Office · Markdown · Jupyter\nSource code · YAML · JSON · configuration",13f))
        column.addView(ui.text("Recent documents",18f,true))
        val docs = model.state.value.library
        if (docs.isEmpty()) column.addView(ui.text("Choose a file to begin. Your place, bookmarks and reading settings are saved on this device."))
        val list = ListView(ui.context).apply {
            divider = null
            adapter = object : BaseAdapter() {
                override fun getCount() = docs.size
                override fun getItem(position: Int) = docs[position]
                override fun getItemId(position: Int) = position.toLong()
                override fun getView(position: Int, old: View?, parent: ViewGroup?): View {
                    val doc = docs[position]
                    return ui.column().apply {
                        setPadding(ui.dp(12),ui.dp(4),ui.dp(12),ui.dp(4))
                        addView(ui.text(doc.title,17f,true))
                        addView(ui.text("${doc.kind.name}  ·  ${android.text.format.Formatter.formatShortFileSize(ui.context,doc.size)}",12f))
                    }
                }
            }
            setOnItemClickListener { _,_,position,_ -> model.workspace.open(docs[position]) }
            setOnItemLongClickListener { _,_,position,_ -> menu(ui,model,docs[position]); true }
        }
        column.addView(list,LinearLayout.LayoutParams(-1,0,1f)); return column
    }
    private fun menu(ui: UiKit, model: ReaderModel, doc: DocumentInfo) {
        AlertDialog.Builder(ui.context).setTitle(doc.title)
            .setItems(arrayOf("Open in a new tab","Remove from library")) { _,which ->
                if (which == 0) model.workspace.open(doc)
                else model.scope.launch {
                    model.state.value.tabs.filter { it.document.id == doc.id }.forEach { model.workspace.close(it.id) }
                    withContext(Dispatchers.IO) { model.repo.remove(doc.id) }; model.refresh()
                }
            }.setNegativeButton("Cancel",null).show()
    }
}
