package app.docreader.pdf

import app.docreader.format.BinarySource
import java.io.File
import java.io.RandomAccessFile

object PdfStreamReader {
    fun read(file: PdfFile, streams: List<PdfStream>, action: (PdfLexer) -> Unit) {
        val temp = File.createTempFile("content-",".tmp",file.file.parentFile)
        try {
            temp.outputStream().use { out -> streams.forEach { stream ->
                file.decoded(stream).use { input -> input.copyTo(out,16384) }; out.write(10)
            } }
            RandomAccessFile(temp,"r").use { input ->
                val source = object : BinarySource {
                    override val size = input.length()
                    override fun read(position: Long, length: Int) = ByteArray(length).also { input.seek(position); input.readFully(it) }
                }
                action(PdfLexer(PdfCursor(source)))
            }
        } finally { temp.delete() }
    }
}
