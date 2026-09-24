package app.docreader.ui

import app.docreader.domain.*
import app.docreader.data.BlockIndex
import app.docreader.render.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import java.io.File

class BookLoading(private val model: ReaderModel) {
    fun start(id: String, password: String? = null) {
        val tab = model.tab(id) ?: return
        val runtime = model.engines[id] ?: return
        val previous=runtime.job
        runtime.job = model.scope.launch {
            previous?.cancelAndJoin()
            runtime.searchJob?.cancelAndJoin()
            model.change(id) { it.copy(busy="Opening…",error="",password=false) }
            try {
                val position = model.repo.position(tab.document.id,id)
                val encoding = model.preferences.encoding(tab.document.id).first()
                withContext(Dispatchers.IO) { runtime.mutex.withLock {
                    runInterruptible {
                        runtime.close()
                        val file = model.repo.file(tab.document.id)
                        if (tab.document.kind == Kind.PDF) runtime.pdf = PdfBook(file,password)
                        else {
                            val cache = model.repo.cache(tab.document.id)
                            val (blocks,detected) = BlockIndex.open(file,tab.document,encoding,cache) {
                                model.assets.resolve(tab.document.source,it)
                            }
                            runtime.text = TextBook(blocks,detected,File(cache,"layout-$id"),model.assets)
                        }
                    }
                } }
                model.change(id) { it.copy(position=position,encoding=runtime.text?.encoding ?: "PDF",
                    count=runtime.pdf?.count ?: 0,busy=if (runtime.text != null) "Preparing pages…" else "") }
                runtime.layout?.let { layout(id,it,true) }
            } catch (e: CancellationException) { throw e }
            catch (e: SecurityException) { model.change(id) { it.copy(busy="",password=true,error="Password required or incorrect. Tap here to unlock.") } }
            catch (e: Exception) { model.change(id) { it.copy(busy="",error=e.message ?: "Unable to read document") } }
        }
    }
    fun restart(id: String) {
        val runtime = model.engines[id] ?: return
        model.scope.launch {
            runtime.job?.cancelAndJoin()
            withContext(Dispatchers.IO) { runtime.mutex.withLock { runtime.close() } }
            start(id)
        }
    }
    fun layout(id: String, spec: LayoutSpec, force: Boolean = false) = BookLayout.apply(model,id,spec,force)
    fun reflow() {
        model.engines.toMap().forEach { (id,runtime) ->
            runtime.layout?.let { layout(id,it.copy(settings=model.state.value.settings),true) }
        }
    }
}
