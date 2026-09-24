package app.docreader.data

import android.content.Context
import app.docreader.domain.*
import org.json.JSONArray

object LegacyImport {
    suspend fun run(context: Context, repository: LocalRepository) {
        val prefs = context.getSharedPreferences("reading", Context.MODE_PRIVATE)
        val raw = prefs.getString("library", null) ?: return
        val docs = JSONArray(raw)
        val existing = repository.documents().map { it.id }.toSet()
        for (i in 0 until docs.length()) {
            val row = docs.getJSONObject(i); val id = row.getString("id")
            if (id in existing || !repository.file(id).exists()) continue
            repository.remember(DocumentInfo(id, row.getString("name"),
                Kind.valueOf(row.getString("format")), row.getLong("size")))
            repository.savePosition(id, "last", ReadingPosition(
                prefs.getInt("$id.page", 0), prefs.getInt("$id.scroll", 0)))
        }
        prefs.edit().remove("library").apply()
    }
}
