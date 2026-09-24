package app.docreader.ui

import android.content.Context
import android.view.*
import android.widget.FrameLayout

class PageSurface(context: Context, private val tap: (Float) -> Unit) : FrameLayout(context) {
    private var startX = 0f; private var startY = 0f; private var startTime = 0L
    private var moved = false
    var consumed = false
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            startX=event.x; startY=event.y; startTime=event.eventTime; moved=false; consumed=false
        }
        if (event.pointerCount > 1 || kotlin.math.abs(event.x-startX)+kotlin.math.abs(event.y-startY)>16*resources.displayMetrics.density) moved=true
        val result = super.dispatchTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP && !moved && !consumed && event.eventTime-startTime<300) {
            tap(event.x/width.coerceAtLeast(1)); performClick(); return true
        }
        return result || true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    companion object {
        fun consume(view: View) {
            var parent = view.parent
            while (parent != null) { if (parent is PageSurface) { parent.consumed=true; return }; parent=parent.parent }
        }
    }
}
