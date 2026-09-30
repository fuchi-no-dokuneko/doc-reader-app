package app.docreader

import android.content.Intent
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class SharedIntentTest {
    @Test fun repeatedTextSharesHaveSeparateSourcesEvenForIdenticalBytes() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            repeat(2) { index ->
                controller.newIntent(Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT,"Independent shared snippet"))
                UiHarness.await(activity) { model.state.value.tabs.size==index+1 }
                UiHarness.ready(activity)
            }
            val docs=model.state.value.library
            assertEquals(2,docs.size)
            assertEquals(2,docs.map { it.id }.distinct().size)
            assertEquals(2,docs.map { it.source }.distinct().size)
            assertEquals(1,docs.map { it.contentHash }.distinct().size)
            assertTrue(docs.all { it.title=="Shared text.txt" })
            model.engines.values.forEach {
                assertEquals("Independent shared snippet",it.text!!.blocks.block(0).text)
            }
            UiHarness.await(activity) { File(activity.cacheDir,"shared-text").listFiles()?.isEmpty()==true }
        }
    }
}
