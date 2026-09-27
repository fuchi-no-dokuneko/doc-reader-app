package app.docreader.ui

import android.view.View
import android.widget.*
import app.docreader.MainActivity
import app.docreader.render.Palette

class WorkspaceView(private val activity: MainActivity, private val model: ReaderModel) {
    private var signature = ""
    private var panes = listOf<ReaderPane>()
    private var bars: View? = null
    private var editor: EditorView? = null
    fun render(state: ReaderState) {
        state.editor?.let { session ->
            if (editor?.session !== session) {
                dispose(); panes=emptyList(); signature=""
                editor=EditorView(activity,model,session).also { activity.install(it.view) }
            }
            editor?.render(); return
        }
        if (editor!=null) { editor?.dispose(); editor=null; signature="" }
        val key = listOf(state.home,state.left,state.right,state.active,state.settings,state.importing,
            state.tabs.map { it.id+it.document.id },state.library.map { it.id }).toString()
        if (signature != key) {
            signature = key; panes.forEach(ReaderPane::dispose)
            val ui = UiKit(activity,Palette.forTheme(activity,state.settings.theme))
            activity.setTheme(if (ui.colors.paper == 0xff151b24.toInt()) app.docreader.R.style.ReaderDark else app.docreader.R.style.ReaderLight)
            val root = ui.column().apply { setBackgroundColor(ui.colors.paper) }
            val header=WorkspaceHeader.create(activity,model,ui,state); bars=header
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
    fun dispose() { panes.forEach(ReaderPane::dispose); editor?.dispose() }
}
