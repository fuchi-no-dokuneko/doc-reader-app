package app.docreader.ui

import app.docreader.domain.DocumentInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object TabRefresh {
    suspend fun apply(model: ReaderModel,document: DocumentInfo,onlyTab: String? = null): DocumentInfo =
        model.tabUpdates.withLock {
            val doc=model.repo.document(document.id) ?: document
            val tabs=model.state.value.tabs.filter {
                if (onlyTab!=null) it.id==onlyTab else it.document.id==doc.id
            }
            for (tab in tabs) {
                model.engines[tab.id]?.let { runtime ->
                    runtime.job?.cancelAndJoin(); runtime.searchJob?.cancelAndJoin()
                    withContext(Dispatchers.IO) { runtime.mutex.withLock { runtime.close() } }
                }
                val position=model.tab(tab.id)?.position ?: continue
                model.repo.savePosition(doc.id,tab.id,position)
                model.change(tab.id) { it.copy(document=doc,count=0,error="",busy="Opening…") }
            }
            model.refresh(); model.workspace.save()
            tabs.forEach { model.loading.start(it.id) }
            doc
        }
}
