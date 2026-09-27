package app.docreader.ui

import android.app.AlertDialog
import android.net.Uri
import app.docreader.MainActivity
import app.docreader.data.SourceWrite

class EditorActions(private val activity: MainActivity,private val model: ReaderModel) {
    fun save(copy: Boolean=false,finish: Boolean=false) {
        val session=model.state.value.editor ?: return
        if (model.state.value.editBusy.isNotEmpty()) return
        if (!session.dirty && !copy) { if (finish) model.editing.close(); return }
        if (copy || !SourceWrite.canWrite(activity,session.document.source)) activity.exportDraft(finish)
        else model.editing.save(Uri.parse(session.document.source),false,finish)
    }
    fun close() {
        val session=model.state.value.editor ?: return
        if (model.state.value.editBusy.isNotEmpty()) return
        if (!session.dirty) { model.editing.close(); return }
        AlertDialog.Builder(activity).setTitle("Save changes?")
            .setMessage("${session.document.title} has unsaved changes.")
            .setPositiveButton("Save") { _,_ -> save(finish=true) }
            .setNeutralButton("Discard") { _,_ -> model.editing.close() }
            .setNegativeButton("Keep editing",null).show()
    }
}
