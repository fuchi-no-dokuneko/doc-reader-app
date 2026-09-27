package app.docreader.ui

import android.content.Context
import android.view.View
import android.widget.*
import app.docreader.domain.Mode

class PageWindow(context: Context, private val changed: (Int,Int) -> Unit) : ScrollView(context) {
    private val column = LinearLayout(context).apply { orientation=LinearLayout.VERTICAL }
    var first = 0
        private set
    private var pageHeight = 1
    private var count = 0
    private var restoring = false
    private var mode = Mode.PAGED
    private var center = 0
    var needWindow: (Int,Int) -> Unit = { _,_ -> }
    init {
        addView(column); isVerticalScrollBarEnabled=false; overScrollMode=OVER_SCROLL_NEVER
        setOnScrollChangeListener { _: View, _: Int, y: Int, _: Int, _: Int ->
            if (!restoring && mode == Mode.SCROLL && count > 0) {
                val page=(first+y/pageHeight).coerceAtMost(count-1); val offset=y%pageHeight
                changed(page,offset)
                if (kotlin.math.abs(page-center)>=2) needWindow(page,offset)
            }
        }
    }
    fun show(page: Int, offset: Int, total: Int, height: Int, mode: Mode, create: (Int) -> View) {
        restoring=true; this.mode=mode; count=total; pageHeight=height.coerceAtLeast(1); center=page
        first=if (mode == Mode.SCROLL) (page-2).coerceAtLeast(0) else page
        val last=if (mode == Mode.SCROLL) (page+2).coerceAtMost(total-1) else page
        disposeChildren(); column.removeAllViews()
        for (number in first..last) column.addView(create(number),LinearLayout.LayoutParams(-1,pageHeight))
        post { scrollTo(0,(page-first)*pageHeight+if (mode == Mode.SCROLL) offset else 0); restoring=false }
    }
    fun disposeChildren() {
        for (i in 0 until column.childCount) (column.getChildAt(i) as? LoadedPage)?.dispose()
    }
}
