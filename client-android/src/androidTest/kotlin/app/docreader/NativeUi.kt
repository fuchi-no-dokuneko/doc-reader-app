package app.docreader

import android.os.SystemClock
import android.graphics.Rect
import android.view.MotionEvent
import android.view.InputDevice
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry

object NativeUi {
    val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    fun waitFor(message: String,condition: () -> Boolean) {
        val limit=SystemClock.uptimeMillis()+120000
        while (SystemClock.uptimeMillis()<limit) {
            if (condition()) return
            SystemClock.sleep(250)
        }
        error("$message; visible: "+nodes(instrumentation.uiAutomation.rootInActiveWindow).mapNotNull { it.text })
    }
    fun nodes(node: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        if (node==null) return emptyList()
        return listOf(node)+(0 until node.childCount).flatMap { nodes(node.getChild(it)) }
    }
    fun visible(text: String) = nodes(instrumentation.uiAutomation.rootInActiveWindow)
        .any { it.text?.toString()?.contains(text)==true && it.isVisibleToUser }
    fun click(label: String) {
        android.util.Log.i("NativeUi","Finding $label")
        instrumentation.uiAutomation.waitForIdle(800,10000)
        waitFor("Cannot click $label") {
            val node=nodes(instrumentation.uiAutomation.rootInActiveWindow).lastOrNull {
                it.isVisibleToUser && (it.text?.toString()==label ||
                    it.contentDescription?.toString()?.contains(label)==true)
            }
            if (node==null) false else {
                val bounds=Rect(); node.getBoundsInScreen(bounds)
                val time=SystemClock.uptimeMillis()
                for (action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) {
                    val event=MotionEvent.obtain(time,SystemClock.uptimeMillis(),action,
                        bounds.exactCenterX(),bounds.exactCenterY(),0)
                    event.source=InputDevice.SOURCE_TOUCHSCREEN
                    check(instrumentation.uiAutomation.injectInputEvent(event,true))
                    event.recycle(); SystemClock.sleep(60)
                }
                android.util.Log.i("NativeUi","Tapped $label")
                SystemClock.sleep(1000)
                instrumentation.uiAutomation.waitForIdle(800,10000)
                true
            }
        }
    }
}
