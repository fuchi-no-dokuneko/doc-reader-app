package app.docreader.ui

import android.app.AlertDialog
import android.widget.ImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

object PdfPreview {
    fun show(pane: ReaderPane, page: Int) {
        val runtime=pane.model.engines[pane.id] ?: return
        val book=runtime.pdf ?: return
        if (page !in 0 until book.count) return
        val image=ImageView(pane.context).apply { adjustViewBounds=true; contentDescription="Preview of page ${page+1}" }
        val dialog=AlertDialog.Builder(pane.context).setTitle("Page ${page+1} preview").setView(image)
            .setPositiveButton("Go to page") { _,_ -> pane.model.reading.move(pane.id,page) }
            .setNegativeButton("Stay here",null).show()
        val job=pane.scope.launch {
            val bitmap=withContext(Dispatchers.IO) { runtime.mutex.withLock { book.bitmap(page,600) } }
            if (dialog.isShowing) image.setImageBitmap(bitmap)
        }
        dialog.setOnDismissListener { job.cancel() }
    }
}
