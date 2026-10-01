package app.docreader.ui

import android.app.Application
import android.net.Uri
import java.io.File
import kotlinx.coroutines.*

object SharedImport {
    fun open(model: ReaderModel, text: String) = model.scope.launch {
        val file = withContext(Dispatchers.IO) {
            val directory=File(model.getApplication<Application>().cacheDir,
                "shared-text/${java.util.UUID.randomUUID()}").apply { mkdirs() }
            File(directory,"Shared text.txt").apply { writeText(text) }
        }
        try { model.importFile(Uri.fromFile(file)).join() }
        finally { withContext(NonCancellable+Dispatchers.IO) { file.parentFile?.deleteRecursively() } }
    }
}
