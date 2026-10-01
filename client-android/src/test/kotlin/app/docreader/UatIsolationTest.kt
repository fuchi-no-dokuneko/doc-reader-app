package app.docreader

import android.net.Uri
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File
import app.docreader.ui.LibraryActions
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class UatIsolationTest {
    @Test fun editingIdenticalBytesInSecondSourceDoesNotRenameFirstTab() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            var activity=controller.get(); val model=activity.model
            val first=File(activity.cacheDir,"first.py").apply { writeText("print('same')\n") }
            val second=File(activity.cacheDir,"second.py").apply { writeText(first.readText()) }
            model.importFile(Uri.fromFile(first)); UiHarness.ready(activity)
            val firstTab=model.state.value.active!!
            model.importFile(Uri.fromFile(second)); UiHarness.ready(activity)
            val secondTab=model.state.value.active!!
            val ids=IsolationState.seed(model,firstTab,secondTab)
            controller.recreate(); activity=controller.get(); UiHarness.ready(activity)
            assertEquals(ids,model.state.value.tabs.map { it.document.id })
            IsolationState.check(model,ids)
            UiHarness.edit(activity)
            UiHarness.field(activity).setText("print('changed')\n")
            UiHarness.button(activity,"Save").performClick()
            UiHarness.await(activity) { model.state.value.editBusy.isEmpty() && !model.state.value.editor!!.dirty }
            assertEquals("print('same')\n",first.readText())
            assertEquals("print('changed')\n",second.readText())
            assertEquals("first.py",model.tab(firstTab)!!.document.title)
            UiHarness.button(activity,"Read").performClick(); UiHarness.ready(activity)
            assertEquals("print('same')",model.engines[firstTab]!!.text!!.blocks.block(0).text)
            assertEquals("print('changed')",model.engines[secondTab]!!.text!!.blocks.block(0).text)
            IsolationState.check(model,ids)
            val remove=LibraryActions.remove(model,ids[1])
            UiHarness.await(activity) { remove.isCompleted }; UiHarness.ready(activity)
            assertEquals(ids[0],model.state.value.library.single().id)
            assertFalse(File(model.repo.derived,ids[1]).exists())
            assertEquals("print('same')\n",model.repo.file(ids[0]).readText())
            assertEquals(1,runBlocking { model.repo.marks(ids[0]).size })
            assertEquals("print('same')",model.engines[firstTab]!!.text!!.blocks.block(0).text)
        }
    }
}
