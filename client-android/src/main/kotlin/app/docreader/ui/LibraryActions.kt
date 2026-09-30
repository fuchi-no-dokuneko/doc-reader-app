package app.docreader.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object LibraryActions {
    fun remove(model: ReaderModel,id: String) = model.scope.launch {
        model.tabUpdates.withLock {
            model.state.value.tabs.filter { it.document.id==id }
                .map { model.workspace.close(it.id) }.joinAll()
            withContext(Dispatchers.IO) { model.repo.remove(id) }
            model.refresh()
        }
    }
}
