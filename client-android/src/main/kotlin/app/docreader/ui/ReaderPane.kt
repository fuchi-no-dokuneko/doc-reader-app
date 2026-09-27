package app.docreader.ui

import android.view.*
import android.widget.*
import app.docreader.domain.*
import kotlinx.coroutines.*

class ReaderPane(val model: ReaderModel, val ui: UiKit, val id: String,
    private val bars: () -> Unit) : LinearLayout(ui.context) {
    val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private val title=ui.text("",12f,true).apply { maxLines=1; ellipsize=android.text.TextUtils.TruncateAt.END }
    private val status=ui.text("",16f)
    private val stage=FrameLayout(context)
    private val window=PageWindow(context) { page,offset -> model.reading.move(id,page,offset,false) }
    private val rail=ProgressRail(context) { model.reading.move(id,it) }
    private val nav=PaneControls(ui,model,id,::move)
    private var version=-1
    init {
        orientation=VERTICAL; addView(title)
        stage.addView(window,FrameLayout.LayoutParams(-1,-1))
        stage.addView(rail,FrameLayout.LayoutParams(ui.dp(40),-1,Gravity.END))
        stage.addView(status,FrameLayout.LayoutParams(-1,-1)); addView(stage,LayoutParams(-1,0,1f))
        addView(nav.view)
        window.needWindow={ page,offset -> show(page,offset) }
        stage.addOnLayoutChangeListener { _,_,_,_,_,_,_,_,_ -> layoutBook() }
        title.setOnClickListener { model.workspace.select(id) }
    }
    private fun move(delta: Int) { model.tab(id)?.let { model.reading.move(id,it.position.page+delta) } }
    fun tap(fraction: Float) {
        if (model.state.value.settings.mode == Mode.SCROLL) return
        when { fraction<1f/3 -> move(-1); fraction>2f/3 -> move(1); else -> {
            nav.toggle(); bars()
        } }
    }
    private fun layoutBook() = PaneLayout.apply(this,stage,model,id)
    fun render(state: ReaderState) {
        val tab=model.tab(id) ?: return
        title.text=tab.document.title; nav.label.text="${tab.position.page+1} / ${tab.count}"
        title.setBackgroundColor(if (state.active==id) ui.colors.surface else ui.colors.paper)
        rail.count=tab.count.coerceAtLeast(1); rail.page=tab.position.page; rail.color=ui.colors.accent; rail.invalidate()
        rail.visibility=if (state.settings.mode==Mode.SCROLL) VISIBLE else GONE
        status.text=tab.error.ifEmpty { tab.busy }; status.visibility=if (status.text.isEmpty()) GONE else VISIBLE
        status.setOnClickListener { if (tab.password) PageDialogs(ui,model,id).password() }
        if (tab.busy.isEmpty() && tab.error.isEmpty() && tab.count>0 && version!=tab.revision) {
            version=tab.revision; show(tab.position.page,tab.position.offset)
        }
        layoutBook()
    }
    private fun show(page: Int, offset: Int) {
        val tab=model.tab(id) ?: return
        if (stage.height<120) return
        window.show(page,offset,tab.count,stage.height,model.state.value.settings.mode) { LoadedPage(this,it) }
    }
    fun dispose() { window.disposeChildren(); scope.cancel(); model.engines[id]?.pdf?.cache?.clear() }
}
