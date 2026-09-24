package app.docreader.ui

import android.graphics.*
import android.view.*
import app.docreader.domain.Mark
import app.docreader.pdf.*

class PdfPageView(private val pane: ReaderPane, private val number: Int, private var bitmap: Bitmap,
    private val data: PdfPageData, marks: List<Mark>) : View(pane.context) {
    val selection=PdfSelection(pane,number,data)
    private val paint=Paint(3)
    private val destination=RectF(0f,0f,data.width.toFloat(),data.height.toFloat())
    private var renderJob: kotlinx.coroutines.Job?=null
    var zoom=1f; var dx=0f; var dy=0f
    private val highlights=marks.filter { it.page==number && it.bounds.isNotEmpty() }.flatMap {
        it.bounds.split(';').mapNotNull { rect -> rect.split(',').mapNotNull(String::toFloatOrNull).takeIf { it.size==4 }?.let { PdfBox(it[0],it[1],it[2],it[3]) } }
    }
    private fun scale()=minOf(width.toFloat()/data.width,height.toFloat()/data.height)*zoom
    private fun x()=(width-data.width*scale())/2+dx
    private fun y()=(height-data.height*scale())/2+dy
    fun point(e: MotionEvent)=((e.x-x())/scale()) to ((e.y-y())/scale())
    private val gestures=PdfGestures(this,pane,data)
    override fun onDraw(canvas: Canvas) {
        canvas.save(); canvas.translate(x(),y()); canvas.scale(scale(),scale())
        paint.color=Color.WHITE; canvas.drawBitmap(bitmap,null,destination,paint)
        paint.color=0x66ffc857
        (highlights+selection.boxes()).forEach { canvas.drawRect(it.left,it.top,it.right,it.bottom,paint) }
        canvas.restore()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (zoom>1f || event.pointerCount>1) parent.requestDisallowInterceptTouchEvent(true)
        gestures.pinch.onTouchEvent(event); gestures.gesture.onTouchEvent(event)
        if (event.actionMasked==MotionEvent.ACTION_UP) { if (selection.start>=0) selection.actions(this); performClick() }
        return true
    }
    fun sharpen() {
        renderJob?.cancel()
        renderJob=PdfZoom.render(pane,number,width,zoom) { bitmap=it; invalidate() }
    }
    override fun onDetachedFromWindow() { renderJob?.cancel(); super.onDetachedFromWindow() }
    override fun performClick(): Boolean { super.performClick(); return true }
}
