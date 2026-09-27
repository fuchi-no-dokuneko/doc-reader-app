package app.docreader.ui

import app.docreader.domain.DocumentInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object EditedTabs {
    suspend fun apply(model: ReaderModel,session: EditSession,doc: DocumentInfo,copy: Boolean) {
        val old=session.document.id
        val tabs=model.state.value.tabs.filter { if (copy) it.id==session.tab else it.document.id==old }
        for (tab in tabs) {
            model.engines[tab.id]?.let { runtime ->
                runtime.job?.cancelAndJoin(); runtime.searchJob?.cancelAndJoin()
                withContext(Dispatchers.IO) { runtime.mutex.withLock { runtime.close() } }
            }
            model.repo.savePosition(doc.id,tab.id,tab.position)
            model.change(tab.id) { it.copy(document=doc,count=0,error="",busy="Opening saved file…") }
        }
        model.preferences.encoding(doc.id,session.source.encoding)
        if (!copy && old!=doc.id) {
            model.repo.marks(old).forEach { model.repo.addMark(it.copy(id=0,document=doc.id)) }
            withContext(Dispatchers.IO) { model.repo.remove(old) }
        }
        model.refresh(); model.workspace.save()
        tabs.forEach { model.loading.start(it.id) }
    }
}
