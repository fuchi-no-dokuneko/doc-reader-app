package app.docreader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentRow(@PrimaryKey val id: String, val title: String, val kind: String,
    val size: Long, val source: String, val opened: Long)
@Entity(tableName = "positions", primaryKeys = ["document", "tab"])
data class PositionRow(val document: String, val tab: String, val page: Int,
    val offset: Int, val anchor: Long)
@Entity(tableName = "marks")
data class MarkRow(@PrimaryKey(autoGenerate = true) val id: Long = 0,
    val document: String, val page: Int, val start: Int, val end: Int,
    val text: String, val type: String, val bounds: String, val anchor: Long)
