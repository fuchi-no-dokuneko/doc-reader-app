package app.docreader

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import app.docreader.render.*
import app.docreader.domain.Mark
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class EditorFlowTest {
    @Test fun editsHighlightImmediatelySaveOriginalAndRefreshBothTabs() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            val original=File(activity.cacheDir,"script.py").apply { writeText("print('before')\n") }
            model.importFile(Uri.fromFile(original)); UiHarness.ready(activity)
            val originalId=model.state.value.library.single().id
            runBlocking { model.repo.addMark(Mark(document=originalId,page=0,text="saved bookmark")) }
            model.workspace.split(); UiHarness.ready(activity)
            UiHarness.edit(activity)
            val field=UiHarness.field(activity)
            val reading=Palette.forTheme(activity,model.state.value.settings.theme)
            assertNotEquals(reading.paper,(field.background as ColorDrawable).color)
            val code="def run():\n    return 42 # changed\n"
            field.text.replace(0,field.length(),code); field.setSelection(4)
            UiHarness.await(activity) { field.text.getSpans(0,field.length(),Syntax.Ink::class.java).size>=5 }
            assertEquals(4,field.selectionStart)
            assertTrue(model.state.value.editor!!.dirty)
            UiHarness.button(activity,"Save").performClick()
            UiHarness.await(activity) { model.state.value.editBusy.isEmpty() && !model.state.value.editor!!.dirty }
            assertEquals(code,original.readText())
            assertEquals(1,model.state.value.tabs.map { it.document.id }.distinct().size)
            UiHarness.button(activity,"Read").performClick(); UiHarness.ready(activity)
            model.engines.values.forEach { runtime ->
                assertEquals("def run():",runtime.text!!.blocks.block(0).text)
            }
            assertEquals(code,model.repo.file(model.state.value.tabs.first().document.id).readText())
            assertEquals(1,model.state.value.library.size)
            assertEquals(originalId,model.state.value.library.single().id)
            assertEquals("saved bookmark",runBlocking { model.repo.marks(originalId).single().text })
        }
    }
}
