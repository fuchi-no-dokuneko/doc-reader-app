package app.docreader

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.*

object NativeOpenWith {
    fun verify(activity: ActivityScenario<MainActivity>) {
        val context=NativeUi.instrumentation.targetContext
        var source=""; var count=0
        lateinit var screen: MainActivity
        activity.onActivity {
            screen=it
            source=it.model.tab(it.model.state.value.active!!)!!.document.source
            count=it.model.state.value.tabs.size
        }
        for (mime in listOf("text/plain","text/x-python","application/json","application/x-yaml",
            "application/x-ipynb+json","application/octet-stream","application/pdf","application/epub+zip")) {
            val request=Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(source),mime)
                .addCategory(Intent.CATEGORY_DEFAULT)
            assertTrue("Missing Open with registration for $mime",
                context.packageManager.queryIntentActivities(request,PackageManager.MATCH_DEFAULT_ONLY)
                    .any { it.activityInfo.packageName==context.packageName })
        }
        val request=Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(source),"application/epub+zip")
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val launchIntent=screen.intent
        try {
            context.startActivity(request)
            NativeUi.waitFor("External VIEW intent did not open the document") {
                var ready=false
                NativeUi.instrumentation.runOnMainSync {
                    val state=screen.model.state.value
                    ready=state.tabs.size==count+1 && screen.model.tab(state.active!!)!!.let { tab ->
                        tab.count>0 && tab.busy.isEmpty() && tab.error.isEmpty()
                    }
                }
                ready
            }
            NativeUi.waitFor("External opening did not show chapter text") { NativeUi.visible("Runtime EPUB chapter") }
            assertEquals(Intent.ACTION_VIEW,screen.intent.action)
        } finally {
            // ActivityScenario matches lifecycle events by the original launch intent.
            NativeUi.instrumentation.runOnMainSync { screen.intent=launchIntent }
        }
    }
}
