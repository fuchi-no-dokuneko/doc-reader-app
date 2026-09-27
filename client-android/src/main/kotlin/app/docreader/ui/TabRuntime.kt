package app.docreader.ui

import app.docreader.domain.LayoutSpec
import app.docreader.render.TextBook
import app.docreader.render.PdfBook
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex

class TabRuntime : AutoCloseable {
    var text: TextBook? = null
    var pdf: PdfBook? = null
    var job: Job? = null
    var searchJob: Job? = null
    var layout: LayoutSpec? = null
    val mutex = Mutex()
    var generation = 0
    override fun close() { text?.close(); pdf?.close(); text = null; pdf = null }
}
