package app.docreader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import app.docreader.ui.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    lateinit var model: ReaderModel
    private lateinit var workspace: WorkspaceView
    private val screenScope = CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private val folder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        if (it != null) FolderAccess.accept(this,it)
    }
    private val picker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) it.data?.let { data -> data.data?.let { uri -> model.importFile(uri,data.flags) } }
    }
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
                if (!model.state.value.home) { model.update { it.copy(home=true) }; model.workspace.save() }
                else finish()
            }
        })
        if (state == null) accept(intent)
    }
    fun pick() = picker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type="*/*"; addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    })
    fun pickFolder() = folder.launch(null)
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
