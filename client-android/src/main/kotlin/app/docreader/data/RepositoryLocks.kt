package app.docreader.data

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex

internal object RepositoryLocks {
    private val locks = ConcurrentHashMap<String,Mutex>()
    fun forDirectory(directory: File): Mutex = locks.getOrPut(directory.canonicalPath) { Mutex() }
}
