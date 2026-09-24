package app.docreader.ui

import android.content.Intent
import android.net.Uri
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView
import kotlinx.coroutines.*

class ReaderLinks(private val pane: ReaderPane) : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val layout=widget.layout ?: return false
        val y=(event.y-widget.totalPaddingTop+widget.scrollY).toInt()
        val x=event.x-widget.totalPaddingLeft+widget.scrollX
        val offset=layout.getOffsetForHorizontal(layout.getLineForVertical(y),x)
        val link=buffer.getSpans(offset,offset,URLSpan::class.java).firstOrNull()
        if (link != null) {
            PageSurface.consume(widget)
            if (event.action==MotionEvent.ACTION_UP) open(pane,link.url)
            return true
        }
        return false
    }
    companion object {
        fun open(pane: ReaderPane, url: String) {
            val uri=Uri.parse(url)
            if (uri.scheme in setOf("https","http","mailto")) {
                runCatching { pane.context.startActivity(Intent(Intent.ACTION_VIEW,uri)) }
                    .onFailure { pane.model.message("No app can open this link") }; return
            }
            val book=pane.model.engines[pane.id]?.text ?: return
            pane.scope.launch {
                val target=withContext(Dispatchers.IO) {
                    (0 until book.blocks.count).firstOrNull { index ->
                        val anchor=book.blocks.block(index).anchor
                        anchor==url || anchor.endsWith(url) || url.startsWith('#') && anchor.substringAfter('#')==url.drop(1)
                    }
                }
                if (target != null) pane.model.reading.jumpAnchor(pane.id,target.toLong() shl 32)
                else pane.model.message("This link's target is not in the document")
            }
        }
    }
}
