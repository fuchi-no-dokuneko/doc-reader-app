package app.docreader

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpubPickerNativeTest {
    @Test fun documentPickerImportsEmptyAndExternalStylesheets() {
        NativeAccess.automation
        ActivityScenario.launch(MainActivity::class.java).use { activity ->
            for (name in listOf("empty","styled")) {
                NativeUi.click("Open")
                if (!NativeUi.visible("$name.epub")) {
                    NativeUi.click("Show roots")
                    NativeUi.click("Doc Reader fixtures")
                }
                NativeUi.click("$name.epub")
                NativeUi.waitFor("EPUB did not paginate: $name") {
                    var ready=false
                    activity.onActivity { screen ->
                        val tab=screen.model.tab(screen.model.state.value.active.orEmpty())
                        ready=tab!=null && tab.document.title=="$name.epub" && tab.count>0 && tab.busy.isEmpty()
                    }
                    ready
                }
                NativeUi.waitFor("EPUB chapter is not visible") { NativeUi.visible("Runtime EPUB chapter") }
                NativeUi.waitFor("EPUB body is not visible") { NativeUi.visible("Readable $name stylesheet EPUB.") }
                val image=NativeUi.instrumentation.uiAutomation.takeScreenshot()
                java.io.File(NativeUi.instrumentation.targetContext.filesDir,"$name.png").outputStream().use {
                    image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)
                }
                image.recycle()
                activity.onActivity { screen ->
                    val tab=screen.model.tab(screen.model.state.value.active!!)!!
                    assertEquals("",tab.error); assertTrue(tab.count>0)
                    val blocks=screen.model.engines[tab.id]!!.text!!.blocks
                    if (name=="styled") assertTrue((0 until blocks.count).any { "italic" in blocks.block(it).style })
                }
            }
            NativeOpenWith.verify(activity)
        }
    }
}
