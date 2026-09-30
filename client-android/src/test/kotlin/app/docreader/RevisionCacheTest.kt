package app.docreader

import android.net.Uri
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
class RevisionCacheTest {
    @Test fun changedImportKeepsOldLiveBlocksAndUsesNewRevision() = runBlocking {
        val app=RuntimeEnvironment.getApplication(); val repo=LocalRepository(app)
        try {
            val source=File(app.cacheDir,"revision.py").apply { writeText("print('A')") }
            val a=DocumentImport(app,repo).open(Uri.fromFile(source))
            val oldSnapshot=repo.snapshot(a).second
            val cache=repo.cache(a.id)
            BlockIndex.open(oldSnapshot,a,"UTF-8",cache) { it }.first.use { old ->
                source.writeText("print('B')")
                val b=DocumentImport(app,repo).open(Uri.fromFile(source))
                assertEquals(a.id,b.id); assertNotEquals(a.contentHash,b.contentHash)
                val newSnapshot=repo.snapshot(b).second
                BlockIndex.open(newSnapshot,b,"UTF-8",cache) { it }.first.use { latest ->
                    assertEquals("print('B')",latest.block(0).text)
                    assertEquals("print('A')",old.block(0).text)
                    assertEquals("print('A')",oldSnapshot.readText())
                }
                assertEquals(2,cache.listFiles()!!.count { it.name.startsWith("text-v3-") })
            }
            repo.remove(a.id)
            assertFalse(cache.exists()); assertFalse(repo.file(a.id).exists())
        } finally { repo.close() }
    }
}
