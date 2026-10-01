package app.docreader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import app.docreader.domain.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class EditorSaveAsTest {
    @Test fun readOnlySourceUsesPickerCancelKeepsDraftAndExportPersistsIt() {
        Robolectric.buildActivity(MainActivity::class.java).setup().use { controller ->
            val activity=controller.get(); val model=activity.model
            assertSame("ViewModel application",activity.application,model.getApplication<android.app.Application>())
            model.repo.file("read-only").writeText("name: before\n")
            val doc=DocumentInfo("read-only","settings.yaml",Kind.YAML,13)
            runBlocking { model.repo.remember(doc) }
            model.workspace.open(doc)
            UiHarness.ready(activity); model.workspace.split()
            UiHarness.ready(activity); UiHarness.edit(activity)
            val field=UiHarness.field(activity); field.setText("name: after\n")
            UiHarness.button(activity,"Save").performClick()
            var request=Shadows.shadowOf(activity).nextStartedActivityForResult
            assertEquals(Intent.ACTION_CREATE_DOCUMENT,request.intent.action)
            assertEquals("settings.yaml",request.intent.getStringExtra(Intent.EXTRA_TITLE))
            activity.activityResultRegistry.dispatchResult(request.requestCode,Activity.RESULT_CANCELED,null)
            UiHarness.await(activity) { model.state.value.editBusy.isEmpty() }
            assertTrue(model.state.value.editor!!.dirty)
            assertEquals("name: after\n",UiHarness.field(activity).text.toString())
            UiHarness.button(activity,"Save").performClick()
            request=Shadows.shadowOf(activity).nextStartedActivityForResult
            val output=File(activity.cacheDir,"saved.yaml")
            activity.activityResultRegistry.dispatchResult(request.requestCode,Activity.RESULT_OK,Intent().setData(Uri.fromFile(output)))
            UiHarness.await(activity) { model.state.value.editBusy.isEmpty() && !model.state.value.editor!!.dirty }
            assertEquals("name: after\n",output.readText())
            assertEquals("name: before\n",model.repo.file("read-only").readText())
            assertEquals(output.toURI().path,Uri.parse(model.state.value.editor!!.document.source).path)
            assertEquals(2,model.state.value.library.size)
            assertEquals(1,model.state.value.tabs.count { it.document.id==doc.id })
            assertNotEquals(doc.id,model.tab(model.state.value.editor!!.tab)!!.document.id)
        }
    }
}
