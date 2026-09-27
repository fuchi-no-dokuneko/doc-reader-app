package app.docreader.ui

import android.widget.*
import app.docreader.MainActivity
import app.docreader.domain.EditableFiles

object WorkspaceHeader {
    fun create(activity: MainActivity,model: ReaderModel,ui: UiKit,state: ReaderState): LinearLayout {
        val header=ui.column()
        if (state.importing) header.addView(ui.text("Importing document…",13f))
        val tools=ui.row()
        tools.addView(ui.button("Library") { model.update { it.copy(home=true) }; model.workspace.save() })
        tools.addView(ui.button("Open",activity::pick))
        tools.addView(ui.button("Split",model.workspace::split))
        tools.addView(ui.button("Reading") { ReaderMenus(activity,model,ui).main() })
        model.tab(state.active.orEmpty())?.takeIf { !state.home && EditableFiles.supports(it.document.kind) }?.let { tab ->
            tools.addView(ui.button("Edit source") { model.editing.start(tab.id) })
        }
        header.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled=false; addView(tools) })
        val tabs=ui.row()
        state.tabs.forEach { tab ->
            tabs.addView(ui.button((if (tab.id==state.active) "● " else "")+tab.document.title.take(25)) { model.workspace.select(tab.id) })
            tabs.addView(ui.button("×") { model.workspace.close(tab.id) }.apply { contentDescription="Close ${tab.document.title}" })
        }
        if (state.tabs.isNotEmpty()) header.addView(HorizontalScrollView(activity).apply { addView(tabs) })
        return header
    }
}
