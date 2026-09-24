package app.docreader.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.*
import java.util.zip.ZipFile

class LocalAssets(private val context: Context) {
    var folder: Uri?=null
    fun open(path: String): InputStream {
        if (path.startsWith("zip:")) {
            val zip = ZipFile(path.removePrefix("zip:").substringBefore("!/"))
            val entry = zip.getEntry(path.substringAfter("!/").substringBefore('#'))
                ?: run { zip.close(); error("Missing image") }
            return object : FilterInputStream(zip.getInputStream(entry)) {
                override fun close() { try { super.close() } finally { zip.close() } }
            }
        }
        val uri = Uri.parse(path)
        require(uri.scheme == null || uri.scheme in setOf("file","content")) { "Only local images are loaded" }
        return if (uri.scheme == null) File(path).inputStream() else
            context.contentResolver.openInputStream(uri) ?: error("Image access is unavailable")
    }
    fun dimensions(path: String): Pair<Int,Int> = runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(path).use { BitmapFactory.decodeStream(it,null,options) }
        options.outWidth.coerceAtLeast(1) to options.outHeight.coerceAtLeast(1)
    }.getOrDefault(4 to 3)
    fun bitmap(path: String, width: Int, height: Int): Bitmap? = runCatching {
        val (w,h) = dimensions(path); var sample = 1
        while (w/sample > width*2 || h/sample > height*2 || w.toLong()*h/(sample.toLong()*sample)>4_000_000) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        open(path).use { BitmapFactory.decodeStream(it,null,options) }
    }.getOrNull()
    fun resolve(source: String, href: String): String {
        if (Uri.parse(href).scheme != null) return href
        val uri = Uri.parse(source)
        if (uri.scheme == "file") return File(File(uri.path.orEmpty()).parentFile,href).toURI().toString()
        if (uri.scheme == "content" && android.provider.DocumentsContract.isDocumentUri(context,uri)) {
            val id = android.provider.DocumentsContract.getDocumentId(uri)
            val sibling = id.substringBeforeLast('/',id.substringBefore(':')+":")
            val documentId=sibling + (if (sibling.endsWith(':')) "" else "/") + href
            return folder?.let { android.provider.DocumentsContract.buildDocumentUriUsingTree(it,documentId) }
                ?.toString() ?: android.provider.DocumentsContract.buildDocumentUri(uri.authority,documentId).toString()
        }
        return folder?.let {
            android.provider.DocumentsContract.buildDocumentUriUsingTree(it,
                android.provider.DocumentsContract.getTreeDocumentId(it)+"/"+href).toString()
        } ?: href
    }
}
