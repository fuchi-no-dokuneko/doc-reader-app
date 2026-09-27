package app.docreader

import android.app.Application
import app.docreader.data.*
import app.docreader.domain.*
import app.docreader.render.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidReadingTest {
    private val context get()=RuntimeEnvironment.getApplication()
    @Test fun realTextMeasurementReflowsAndStaysBounded() {
        val dir=File(context.cacheDir,"layout-test").apply { mkdirs() }
        DiskBlocks(dir,true).use { out -> repeat(6000) { out.emit(Block(text="Chapter $it. "+"Readable text across several lines. ".repeat(50))) } }
        TextBook(DiskBlocks(dir),"UTF-8",File(dir,"pages"),LocalAssets(context)).use { book ->
            val spec=LayoutSpec(480,640,1f,1f,ReadingSettings())
            book.paginate(spec); val first=book.pages!!.count
            assertTrue("Actual page count: $first",first>5000)
            assertTrue(book.page(first-1).last().second.text.contains("Chapter 5999"))
            book.paginate(spec.copy(settings=spec.settings.copy(size=24f)))
            assertTrue(book.pages!!.count>first)
        }
    }
    @Test fun positionsMarksAndPreferencesSurviveRepositoryReopen() = runBlocking {
        val doc=DocumentInfo("persistence-test","Example.md",Kind.MARKDOWN,100)
        LocalRepository(context).also { repo ->
            repo.remember(doc)
            repo.savePosition(doc.id,"left",ReadingPosition(14,125,1234))
            repo.savePosition(doc.id,"right",ReadingPosition(84,46,9876))
            repo.addMark(Mark(document=doc.id,page=14,text="selected",type="highlight",anchor=1234))
            repo.close()
        }
        val repo=LocalRepository(context)
        assertEquals(ReadingPosition(14,125,1234),repo.position(doc.id,"left"))
        assertEquals(ReadingPosition(84,46,9876),repo.position(doc.id,"right"))
        assertEquals("selected",repo.marks(doc.id).single().text)
        val prefs=PreferenceStore(context)
        prefs.save(ReadingSettings(theme=Theme.SEPIA,jump=50,mode=Mode.SCROLL))
        prefs.encoding(doc.id,"Big5")
        assertEquals(50,PreferenceStore(context).settings.first().jump)
        assertEquals("Big5",PreferenceStore(context).encoding(doc.id).first())
        repo.close()
    }
}
