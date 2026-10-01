package app.docreader.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.docreader.data.*
import app.docreader.domain.*
import app.docreader.render.LocalAssets
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ReaderModel(app: Application) : AndroidViewModel(app) {
    val repo = LocalRepository(app)
    val preferences = PreferenceStore(app)
    val assets = LocalAssets(app)
    val scope get() = viewModelScope
    private val mutable = MutableStateFlow(ReaderState())
    val state = mutable.asStateFlow()
    val engines = mutableMapOf<String,TabRuntime>()
    val tabUpdates = kotlinx.coroutines.sync.Mutex()
    val workspace = Workspace(this)
    val loading = BookLoading(this)
    val reading = ReadingActions(this)
    val editing = Editing(this)
    init {
        scope.launch {
            preferences.settings.collect { settings ->
                val old = state.value.settings
                update { it.copy(settings = settings) }
                if (old != settings) loading.reflow()
            }
        }
        scope.launch {
            try {
                preferences.folder.first().takeIf { it.isNotBlank() }?.let { assets.folder=Uri.parse(it) }
                withContext(Dispatchers.IO) { LegacyImport.run(app,repo) }
                refresh(); workspace.restore(preferences.workspace.first())
            } catch (e: Exception) { message(e.message ?: "Could not restore the library") }
        }
    }
    fun update(transform: (ReaderState) -> ReaderState) { mutable.update(transform) }
    fun tab(id: String) = state.value.tabs.firstOrNull { it.id == id }
    fun change(id: String, transform: (ReaderTab) -> ReaderTab) = update { s ->
        s.copy(tabs = s.tabs.map { if (it.id == id) transform(it) else it })
    }
    fun message(text: String) = update { it.copy(message = text) }
    suspend fun refresh() { val docs = repo.documents(); update { it.copy(library = docs) } }
    fun importFile(uri: Uri, flags: Int = 0) = scope.launch {
        update { it.copy(importing = true) }
        try {
            val info = withContext(Dispatchers.IO) { DocumentImport(getApplication(),repo).open(uri,flags) }
            workspace.open(TabRefresh.apply(this@ReaderModel,info))
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) { message(e.message ?: "Unable to open file") }
        finally { update { it.copy(importing = false) } }
    }
    fun settings(value: ReadingSettings) = scope.launch { preferences.save(value) }
    fun encoding(id: String, value: String) = scope.launch {
        val tab = tab(id) ?: return@launch
        preferences.encoding(tab.document.id,value)
        state.value.tabs.filter { it.document.id == tab.document.id }.forEach { loading.restart(it.id) }
    }
    override fun onCleared() = ModelCleanup.close(engines.values.toList(),state.value.tabs,repo)
}
