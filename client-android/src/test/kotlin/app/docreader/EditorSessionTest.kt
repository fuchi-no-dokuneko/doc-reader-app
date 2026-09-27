package app.docreader

import android.app.AlertDialog
import android.net.Uri
import android.content.res.Configuration
import app.docreader.ui.EditorPalette
import app.docreader.render.Palette
import app.docreader.domain.Theme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class EditorSessionTest {
    @Test fun rotationRetainsDraftBackPromptsAndDiscardDoesNotWrite() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            var activity=controller.get()
            val file=File(activity.cacheDir,"draft.kt").apply { writeText("val x = 1\n") }
            activity.model.importFile(Uri.fromFile(file)); UiHarness.ready(activity); UiHarness.edit(activity)
            UiHarness.field(activity).setText("val x = 2\n")
            val config=Configuration(activity.resources.configuration).apply { orientation=Configuration.ORIENTATION_LANDSCAPE }
            controller.configurationChange(config); activity=controller.get()
            UiHarness.await(activity) { activity.model.state.value.editor!=null }
            assertEquals("val x = 2\n",UiHarness.field(activity).text.toString())
            activity.onBackPressedDispatcher.onBackPressed()
            ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            assertTrue(activity.model.state.value.editor!!.dirty)
            activity.onBackPressedDispatcher.onBackPressed()
            ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_NEUTRAL).performClick()
            UiHarness.await(activity) { activity.model.state.value.editor==null }
            assertEquals("val x = 1\n",file.readText())
        }
    }
    @Test fun editPaletteDiffersInEveryReadingTheme() {
        val context=RuntimeEnvironment.getApplication()
        Theme.entries.forEach { theme ->
            val reading=Palette.forTheme(context,theme); val editing=EditorPalette.from(reading)
            assertNotEquals(reading.paper,editing.paper)
            assertNotEquals(reading.surface,editing.surface)
            assertTrue(androidx.core.graphics.ColorUtils.calculateContrast(editing.ink,editing.paper)>4.5)
        }
    }
}
