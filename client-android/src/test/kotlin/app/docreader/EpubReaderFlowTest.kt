package app.docreader

import android.net.Uri
import android.widget.TextView
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class EpubReaderFlowTest {
    @Test fun importPaginateAndRenderLastEpubPageWithAndroidXml() = androidXml {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            val book=zipFixture(File(activity.cacheDir,"reading.epub"),mapOf(
                "META-INF/container.xml" to "<container><rootfiles><rootfile full-path='book.opf'/></rootfiles></container>",
                "book.opf" to "<package><manifest><item id='one' href='one.xhtml'/><item id='two' href='two.xhtml'/></manifest><spine><itemref idref='one'/><itemref idref='two'/></spine></package>",
                "one.xhtml" to "<html><body><h1>First chapter</h1><p>"+"Readable EPUB paragraph. ".repeat(200)+"</p></body></html>",
                "two.xhtml" to "<html><body><h1>Second chapter</h1><p>EPUB final marker 中文</p></body></html>"))
            model.importFile(Uri.fromFile(book)); UiHarness.ready(activity)
            val tab=model.state.value.tabs.single()
            assertTrue(tab.count>2)
            model.reading.move(tab.id,tab.count-1)
            UiHarness.await(activity) {
                UiHarness.views(activity.window.decorView).filterIsInstance<TextView>()
                    .any { "EPUB final marker 中文" in it.text.toString() }
            }
            assertEquals("",model.tab(tab.id)!!.error)
        }
    }
}
