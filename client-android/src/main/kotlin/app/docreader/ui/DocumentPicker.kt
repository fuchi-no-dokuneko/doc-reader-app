package app.docreader.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import app.docreader.MainActivity

class DocumentPicker(private val activity: MainActivity) {
    private val folder=activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        if (it!=null) FolderAccess.accept(activity,it)
    }
    private val picker=activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode==Activity.RESULT_OK) it.data?.let { data ->
            data.data?.let { uri -> activity.model.importFile(uri,data.flags) }
        }
    }
    private val export=activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val model=activity.model
        val pending=model.editing.exportPending
        model.editing.exportPending=false; model.update { it.copy(editBusy="") }
        if (pending && uri!=null) model.editing.save(uri,true,model.editing.finishAfterExport)
    }
    fun open() = picker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type="*/*"; addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    })
    fun folder() = folder.launch(null)
    fun export(finish: Boolean) {
        val model=activity.model; val session=model.state.value.editor ?: return
        model.editing.finishAfterExport=finish; model.editing.exportPending=true
        model.update { it.copy(editBusy="Choose where to save the edited copy…") }
        try { export.launch(session.document.title) }
        catch (e: Exception) {
            model.editing.exportPending=false; model.update { it.copy(editBusy="") }
            model.message(e.message ?: "No file picker is available")
        }
    }
}
