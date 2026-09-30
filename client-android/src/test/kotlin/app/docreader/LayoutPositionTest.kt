package app.docreader

import android.net.Uri
import app.docreader.domain.ReadingPosition
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class LayoutPositionTest {
    @Test fun pendingPaginationKeepsTheLatestRestoredPosition() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            val file=File(activity.cacheDir,"layout-position.py").apply {
                writeText((1..160).joinToString("\n") { "print('line $it')" })
            }
            model.importFile(Uri.fromFile(file)); UiHarness.ready(activity)
            val id=model.state.value.active!!
            val runtime=model.engines[id]!!
            model.update { it.copy(home=true) }; UiHarness.ready(activity)
            assertEquals(0,model.tab(id)!!.position.page)
            val pages=runtime.text!!.pages!!
            assertTrue(pages.count>2)
            val restored=ReadingPosition(2,7,pages.page(2).anchor)
            runBlocking { runtime.mutex.lock() }
            try {
                model.loading.layout(id,runtime.layout!!,true)
                UiHarness.await(activity) { model.tab(id)!!.busy=="Laying out pages…" }
                // Loading a stored position may finish while pagination is pending.
                model.change(id) { it.copy(position=restored) }
            } finally { runtime.mutex.unlock() }
            UiHarness.ready(activity)
            assertEquals(restored,model.tab(id)!!.position)
        }
    }
}
