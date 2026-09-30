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
            val session=withContext(Dispatchers.IO) {
                val (document,file)=model.repo.snapshot(tab.document)
                EditSession(id,document,SourceText.read(file,encoding))
            }
            model.update { it.copy(editor=session) }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { model.message(e.message ?: "Unable to edit this file") }
        finally { model.update { it.copy(editBusy="") } }
    }
    fun save(uri: Uri,copy: Boolean,finish: Boolean=false) = model.scope.launch {
        val session=model.state.value.editor ?: return@launch
        if (model.state.value.editBusy.isNotEmpty()) return@launch
        val value=session.text
        val move=copy && uri.toString()!=session.document.source
        model.update { it.copy(editBusy="Saving…") }
        try {
            val doc=withContext(Dispatchers.IO) {
                val bytes=session.source.encode(value)
                require(move || uri.toString()==session.document.source) { "Use Save a copy for a different source." }
                require(move || model.repo.document(session.document.id)?.source==uri.toString()) {
                    "The original library entry changed. Use Save a copy."
                }
                SourceWrite.write(model.getApplication(),uri,bytes,
                    if (move) null else session.baselineHash)
                DocumentImport(model.getApplication(),model.repo).open(uri,retainId=if (move) null else session.document.id)
            }
            EditedTabs.apply(model,session,doc,move)
            session.document=doc; session.baselineHash=doc.contentHash; session.saved=value
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
