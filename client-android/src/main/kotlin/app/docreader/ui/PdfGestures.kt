package app.docreader.ui

import android.view.*
import app.docreader.pdf.*

class PdfGestures(private val view: PdfPageView, private val pane: ReaderPane, private val data: PdfPageData) {
    val pinch=ScaleGestureDetector(pane.context,object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleEnd(detector: ScaleGestureDetector) { view.sharpen() }
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            view.zoom=(view.zoom*detector.scaleFactor).coerceIn(1f,5f); view.invalidate(); return true
        }
    })
    val gesture=GestureDetector(pane.context,object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent)=true
        override fun onDoubleTap(e: MotionEvent): Boolean { view.zoom=if (view.zoom>1) 1f else 2f; view.dx=0f; view.dy=0f; view.invalidate(); view.sharpen(); return true }
        override fun onLongPress(e: MotionEvent) {
            PageSurface.consume(view)
            val (x,y)=view.point(e)
            val link=data.links.firstOrNull { it.page>=0 && it.bounds.any { b -> b.contains(x,y) } }
            if (link!=null) PdfPreview.show(pane,link.page)
            else { view.selection.start=view.selection.hit(x,y); view.selection.end=view.selection.start; view.parent.requestDisallowInterceptTouchEvent(true); view.invalidate() }
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val (x,y)=view.point(e); val link=data.links.firstOrNull { it.bounds.any { b -> b.contains(x,y) } } ?: return false
            PageSurface.consume(view)
            if (link.page>=0) pane.model.reading.move(pane.id,link.page) else ReaderLinks.open(pane,link.url)
            return true
        }
        override fun onScroll(a: MotionEvent?, b: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (view.selection.start>=0) { val (x,y)=view.point(b); view.selection.hit(x,y).takeIf { it>=0 }?.let { view.selection.end=it }; view.invalidate(); return true }
            if (view.zoom>1f) { view.dx-=distanceX; view.dy-=distanceY; view.parent.requestDisallowInterceptTouchEvent(true); view.invalidate(); return true }; return false
        }
    })
}
