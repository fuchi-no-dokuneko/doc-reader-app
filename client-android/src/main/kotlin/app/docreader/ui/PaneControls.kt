package app.docreader.ui

import android.view.View
import android.widget.LinearLayout
import android.widget.HorizontalScrollView

class PaneControls(ui: UiKit, model: ReaderModel, id: String, move: (Int) -> Unit) {
    private val row=ui.row()
    val view=HorizontalScrollView(ui.context).apply { isHorizontalScrollBarEnabled=false; addView(row) }
    val label=ui.button("Page") { PageDialogs(ui,model,id).jump() }
    init {
        row.addView(ui.button("−K") { move(-model.state.value.settings.jump) })
        row.addView(ui.button("‹") { move(-1) }.apply { contentDescription="Previous page" })
        label.minWidth=ui.dp(90); row.addView(label)
        row.addView(ui.button("›") { move(1) }.apply { contentDescription="Next page" })
        row.addView(ui.button("+K") { move(model.state.value.settings.jump) })
    }
    fun toggle() { view.visibility=if (view.visibility==View.VISIBLE) View.GONE else View.VISIBLE }
}
