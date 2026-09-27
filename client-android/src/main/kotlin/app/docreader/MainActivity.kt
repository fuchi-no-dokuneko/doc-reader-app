package app.docreader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import app.docreader.ui.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    lateinit var model: ReaderModel
    private lateinit var workspace: WorkspaceView
    private val screenScope = CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private val files=DocumentPicker(this)
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        model = ViewModelProvider(this)[ReaderModel::class.java]
        workspace = WorkspaceView(this,model)
        screenScope.launch { model.state.collect { value ->
            workspace.render(value)
            if (value.message.isNotEmpty()) { Toast.makeText(this@MainActivity,value.message,Toast.LENGTH_LONG).show(); model.message("") }
        } }
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (model.state.value.editor!=null) EditorActions(this@MainActivity,model).close()
                else if (!model.state.value.home) { model.update { it.copy(home=true) }; model.workspace.save() }
                else finish()
            }
        })
        if (state == null) accept(intent)
    }
    fun pick() = files.open()
    fun pickFolder() = files.folder()
    fun exportDraft(finish: Boolean) = files.export(finish)
    fun install(view: View) {
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v,insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom); insets
        }; ViewCompat.requestApplyInsets(view)
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); accept(intent) }
    private fun accept(intent: Intent) = IncomingIntent.accept(model,intent)
    override fun onStop() { model.workspace.save(); super.onStop() }
    override fun onDestroy() { screenScope.cancel(); workspace.dispose(); super.onDestroy() }
}
