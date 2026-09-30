package app.docreader

import android.net.Uri
import app.docreader.data.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ImportIdentityTest {
    private val app get()=RuntimeEnvironment.getApplication()
    @Test fun concurrentImportersAndRepositoriesKeepOneSourceIdentity() = runBlocking {
        val file=File(app.cacheDir,"concurrent.py").apply { writeText("print(1)") }
        val repos=listOf(LocalRepository(app),LocalRepository(app))
        try {
            val docs=(0..11).map { i -> async(Dispatchers.IO) {
                DocumentImport(app,repos[i%2]).open(Uri.fromFile(file))
            } }.awaitAll()
            assertEquals(1,docs.map { it.id }.distinct().size)
            assertEquals(1,repos.first().documents().size)
            assertEquals(FileHash.of(file),docs.first().contentHash)
            assertEquals(1,repos.first().files.listFiles()!!.size)
        } finally { repos.forEach { it.close() } }
    }
    @Test fun identicalNamesAndBytesRemainSeparateAndHistoricalDuplicatesRemain() = runBlocking {
        val repo=LocalRepository(app)
        try {
            suspend fun open(dir: String) = File(app.cacheDir,"$dir/same.py").let {
                it.parentFile!!.mkdirs(); it.writeText("same bytes")
                DocumentImport(app,repo).open(Uri.fromFile(it))
            }
            val first=open("a"); val second=open("b")
            assertNotEquals(first.id,second.id); assertEquals(first.contentHash,second.contentHash)
            repo.remember(first.copy(id="old-b",opened=1))
            repo.remember(first.copy(id="old-a",opened=1))
            repo.remember(first.copy(opened=0))
            val again=DocumentImport(app,repo).open(Uri.parse(first.source))
            assertEquals("old-a",again.id)
            assertEquals(4,repo.documents().size)
            assertEquals(second,repo.document(second.id))
            val same=DocumentImport(app,repo).open(Uri.parse(first.source))
            assertEquals(again.id,same.id); assertEquals(again.contentHash,same.contentHash)
        } finally { repo.close() }
    }
}
