package app.docreader.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import app.docreader.domain.*
import java.io.File
import java.security.MessageDigest

class DocumentImport(private val context: Context, private val repo: LocalRepository) {
    suspend fun open(uri: Uri, flags: Int = 0): DocumentInfo {
        val resolver = context.contentResolver
        if (flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0) runCatching {
            resolver.takePersistableUriPermission(uri, flags and
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
        }
        val title = if (uri.scheme == "file") File(uri.path.orEmpty()).name else
            resolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Document"
        val temporary = File.createTempFile("import-", ".part",repo.files)
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            resolver.openInputStream(uri)?.use { input -> temporary.outputStream().use { out ->
                val buffer = ByteArray(65536)
                while (true) {
                    if (Thread.currentThread().isInterrupted) throw InterruptedException()
                    val n = input.read(buffer); if (n < 0) break
                    if (repo.files.usableSpace < buffer.size * 2) error("Not enough storage to import this document")
                    digest.update(buffer,0,n); out.write(buffer,0,n)
                }
            } } ?: error("The file provider could not open this document")
            val id = digest.digest().joinToString("") { "%02x".format(it) }
            val kind = Formats.detect(title,resolver.getType(uri))
            validate(temporary,kind)
            val destination = repo.file(id)
            if (!destination.exists()) check(temporary.renameTo(destination)) { "Could not save document" }
            val info = DocumentInfo(id,title,kind,destination.length(),uri.toString())
            repo.remember(info); return info
        } finally { temporary.delete() }
    }
    private fun validate(file: File, kind: Kind) {
        if (kind != Kind.TEXT) return
        val head = file.inputStream().use { input -> ByteArray(512).let { it.take(input.read(it).coerceAtLeast(0)) } }
        val zeros = head.count { it.toInt() == 0 }
        require(zeros == 0 || head.size > 2 && (zeros >= head.size / 4)) {
            "Unsupported binary file. Choose a document, notebook, source or configuration file."
        }
    }
}
