package app.docreader.ui

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import app.docreader.domain.*
import app.docreader.pdf.PdfPageData

object PageAssembly {
    fun create(pane: ReaderPane, number: Int, spec: LayoutSpec, text: List<Pair<PageSlice,Block>>,
        images: Map<String,Bitmap?>, marks: List<Mark>, webViews: MutableList<WebView>, heights: Map<Int,Int>,
        bitmap: Bitmap?, pdf: PdfPageData?): View {
        val surface=PageSurface(pane.context,pane::tap)
        val column=pane.ui.column()
        column.addView(pane.ui.text(pane.model.tab(pane.id)?.document?.title.orEmpty(),11f).apply {
            maxLines=1; setPadding(12,0,12,0); gravity=android.view.Gravity.CENTER_VERTICAL
        },LinearLayout.LayoutParams(-1,(28*spec.density).toInt()))
        val body = if (pdf != null) PdfPageView(pane,number,bitmap!!,pdf,marks)
            else TextPageView.create(pane,spec,text,images,marks,webViews,heights)
        column.addView(body,LinearLayout.LayoutParams(-1,0,1f))
        column.addView(pane.ui.text("${number+1} / ${pane.model.tab(pane.id)?.count}",11f).apply {
            gravity=android.view.Gravity.CENTER; setPadding(0,0,0,0)
        },LinearLayout.LayoutParams(-1,(28*spec.density).toInt()))
        surface.addView(column,android.widget.FrameLayout.LayoutParams(-1,-1)); return surface
    }
}
