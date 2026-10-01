package app.docreader.data

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import java.io.File

object SourceWrite {
    fun canWrite(context: Context, source: String): Boolean {
        val uri=Uri.parse(source)
        if (uri.scheme=="file") return File(uri.path.orEmpty()).canWrite()
        return uri.scheme=="content" && context.checkUriPermission(uri,Process.myPid(),Process.myUid(),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)==PackageManager.PERMISSION_GRANTED
    }
    fun write(context: Context,uri: Uri,bytes: ByteArray,expectedHash: String? = null) {
        val resolver=context.contentResolver
        if (expectedHash!=null) {
            val actual=resolver.openInputStream(uri)?.use(FileHash::read)
            require(expectedHash.isNotEmpty() && actual==expectedHash) {
                "The original file changed outside Doc Reader. Use Save a copy to keep both versions."
            }
        }
        if (uri.scheme=="file") {
            val atomic=android.util.AtomicFile(File(uri.path.orEmpty()))
            val out=atomic.startWrite()
            try { out.write(bytes); atomic.finishWrite(out) }
            catch (e: Exception) { atomic.failWrite(out); throw e }
        } else {
            resolver.openOutputStream(uri,"wt")?.use { it.write(bytes); it.flush() }
                ?: error("This file provider did not allow saving. Use Save a copy.")
            runCatching { resolver.takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
        }
    }
}
