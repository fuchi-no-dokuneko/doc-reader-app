package app.docreader.ui

import android.app.AlertDialog
import app.docreader.MainActivity
import app.docreader.domain.*
import app.docreader.format.Encodings

class ReaderMenus(private val activity: MainActivity, private val model: ReaderModel, private val ui: UiKit) {
    fun main() {
        val id=model.state.value.active
        val items=arrayOf("Reading mode","Appearance & spacing","Find text","Go to page","Chapters",
            "Add bookmark","Bookmarks & highlights","Text encoding","Open another tab","Split / single pane","Linked images")
        AlertDialog.Builder(activity).setTitle("Reading").setItems(items) { _,which ->
            when(which) {
                0 -> mode()
                1 -> AppearanceDialog.show(ui,model)
                10 -> activity.pickFolder()
                else -> if (id!=null) when(which) {
                    2 -> SearchDialog.show(ui,model,id)
                    3 -> PageDialogs(ui,model,id).jump()
                    4 -> NavigationDialogs.chapters(ui,model,id)
                    5 -> model.reading.bookmark(id)
                    6 -> NavigationDialogs.marks(ui,model,id)
                    7 -> encoding(id)
                    8 -> model.tab(id)?.let { model.workspace.open(it.document) }
                    9 -> model.workspace.split()
                } else model.message("Open a document first")
            }
        }.setNegativeButton("Close",null).show()
    }
    private fun mode() {
        val current=model.state.value.settings
        AlertDialog.Builder(activity).setTitle("Reading mode")
            .setSingleChoiceItems(arrayOf("Continuous scroll","Paged · tap thirds","Precision · page jumps"),current.mode.ordinal) { dialog,index ->
                model.settings(current.copy(mode=Mode.entries[index])); dialog.dismiss()
            }.setNegativeButton("Cancel",null).show()
    }
    private fun encoding(id: String) {
        val tab=model.tab(id) ?: return
        AlertDialog.Builder(activity).setTitle("Text encoding · detected ${tab.encoding}")
            .setItems(Encodings.choices.toTypedArray()) { _,which -> model.encoding(id,Encodings.choices[which]) }
            .setNegativeButton("Cancel",null).show()
    }
}
