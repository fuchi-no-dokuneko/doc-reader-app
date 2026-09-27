package app.docreader.render

import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import app.docreader.pdf.*

@androidx.annotation.RequiresApi(35)
object NativePdfText {
    fun data(page: PdfRenderer.Page): PdfPageData {
        fun box(rect: RectF) = PdfBox(rect.left,rect.top,rect.right,rect.bottom)
        val texts = page.textContents.map { PdfText(it.text,it.bounds.map(::box)) }
        val links = page.gotoLinks.map { PdfLink(it.bounds.map(::box),it.destination.pageNumber) }+
            page.linkContents.map { PdfLink(it.bounds.map(::box),url=it.uri.toString()) }
        return PdfPageData(page.width,page.height,texts,links)
    }
}
