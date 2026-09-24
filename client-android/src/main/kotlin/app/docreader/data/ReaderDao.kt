package app.docreader.data

import androidx.room.*

@Dao
interface ReaderDao {
    @Query("SELECT * FROM documents ORDER BY opened DESC")
    suspend fun documents(): List<DocumentRow>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun document(row: DocumentRow)
    @Query("DELETE FROM documents WHERE id = :id") suspend fun deleteDocument(id: String)
    @Query("SELECT * FROM positions WHERE document = :id AND tab = :tab LIMIT 1")
    suspend fun position(id: String, tab: String): PositionRow?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun position(row: PositionRow)
    @Query("DELETE FROM positions WHERE document = :id") suspend fun deletePositions(id: String)
    @Query("SELECT * FROM marks WHERE document = :id ORDER BY page, start")
    suspend fun marks(id: String): List<MarkRow>
    @Query("SELECT * FROM marks WHERE document=:id AND type='highlight' AND ((bounds<>'' AND page=:page) OR (bounds='' AND anchor>=:start AND anchor<:end))")
    suspend fun pageMarks(id: String,page: Int,start: Long,end: Long): List<MarkRow>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun mark(row: MarkRow)
    @Query("DELETE FROM marks WHERE id = :id") suspend fun deleteMark(id: Long)
    @Query("DELETE FROM marks WHERE document = :id") suspend fun deleteMarks(id: String)
}

@Database(entities = [DocumentRow::class, PositionRow::class, MarkRow::class],
    version = 1, exportSchema = false)
abstract class ReaderDatabase : RoomDatabase() { abstract fun reader(): ReaderDao }
