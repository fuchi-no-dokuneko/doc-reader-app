package app.docreader.ui

import app.docreader.domain.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import org.json.*

class Workspace(private val model: ReaderModel) {
    private var restoring=false
    fun open(info: DocumentInfo, id: String = UUID.randomUUID().toString()) {
        model.engines[id] = TabRuntime()
        model.update { s -> s.copy(tabs = s.tabs+ReaderTab(id,info),home=false,
            left = if (s.right == s.active && s.right != null) s.left else id,
            right = if (s.right == s.active && s.right != null) id else s.right,active=id) }
        model.loading.start(id); save()
    }
    fun select(id: String) {
        model.update { s -> when (id) {
            s.left, s.right -> s.copy(active=id,home=false)
            else -> if (s.active == s.right && s.right != null) s.copy(right=id,active=id,home=false)
                else s.copy(left=id,active=id,home=false)
        } }; save()
    }
    fun split() {
        val s = model.state.value
        if (s.right != null) model.update { it.copy(right=null,active=it.left) }
        else {
            val tab = s.tabs.firstOrNull { it.id == s.active } ?: return
            val other = s.tabs.firstOrNull { it.id != tab.id }
            if (other != null) model.update { it.copy(left=tab.id,right=other.id) }
            else { open(tab.document); model.update { it.copy(left=tab.id,right=it.active) } }
        }; save()
    }
    fun close(id: String) {
        model.engines.remove(id)?.let { r ->
            model.scope.launch(Dispatchers.IO) {
                r.job?.cancelAndJoin(); r.searchJob?.cancelAndJoin(); r.mutex.withLock { r.close() }
            }
        }
        model.update { s ->
            val tabs = s.tabs.filterNot { it.id == id }; val next = tabs.firstOrNull()?.id
            s.copy(tabs=tabs,left=if (s.left == id) s.right ?: next else s.left,
                right=if (s.right == id || s.left == id) null else s.right,
                active=if (s.active == id) next else s.active,home=tabs.isEmpty())
        }; save()
    }
    fun save() {
        if (restoring) return
        val json=WorkspaceCodec.encode(model.state.value)
        model.scope.launch { model.preferences.workspace(json) }
    }
    fun restore(raw: String) {
        if (raw.isBlank() || model.state.value.tabs.any()) return
        restoring=true
        runCatching {
            val json = JSONObject(raw); val tabs = json.getJSONArray("tabs")
            repeat(tabs.length()) {
                val row = tabs.getJSONObject(it)
                model.state.value.library.find { d -> d.id == row.getString("doc") }?.let { open(it,row.getString("id")) }
            }
            fun id(key: String) = json.optString(key).takeIf { model.tab(it) != null }
            model.update { it.copy(left=id("left"),right=id("right"),active=id("active"),home=json.optBoolean("home")) }
        }
        restoring=false; save()
    }
}
