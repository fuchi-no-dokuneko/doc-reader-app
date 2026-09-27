package app.docreader.ui

import android.net.Uri
import app.docreader.data.*
import app.docreader.domain.EditableFiles
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class Editing(private val model: ReaderModel) {
    var finishAfterExport=false
    var exportPending=false
    fun start(id: String) = model.scope.launch {
        if (model.state.value.editBusy.isNotEmpty() || model.state.value.editor!=null) return@launch
        val tab=model.tab(id) ?: return@launch
        if (!EditableFiles.supports(tab.document.kind)) return@launch
        model.update { it.copy(editBusy="Opening editor…") }
        try {
            val encoding=model.preferences.encoding(tab.document.id).first()
            val source=withContext(Dispatchers.IO) { SourceText.read(model.repo.file(tab.document.id),encoding) }
            model.update { it.copy(editor=EditSession(id,tab.document,source)) }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { model.message(e.message ?: "Unable to edit this file") }
        finally { model.update { it.copy(editBusy="") } }
    }
    fun save(uri: Uri,copy: Boolean,finish: Boolean=false) = model.scope.launch {
        val session=model.state.value.editor ?: return@launch
        if (model.state.value.editBusy.isNotEmpty()) return@launch
        val value=session.text
        model.update { it.copy(editBusy="Saving…") }
        try {
            val doc=withContext(Dispatchers.IO) {
                val bytes=session.source.encode(value)
                SourceWrite.write(model.getApplication(),uri,bytes,
                    if (copy) null else model.repo.file(session.document.id))
                DocumentImport(model.getApplication(),model.repo).open(uri)
            }
            EditedTabs.apply(model,session,doc,copy)
            session.document=doc; session.saved=value
            model.message("Saved ${doc.title}")
            if (finish) close()
        } catch (e: CancellationException) { throw e }
        catch (e: java.nio.charset.CharacterCodingException) {
            model.message("Some characters cannot be saved as ${session.source.encoding}. Your edits are still open.")
        } catch (e: Exception) { model.message(e.message ?: "Save failed. Your edits are still open.") }
        finally { model.update { it.copy(editBusy="") } }
    }
    fun close() { exportPending=false; model.update { it.copy(editor=null) } }
}
