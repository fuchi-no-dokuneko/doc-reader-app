package app.docreader.ui

import android.graphics.Bitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object PdfZoom {
    fun render(pane: ReaderPane,page: Int,width: Int,zoom: Float,done: (Bitmap) -> Unit)=pane.scope.launch {
        val runtime=pane.model.engines[pane.id] ?: return@launch
        val book=runtime.pdf ?: return@launch
        try {
            val bitmap=withContext(Dispatchers.IO) { runtime.mutex.withLock { runInterruptible { book.bitmap(page,width,zoom) } } }
            done(bitmap)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { pane.model.message(e.message ?: "Could not increase zoom detail") }
    }
}
