package app.docreader.ui

import app.docreader.data.LocalRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object ModelCleanup {
    fun close(engines: List<TabRuntime>,tabs: List<ReaderTab>,repo: LocalRepository) {
        engines.forEach { it.job?.cancel(); it.searchJob?.cancel() }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                engines.forEach { it.job?.join(); it.searchJob?.join(); it.mutex.withLock { it.close() } }
                tabs.forEach { repo.savePosition(it.document.id,it.id,it.position) }
            } finally { repo.close() }
        }
    }
}
