package app.docreader.ui

import android.widget.EditText
import app.docreader.render.*
import kotlinx.coroutines.*

class EditorHighlight(private val field: EditText,var language: String,private val dark: Boolean) {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private var job: Job?=null
    fun refresh() {
        job?.cancel()
        job=scope.launch {
            delay(120)
            val text=field.text.toString()
            val context=currentCoroutineContext()
            val tokens=withContext(Dispatchers.Default) { SyntaxTokens.scan(text,language) { context.isActive } }
            ensureActive()
            if (field.text.toString()==text) Syntax.apply(field.text,tokens,dark)
        }
    }
    fun close() { scope.cancel() }
}
