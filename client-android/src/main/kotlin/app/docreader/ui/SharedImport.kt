package app.docreader.ui

import android.app.Application
import android.net.Uri
import java.io.File
import kotlinx.coroutines.*

object SharedImport {
    fun open(model: ReaderModel, text: String) = model.scope.launch {
        val file = withContext(Dispatchers.IO) {
            File(model.getApplication<Application>().cacheDir,"Shared text.txt").apply { writeText(text) }
        }
        model.importFile(Uri.fromFile(file)).join()
        withContext(Dispatchers.IO) { file.delete() }
    }
}
