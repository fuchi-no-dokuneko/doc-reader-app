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
class EditorReimportConflictTest {
    @Test fun reimportCannotReplaceTheEditorsConflictBaseline() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            val file=File(activity.cacheDir,"conflict.py").apply { writeText("print('A')") }
            val uri=Uri.fromFile(file)
            model.importFile(uri); UiHarness.ready(activity); UiHarness.edit(activity)
            val session=model.state.value.editor!!; val baseline=session.baselineHash
            UiHarness.field(activity).setText("print('draft')")
            file.writeText("print('B')")
            val imported=model.importFile(uri)
            UiHarness.await(activity) { imported.isCompleted }
            assertEquals(baseline,session.baselineHash)
            assertNotEquals(baseline,model.state.value.library.single().contentHash)
            assertEquals("print('B')",model.repo.file(session.document.id).readText())
            UiHarness.button(activity,"Save").performClick()
            UiHarness.await(activity) { model.state.value.editBusy.isEmpty() }
            assertEquals("print('B')",file.readText())
            assertTrue(session.dirty); assertSame(session,model.state.value.editor)
            assertEquals("print('draft')",UiHarness.field(activity).text.toString())
            assertEquals("print('A')",session.saved)
        }
    }
}
