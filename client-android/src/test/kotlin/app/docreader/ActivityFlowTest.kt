package app.docreader

import android.app.Application
import android.view.View
import app.docreader.domain.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import android.os.Looper
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ActivityFlowTest {
    @Test fun openingSameDocumentInTwoPanesKeepsIndependentPlaces() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            model.repo.file("flow-test").writeText((1..120).joinToString("\n") { "Line $it "+"reader ".repeat(40) })
            val info=DocumentInfo("flow-test","sample.py",Kind.CODE,model.repo.file("flow-test").length())
            model.workspace.open(info)
            fun pump() {
                val root=activity.window.decorView
                root.measure(View.MeasureSpec.makeMeasureSpec(1000,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1400,View.MeasureSpec.EXACTLY))
                root.layout(0,0,1000,1400)
                Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20))
            }
            fun awaitReady() {
                val deadline=System.nanoTime()+10_000_000_000
                while (System.nanoTime()<deadline) {
                    pump()
                    if (model.state.value.tabs.all { it.count>2 && it.busy.isEmpty() }) return
                    Thread.sleep(20)
                }
                fail(model.state.value.tabs.toString())
            }
            awaitReady()
            val left=model.state.value.active!!
            model.reading.move(left,2)
            model.workspace.split(); awaitReady()
            val right=model.state.value.right!!
            assertNotEquals(left,right)
            model.reading.move(right,5)
            assertEquals(2,model.tab(left)!!.position.page)
            assertEquals(5,model.tab(right)!!.position.page)
            model.settings(model.state.value.settings.copy(size=24f)); awaitReady()
            assertTrue(model.tab(left)!!.position.anchor>0)
            assertTrue(model.tab(right)!!.position.anchor>model.tab(left)!!.position.anchor)
        }
    }
}
