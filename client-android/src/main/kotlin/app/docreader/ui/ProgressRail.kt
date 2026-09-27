package app.docreader.ui

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.roundToInt

class ProgressRail(context: Context, private val jump: (Int) -> Unit) : View(context) {
    var count=1; var page=0
    var color=0xff305fba.toInt()
    private val paint=Paint(3)
    private var dragging=false
    init { contentDescription="Drag to jump through document"; isFocusable=true }
    override fun onDraw(canvas: Canvas) {
        val x=width/2f; paint.color=color; paint.alpha=60; paint.strokeWidth=3*resources.displayMetrics.density
        canvas.drawLine(x,0f,x,height.toFloat(),paint); paint.alpha=255
        val y=12f+(height-24)*page.toFloat()/(count-1).coerceAtLeast(1)
        canvas.drawRoundRect(x-6,y-16,x+6,y+16,6f,6f,paint)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { dragging=true; parent.requestDisallowInterceptTouchEvent(true) }
            MotionEvent.ACTION_UP -> { dragging=false; performClick() }
            MotionEvent.ACTION_CANCEL -> dragging=false
        }
        if (dragging || event.actionMasked == MotionEvent.ACTION_UP) {
            page=((event.y/height).coerceIn(0f,1f)*(count-1)).roundToInt(); invalidate()
            if (event.actionMasked == MotionEvent.ACTION_UP) jump(page)
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}
