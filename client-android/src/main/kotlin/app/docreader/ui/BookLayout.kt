package app.docreader.ui

import app.docreader.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object BookLayout {
    fun apply(model: ReaderModel, id: String, spec: LayoutSpec, force: Boolean) {
        val runtime = model.engines[id] ?: return
        if (!force && runtime.layout == spec) return
        runtime.layout = spec
        val book = runtime.text
        if (book==null) {
            if (runtime.pdf!=null) model.change(id) { it.copy(revision=it.revision+1) }
            return
        }
        val old = runtime.job
        val generation = ++runtime.generation
        val tab = model.tab(id) ?: return
        runtime.job = model.scope.launch {
            if (old != currentCoroutineContext()[Job]) old?.cancelAndJoin()
            model.change(id) { it.copy(busy="Laying out pages…") }
            try {
                withContext(Dispatchers.IO) { runtime.mutex.withLock {
                    runInterruptible { book.paginate(spec) }
                } }
                if (generation != runtime.generation) return@launch
                val pages = book.pages ?: return@launch
                val page = if (tab.position.anchor > 0) pages.find(tab.position.anchor)
                    else tab.position.page.coerceIn(0,pages.count-1)
                model.change(id) { it.copy(busy="",count=pages.count,position=it.position.copy(page=page),revision=it.revision+1) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { model.change(id) { it.copy(busy="",error=e.message ?: "Pagination failed") } }
        }
    }
}
