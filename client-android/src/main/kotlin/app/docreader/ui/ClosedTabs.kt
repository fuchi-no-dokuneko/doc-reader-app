package app.docreader.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

internal object ClosedTabs {
    fun close(model: ReaderModel,id: String): Job {
        val runtime=model.engines.remove(id)
        model.update { s ->
            val tabs=s.tabs.filterNot { it.id==id }; val next=tabs.firstOrNull()?.id
            s.copy(tabs=tabs,left=if (s.left==id) s.right ?: next else s.left,
                right=if (s.right==id || s.left==id) null else s.right,
                active=if (s.active==id) next else s.active,home=tabs.isEmpty())
        }
        model.workspace.save()
        return model.scope.launch(Dispatchers.IO) {
            runtime?.let { r ->
                r.job?.cancelAndJoin(); r.searchJob?.cancelAndJoin(); r.mutex.withLock { r.close() }
            }
        }
    }
}
