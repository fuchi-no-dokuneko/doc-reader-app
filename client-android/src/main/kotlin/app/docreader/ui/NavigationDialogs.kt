package app.docreader.ui

import android.app.AlertDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object NavigationDialogs {
    fun chapters(ui: UiKit, model: ReaderModel, id: String) = model.scope.launch {
        val runtime=model.engines[id] ?: return@launch
        val items=withContext(Dispatchers.IO) { runtime.mutex.withLock {
            runtime.text?.blocks?.chapters?.map { ("  ".repeat((it.level-1).coerceIn(0,4))+it.title) to (it.block.toLong() shl 32) }
                ?: runtime.pdf?.chapters()?.map { ("  ".repeat(it.level.coerceIn(0,4))+it.title) to it.page.toLong() }.orEmpty()
        } }
        if (items.isEmpty()) { model.message("This document has no chapter headings"); return@launch }
        AlertDialog.Builder(ui.context).setTitle("Chapters").setItems(items.map { it.first }.toTypedArray()) { _,which ->
            model.reading.jumpAnchor(id,items[which].second)
        }.setNegativeButton("Close",null).show()
    }
    fun marks(ui: UiKit, model: ReaderModel, id: String) = model.scope.launch {
        val tab=model.tab(id) ?: return@launch
        val items=model.repo.marks(tab.document.id)
        if (items.isEmpty()) { model.message("No bookmarks or highlights yet. Select text to highlight it."); return@launch }
        AlertDialog.Builder(ui.context).setTitle("Bookmarks & highlights").setItems(items.map {
            "Page ${it.page+1} · "+if (it.type=="bookmark") "Bookmark" else it.text.take(80)
        }.toTypedArray()) { _,which ->
            val mark=items[which]
            AlertDialog.Builder(ui.context).setTitle("Page ${mark.page+1}").setMessage(mark.text)
                .setPositiveButton("Go") { _,_ -> model.reading.jumpAnchor(id,mark.anchor) }
                .setNeutralButton("Remove") { _,_ -> model.scope.launch {
                    model.repo.removeMark(mark.id); model.change(id) { it.copy(revision=it.revision+1) }
                } }.setNegativeButton("Close",null).show()
        }.setNegativeButton("Close",null).show()
    }
}
