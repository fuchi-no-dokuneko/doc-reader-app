package app.docreader.ui

import android.graphics.Bitmap
import android.widget.*
import app.docreader.domain.*
import app.docreader.pdf.PdfPageData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

class LoadedPage(private val pane: ReaderPane, private val number: Int) : FrameLayout(pane.context) {
    private var job: Job?=null
    private val webViews=mutableListOf<android.webkit.WebView>()
    init {
        addView(pane.ui.text("Preparing page ${number+1}…",14f))
        job=pane.scope.launch {
            val runtime=pane.model.engines[pane.id] ?: return@launch
            val spec=runtime.layout ?: return@launch
            try {
                val document=pane.model.tab(pane.id)!!.document.id
                var text: List<Pair<PageSlice,Block>> = emptyList()
                var bitmap: Bitmap?=null; var pdf: PdfPageData?=null
                val images=mutableMapOf<String,Bitmap?>()
                val heights=mutableMapOf<Int,Int>()
                withContext(Dispatchers.IO) { runtime.mutex.withLock { runInterruptible {
                    runtime.text?.let { book ->
                        text=book.page(number)
                        val measure=app.docreader.render.TextMeasure(spec,pane.model.assets)
                        text.filter { it.second.type in setOf(BlockType.IMAGE,BlockType.TABLE) }.forEach {
                            heights[it.first.block]=measure.measure(it.second).lines.sumOf { line -> line.height }
                        }
                        text.filter { it.second.type==BlockType.IMAGE }.forEach {
                            images[it.second.asset]=pane.model.assets.bitmap(it.second.asset,spec.width,spec.height)
                        }
                    }
                    runtime.pdf?.let { book -> bitmap=book.bitmap(number,spec.width); pdf=book.data(number) }
                } } }
                val start=(text.firstOrNull()?.first?.block ?: 0).toLong() shl 32
                val end=(text.lastOrNull()?.first?.block?.plus(1) ?: 0).toLong() shl 32
                val marks=pane.model.repo.pageMarks(document,number,start,end)
                ensureActive(); removeAllViews()
                addView(PageAssembly.create(pane,number,spec,text,images,marks,webViews,heights,bitmap,pdf),LayoutParams(-1,-1))
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { removeAllViews(); addView(pane.ui.text(e.message ?: "Could not render page")) }
        }
    }
    fun dispose() { job?.cancel(); webViews.forEach { it.stopLoading(); it.destroy() }; webViews.clear() }
}
