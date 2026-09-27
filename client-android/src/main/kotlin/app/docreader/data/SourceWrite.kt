package app.docreader.data

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import java.io.File
import java.security.MessageDigest

object SourceWrite {
    fun canWrite(context: Context, source: String): Boolean {
        val uri=Uri.parse(source)
        if (uri.scheme=="file") return File(uri.path.orEmpty()).canWrite()
        return uri.scheme=="content" && context.checkUriPermission(uri,Process.myPid(),Process.myUid(),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)==PackageManager.PERMISSION_GRANTED
    }
    fun write(context: Context,uri: Uri,bytes: ByteArray,expected: File? = null) {
        val resolver=context.contentResolver
        if (expected!=null) {
            val actual=resolver.openInputStream(uri)?.use(::digest)
            require(actual!=null && expected.inputStream().use(::digest).contentEquals(actual)) {
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
    private fun digest(input: java.io.InputStream): ByteArray {
        val digest=MessageDigest.getInstance("SHA-256"); val buffer=ByteArray(65536)
        while (true) { val n=input.read(buffer); if (n<0) break; digest.update(buffer,0,n) }
        return digest.digest()
    }
}
