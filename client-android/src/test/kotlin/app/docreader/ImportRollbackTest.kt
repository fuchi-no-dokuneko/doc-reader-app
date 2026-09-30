package app.docreader

import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import app.docreader.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ImportRollbackTest {
    @Test fun failedDatabaseCommitRestoresSnapshotAndDescriptor() = runBlocking {
        val app=RuntimeEnvironment.getApplication(); val repo=LocalRepository(app)
        try {
            val file=File(app.cacheDir,"rollback.py").apply { writeText("original") }
            val before=DocumentImport(app,repo).open(Uri.fromFile(file))
            SQLiteDatabase.openDatabase(app.getDatabasePath("reader.db").path,null,0).use {
                it.execSQL("CREATE TRIGGER reject_import BEFORE INSERT ON documents BEGIN SELECT RAISE(ABORT,'test commit failure'); END")
            }
            file.writeText("replacement")
            val failure=runCatching { DocumentImport(app,repo).open(Uri.fromFile(file)) }.exceptionOrNull()
            assertNotNull(failure)
            assertEquals(before,repo.documents().single())
            assertEquals("original",repo.file(before.id).readText())
            assertEquals(listOf(before.id),repo.files.listFiles()!!.map { it.name })
            val other=File(app.cacheDir,"new.py").apply { writeText("new source") }
            assertTrue(runCatching { DocumentImport(app,repo).open(Uri.fromFile(other)) }.isFailure)
            assertEquals(listOf(before.id),repo.files.listFiles()!!.map { it.name })
        } finally { repo.close() }
    }
}
