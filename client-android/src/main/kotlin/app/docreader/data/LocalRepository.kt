package app.docreader.data

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import app.docreader.domain.*
import java.io.File
import kotlinx.coroutines.sync.withLock

class LocalRepository(context: Context, name: String = "reader.db") : DocumentRepository {
    val files = File(context.filesDir, "documents").apply { mkdirs() }
    val derived = File(context.cacheDir, "reading").apply { mkdirs() }
    val importLock = RepositoryLocks.forDirectory(files)
    private val db = Room.databaseBuilder(context.applicationContext,
        ReaderDatabase::class.java, name).addMigrations(ReaderMigrations.V1_V2).build()
    private val dao = db.reader()
    override suspend fun documents() = dao.documents().map { it.info() }
    suspend fun document(id: String) = dao.document(id)?.info()
    suspend fun source(uri: String) = dao.source(uri)?.info()
    suspend fun snapshot(info: DocumentInfo) = RevisionSnapshot.open(this,info)
    override suspend fun remember(document: DocumentInfo) {
        with(document) { dao.document(DocumentRow(id,title,kind.name,size,source,opened,contentHash)) }
    }
    override suspend fun remove(id: String) = importLock.withLock {
        db.withTransaction { dao.deleteMarks(id); dao.deletePositions(id); dao.deleteDocument(id) }
        file(id).delete(); File(derived, id).deleteRecursively()
        Unit
    }
    override suspend fun position(document: String, tab: String): ReadingPosition {
        val row = dao.position(document, tab) ?: dao.position(document, "last")
        return row?.let { ReadingPosition(it.page, it.offset, it.anchor) } ?: ReadingPosition()
    }
    override suspend fun savePosition(document: String, tab: String, position: ReadingPosition) {
        val row = PositionRow(document, tab, position.page, position.offset, position.anchor)
        dao.position(row); dao.position(row.copy(tab = "last"))
    }
    override suspend fun marks(document: String) = dao.marks(document).map {
        Mark(it.id, it.document, it.page, it.start, it.end, it.text, it.type, it.bounds, it.anchor)
    }
    override suspend fun addMark(mark: Mark) {
        with(mark) { dao.mark(MarkRow(id, document, page, start, end, text, type, bounds, anchor)) }
    }
    suspend fun pageMarks(id: String,page: Int,start: Long,end: Long) = dao.pageMarks(id,page,start,end).map {
        Mark(it.id,it.document,it.page,it.start,it.end,it.text,it.type,it.bounds,it.anchor)
    }
    override suspend fun removeMark(id: Long) = dao.deleteMark(id)
    fun file(id: String) = File(files, id)
    fun cache(id: String) = File(derived, id).apply { mkdirs() }
    fun close() = db.close()
}
