package app.docreader.pdf

import app.docreader.format.BinarySource
import java.io.File
import java.io.RandomAccessFile

object PdfObjects {
    fun compressed(file: PdfFile, container: Int, index: Int): PdfValue {
        val stream = file.get(container) as? PdfStream ?: error("Missing PDF object stream")
        val temp = File.createTempFile("objects-",".tmp",file.file.parentFile)
        try {
            file.decoded(stream).use { input -> temp.outputStream().use { input.copyTo(it,16384) } }
            RandomAccessFile(temp,"r").use { input ->
                val source = object : BinarySource {
                    override val size = input.length()
                    override fun read(position: Long, length: Int) = ByteArray(length).also { input.seek(position); input.readFully(it) }
                }
                val cursor = PdfCursor(source); var offset = 0L
                repeat(index+1) { cursor.word(); offset = cursor.word().toLong() }
                cursor.position = stream.dict.number("First") + offset
                return PdfLexer(cursor).value()
            }
        } finally { temp.delete() }
    }
}
