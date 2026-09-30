package app.docreader.data

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object FileHash {
    fun of(file: File): String = file.inputStream().use(::read)
    fun read(input: InputStream): String {
        val digest=MessageDigest.getInstance("SHA-256")
        val buffer=ByteArray(65536)
        while (true) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            val n=input.read(buffer); if (n<0) break
            digest.update(buffer,0,n)
        }
        return hex(digest.digest())
    }
    fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
}
