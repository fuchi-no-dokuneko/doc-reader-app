package app.docreader

import android.os.Looper
import android.view.*
import android.widget.*
import org.junit.Assert.*
import org.robolectric.Shadows
import java.time.Duration

object UiHarness {
    fun views(root: View): List<View> = listOf(root)+if (root is ViewGroup)
        (0 until root.childCount).flatMap { views(root.getChildAt(it)) } else emptyList()
    fun button(activity: MainActivity,label: String) = views(activity.window.decorView)
        .filterIsInstance<Button>().first { it.text.toString()==label }
    fun field(activity: MainActivity) = views(activity.window.decorView).filterIsInstance<EditText>().single()
    fun await(activity: MainActivity,condition: () -> Boolean) {
        val deadline=System.nanoTime()+15_000_000_000
        while (System.nanoTime()<deadline) {
            val root=activity.window.decorView
            root.measure(View.MeasureSpec.makeMeasureSpec(1000,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1400,View.MeasureSpec.EXACTLY))
            root.layout(0,0,1000,1400)
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(30))
            if (condition()) return
            Thread.sleep(15)
        }
        fail("UI did not settle: ${activity.model.state.value}")
    }
    fun ready(activity: MainActivity) = await(activity) {
        val tabs=activity.model.state.value.tabs
        tabs.isNotEmpty() && tabs.all { it.busy.isEmpty() && it.count>0 && it.error.isEmpty() }
    }
    fun edit(activity: MainActivity) {
        button(activity,"Edit source").performClick()
        await(activity) { views(activity.window.decorView).any { it is EditText } && activity.model.state.value.editBusy.isEmpty() }
    }
}
