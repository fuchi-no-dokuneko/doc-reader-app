package app.docreader.format

import java.io.File
import java.io.InputStream
import java.util.UUID

class AssetFiles(val directory: File) {
    init { directory.mkdirs() }
    fun save(input: InputStream, suffix: String = "bin"): String {
        val name = UUID.randomUUID().toString() + "." + suffix
        val target = File(directory, name)
        try { input.use { source -> target.outputStream().use { source.copyTo(it, 16384) } } }
        catch (error: Exception) { target.delete(); throw error }
        return target.absolutePath
    }
    fun base64(json: JsonInput, suffix: String): String {
        val encoded = File.createTempFile("image-", ".encoded", directory)
        try {
            encoded.bufferedWriter(Charsets.US_ASCII).use { out -> json.textChunks { out.write(it) } }
            return save(java.util.Base64.getMimeDecoder().wrap(encoded.inputStream()), suffix)
        } finally { encoded.delete() }
    }
    fun local(href: String): File? {
        val path = File(directory, href.substringBefore('#')).canonicalFile
        return path.takeIf { it.path.startsWith(directory.canonicalPath + File.separator) && it.isFile }
    }
}
