package app.docreader

import android.net.Uri
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ReimportFlowTest {
    @Test fun sameUriRefreshesAllTabsAndKeepsTheirPlaces() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            fun text(value: String)=(1..160).joinToString("\n") { "print('$value line $it')" }
            val file=File(activity.cacheDir,"repeat.py").apply { writeText(text("A")) }
            model.importFile(Uri.fromFile(file)); UiHarness.ready(activity)
            val before=model.state.value.library.single()
            model.workspace.split(); UiHarness.ready(activity)
            val ids=model.state.value.tabs.map { it.id }
            ids.forEachIndexed { i,id -> model.reading.move(id,i+1) }
            val positions=ids.map { model.tab(it)!!.position }
            file.writeText(text("B"))
            val job=model.importFile(Uri.fromFile(file))
            UiHarness.await(activity) { job.isCompleted }; UiHarness.ready(activity)
            val after=model.state.value.library.single()
            assertEquals(before.id,after.id); assertNotEquals(before.contentHash,after.contentHash)
            assertEquals(positions,ids.map { model.tab(it)!!.position })
            model.engines.values.forEach { assertEquals("print('B line 1')",it.text!!.blocks.block(0).text) }
            val again=model.importFile(Uri.fromFile(file))
            UiHarness.await(activity) { again.isCompleted }; UiHarness.ready(activity)
            assertEquals(after.id,model.state.value.library.single().id)
            assertEquals(after.contentHash,model.state.value.library.single().contentHash)
            assertEquals(setOf(after.id),model.state.value.tabs.map { it.document.id }.toSet())
        }
    }
}
