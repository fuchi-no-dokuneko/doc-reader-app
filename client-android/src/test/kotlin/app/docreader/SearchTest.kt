package app.docreader

import app.docreader.data.DiskBlocks
import app.docreader.domain.Block
import app.docreader.render.*
import app.docreader.ui.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class SearchTest {
    @Test fun findNextAdvancesWithinAndAcrossBlocks()=runBlocking {
        val context=RuntimeEnvironment.getApplication()
        val directory=File(context.cacheDir,"search").apply { mkdirs() }
        DiskBlocks(directory,true).use { it.emit(Block(text="needle then needle")); it.emit(Block(text="中文 NEEDLE")) }
        val runtime=TabRuntime().apply { text=TextBook(DiskBlocks(directory),"UTF-8",File(directory,"pages"),LocalAssets(context)) }
        try {
            val first=DocumentSearch.find(runtime,"needle",0)!!
            assertEquals(0,first.first)
            val next=DocumentSearch.find(runtime,"needle",first.first+1)!!
            assertEquals(12,next.first)
            val third=DocumentSearch.find(runtime,"needle",next.first+1)!!
            assertEquals((1L shl 32)+3,third.first)
            assertNull(DocumentSearch.find(runtime,"missing",0))
        } finally { runtime.close() }
    }
}
