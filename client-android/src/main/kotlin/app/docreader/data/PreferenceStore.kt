package app.docreader.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.docreader.domain.*
import kotlinx.coroutines.flow.map

private val Context.readerPreferences by preferencesDataStore("reader-settings")
class PreferenceStore(context: Context) {
    private val store = context.applicationContext.readerPreferences
    private val theme = stringPreferencesKey("theme")
    private val mode = stringPreferencesKey("mode")
    private val font = stringPreferencesKey("font")
    private val size = floatPreferencesKey("size")
    private val line = floatPreferencesKey("line")
    private val margin = intPreferencesKey("margin")
    private val encoding = stringPreferencesKey("encoding")
    private val jump = intPreferencesKey("jump")
    val settings = store.data.map { p -> ReadingSettings(
        Theme.valueOf(p[theme] ?: "SYSTEM"), Mode.valueOf(p[mode] ?: "PAGED"),
        p[font] ?: "serif", p[size] ?: 18f, p[line] ?: 1.4f, p[margin] ?: 24,
        p[encoding] ?: "Auto", p[jump] ?: 10) }
    suspend fun save(value: ReadingSettings) = store.edit {
        it[theme] = value.theme.name; it[mode] = value.mode.name; it[font] = value.font
        it[size] = value.size.coerceIn(12f, 40f); it[line] = value.lineHeight.coerceIn(1f, 2.5f)
        it[margin] = value.margin.coerceIn(0, 64); it[encoding] = value.encoding
        it[jump] = value.jump.coerceAtLeast(1)
    }
    val workspace = store.data.map { it[stringPreferencesKey("workspace")] ?: "" }
    suspend fun workspace(value: String) = store.edit { it[stringPreferencesKey("workspace")] = value }
    val folder = store.data.map { it[stringPreferencesKey("folder")] ?: "" }
    suspend fun folder(value: String) = store.edit { it[stringPreferencesKey("folder")] = value }
    fun encoding(id: String) = store.data.map { it[stringPreferencesKey("encoding.$id")] ?: "Auto" }
    suspend fun encoding(id: String, value: String) = store.edit {
        it[stringPreferencesKey("encoding.$id")] = value
    }
}
