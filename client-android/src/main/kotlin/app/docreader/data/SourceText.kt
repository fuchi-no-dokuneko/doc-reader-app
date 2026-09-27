package app.docreader.data

import app.docreader.format.Encodings
import java.io.File
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction.REPORT

data class SourceText(val text: String,val encoding: String,val bom: Boolean) {
    // Keep existing CRLF/LF bytes, including mixed endings; never pretty-print source.
    val newline=if ("\r\n" in text) "\r\n" else "\n"
    fun encode(value: String): ByteArray {
        val encoder=Charset.forName(encoding).newEncoder()
            .onMalformedInput(REPORT).onUnmappableCharacter(REPORT)
        val buffer=encoder.encode(CharBuffer.wrap((if (bom) "\uFEFF" else "")+value))
        return ByteArray(buffer.remaining()).also(buffer::get)
    }
    companion object {
        fun read(file: File,encoding: String): SourceText {
            val (reader,detected)=Encodings.open(file,encoding)
            val raw=reader.use { it.readText() }
            return SourceText(raw.removePrefix("\uFEFF"),detected,raw.startsWith('\uFEFF'))
        }
    }
}
