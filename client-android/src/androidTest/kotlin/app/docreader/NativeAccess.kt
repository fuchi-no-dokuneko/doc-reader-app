package app.docreader

import android.accessibilityservice.AccessibilityServiceInfo
import androidx.test.platform.app.InstrumentationRegistry

object NativeAccess {
    val automation by lazy {
        InstrumentationRegistry.getInstrumentation().uiAutomation.also { automation ->
            automation.serviceInfo=automation.serviceInfo.apply {
                flags=flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            }
        }
    }
    fun root() = automation.rootInActiveWindow ?: automation.windows.firstOrNull { it.isActive }?.root
}
