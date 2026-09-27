package app.docreader

import android.net.Uri
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.shadows.ShadowToast
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class EditorConflictTest {
    @Test fun externallyChangedSourceIsPreservedAndDraftRemainsEditable() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            val file=File(activity.cacheDir,"conflict.js").apply { writeText("const n = 1;\n") }
            model.importFile(Uri.fromFile(file)); UiHarness.ready(activity); UiHarness.edit(activity)
            UiHarness.field(activity).setText("const n = 2;\n")
            file.writeText("const n = 3;\n")
            UiHarness.button(activity,"Save").performClick()
            UiHarness.await(activity) { model.state.value.editBusy.isEmpty() && ShadowToast.getTextOfLatestToast()!=null }
            assertEquals("const n = 3;\n",file.readText())
            assertEquals("const n = 2;\n",UiHarness.field(activity).text.toString())
            assertTrue(model.state.value.editor!!.dirty)
            assertTrue(ShadowToast.getTextOfLatestToast().contains("changed outside"))
            assertTrue(UiHarness.field(activity).isEnabled)
        }
    }
}
