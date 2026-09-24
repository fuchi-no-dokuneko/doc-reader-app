package app.docreader.ui

import app.docreader.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

class ReadingActions(private val model: ReaderModel) {
    fun move(id: String, page: Int, offset: Int = 0, redraw: Boolean = true) {
        val tab = model.tab(id) ?: return
        if (tab.count == 0 || tab.busy.isNotEmpty()) return
        val number = page.coerceIn(0,tab.count-1)
        val runtime = model.engines[id] ?: return
        val anchor = runtime.text?.pages?.page(number)?.anchor ?: number.toLong()
        val position = ReadingPosition(number,offset,anchor)
        model.change(id) { it.copy(position=position,revision=it.revision+if (redraw) 1 else 0) }
        model.scope.launch { model.repo.savePosition(tab.document.id,id,position) }
    }
    fun jumpAnchor(id: String, anchor: Long) {
        val runtime = model.engines[id] ?: return
        move(id,runtime.text?.pages?.find(anchor) ?: anchor.toInt())
    }
    fun bookmark(id: String) {
        val tab = model.tab(id) ?: return
        model.scope.launch {
            model.repo.addMark(Mark(document=tab.document.id,page=tab.position.page,anchor=tab.position.anchor))
            model.message("Bookmark saved")
        }
    }
    fun highlight(id: String, slice: PageSlice, start: Int, end: Int, text: String) {
        val tab = model.tab(id) ?: return
        model.scope.launch {
            model.repo.addMark(Mark(document=tab.document.id,page=tab.position.page,start=start,end=end,
                text=text,type="highlight",anchor=(slice.block.toLong() shl 32) or slice.start.toLong()))
            model.change(id) { it.copy(revision=it.revision+1) }
            model.message("Highlight saved")
        }
    }
    fun fold(id: String, block: Int) {
        val runtime = model.engines[id] ?: return
        val book = runtime.text ?: return
        if (!book.collapsed.add(block)) book.collapsed.remove(block)
        runtime.layout?.let { model.loading.layout(id,it,true) }
    }
    fun search(id: String, query: String, after: Long, result: (Long?,String) -> Unit): Job? {
        val runtime=model.engines[id] ?: return null
        runtime.searchJob?.cancel()
        return model.scope.launch {
            try {
                val found=DocumentSearch.find(runtime,query,after)
                result(found?.first,found?.second ?: "No more matches")
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { result(null,e.message ?: "Search failed") }
        }.also { runtime.searchJob=it }
    }
}
