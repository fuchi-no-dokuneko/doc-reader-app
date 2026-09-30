package app.docreader

import android.net.Uri
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.docreader.data.*
import app.docreader.domain.ReadingPosition
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationNativeTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    @get:Rule val helper=MigrationTestHelper(instrumentation,ReaderDatabase::class.java)
    @Test fun versionOnePreservesIdentityMarksPositionsWorkspaceAndEncoding() = runBlocking {
        val context=instrumentation.targetContext
        context.deleteDatabase(MigrationFixture.name)
        val (id,source)=MigrationFixture.create(helper,context)
        helper.runMigrationsAndValidate(MigrationFixture.name,2,true,ReaderMigrations.V1_V2).close()
        val repo=LocalRepository(context,MigrationFixture.name)
        try {
            assertEquals(2,repo.documents().size)
            assertEquals(id.lowercase(),repo.document(id)!!.contentHash)
            assertEquals(ReadingPosition(3,27,4294967296),repo.position(id,"legacy-tab"))
            assertEquals(9L,repo.marks(id).single().id)
            assertEquals("retained",repo.marks(id).single().text)
            val prefs=PreferenceStore(context)
            assertTrue(prefs.workspace.first().contains(id))
            assertEquals("UTF-8",prefs.encoding(id).first())
            val old=repo.document("opaque-old")!!
            assertEquals("",old.contentHash)
            val (resolved,snapshot)=repo.snapshot(old)
            assertEquals(FileHash.of(snapshot),resolved.contentHash)
            assertEquals(resolved,repo.document(old.id))
            source.writeText("print('updated')")
            val imported=DocumentImport(context,repo).open(Uri.fromFile(source))
            assertEquals(id,imported.id)
            assertEquals(FileHash.of(source),imported.contentHash)
            assertEquals(2,repo.documents().size)
            assertEquals(9L,repo.marks(id).single().id)
            assertEquals(ReadingPosition(3,27,4294967296),repo.position(id,"legacy-tab"))
            assertTrue(prefs.workspace.first().contains(id))
        } finally { repo.close(); context.deleteDatabase(MigrationFixture.name) }
    }
}
