package app.docreader.ui

import android.view.View
import android.widget.*
import app.docreader.MainActivity
import app.docreader.render.Palette

class WorkspaceView(private val activity: MainActivity, private val model: ReaderModel) {
    private var signature = ""
    private var panes = listOf<ReaderPane>()
    private var bars: View? = null
    fun render(state: ReaderState) {
        val key = listOf(state.home,state.left,state.right,state.active,state.settings,state.importing,
            state.tabs.map { it.id },state.library.map { it.id }).toString()
        if (signature != key) {
            signature = key; panes.forEach(ReaderPane::dispose)
            val ui = UiKit(activity,Palette.forTheme(activity,state.settings.theme))
            activity.setTheme(if (ui.colors.paper == 0xff151b24.toInt()) app.docreader.R.style.ReaderDark else app.docreader.R.style.ReaderLight)
            val root = ui.column().apply { setBackgroundColor(ui.colors.paper) }
            val header = ui.column(); bars = header
            if (state.importing) header.addView(ui.text("Importing document…",13f))
            val tools = ui.row()
            tools.addView(ui.button("Library") { model.update { it.copy(home=true) }; model.workspace.save() })
            tools.addView(ui.button("Open",activity::pick))
            tools.addView(ui.button("Split",model.workspace::split))
            tools.addView(ui.button("Reading") { ReaderMenus(activity,model,ui).main() })
            header.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled=false; addView(tools) })
            val tabs = ui.row()
            state.tabs.forEach { tab ->
                tabs.addView(ui.button((if (tab.id == state.active) "● " else "")+tab.document.title.take(25)) { model.workspace.select(tab.id) })
                tabs.addView(ui.button("×") { model.workspace.close(tab.id) }.apply { contentDescription="Close ${tab.document.title}" })
            }
            if (state.tabs.isNotEmpty()) header.addView(HorizontalScrollView(activity).apply { addView(tabs) })
            root.addView(header)
            if (state.home) { panes=emptyList(); root.addView(LibraryView.create(ui,model,activity::pick),LinearLayout.LayoutParams(-1,0,1f)) }
            else {
                val content = ui.row()
                panes = listOfNotNull(state.left,state.right).distinct().map { id ->
                    ReaderPane(model,ui,id) { header.visibility=if (header.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
                        .also { content.addView(it,LinearLayout.LayoutParams(0,-1,1f)) }
                }
                root.addView(content,LinearLayout.LayoutParams(-1,0,1f))
            }
            activity.install(root)
        }
        panes.forEach { it.render(state) }
    }
    fun dispose() { panes.forEach(ReaderPane::dispose) }
}
