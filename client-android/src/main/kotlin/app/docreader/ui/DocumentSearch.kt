package app.docreader.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object DocumentSearch {
    suspend fun find(runtime: TabRuntime,query: String,after: Long): Pair<Long,String>? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null
        val pdf=runtime.pdf; val text=runtime.text
        if (pdf!=null) {
            for (page in after.toInt().coerceAtLeast(0) until pdf.count) {
                ensureActive()
                val content=runtime.mutex.withLock { runInterruptible { pdf.data(page).text.joinToString("\n") { it.text } } }
                if (content.contains(query,true)) return@withContext page.toLong() to content
                yield()
            }
        } else if (text!=null) {
            val first=(after shr 32).toInt().coerceAtLeast(0)
            for (index in first until text.blocks.count) {
                ensureActive()
                val content=runtime.mutex.withLock { text.blocks.block(index).text }
                val start=if (index==first) (after and 0xffffffffL).toInt() else 0
                val position=content.indexOf(query,start,true)
                if (position>=0) return@withContext ((index.toLong() shl 32) or position.toLong()) to content
                if (index%32==0) yield()
            }
        }
        null
    }
}
