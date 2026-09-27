package app.docreader.format

import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

object Encodings {
    val choices = listOf("Auto", "UTF-8", "UTF-16LE", "UTF-16BE", "GBK", "GB2312", "GB18030", "Big5", "windows-1252")
    fun detect(file: File): String {
        val bytes = file.inputStream().use { it.readHead(65536) }
        if (bytes.take(3) == listOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())) return "UTF-8"
        if (bytes.take(2) == listOf(0xff.toByte(), 0xfe.toByte())) return "UTF-16LE"
        if (bytes.take(2) == listOf(0xfe.toByte(), 0xff.toByte())) return "UTF-16BE"
        if (bytes.size > 4 && bytes.take(128).count { it == 0.toByte() } > bytes.take(128).size / 4)
            return if (bytes[0] == 0.toByte()) "UTF-16BE" else "UTF-16LE"
        val samples = mutableListOf(bytes)
        if (bytes.all { it >= 0 } && file.length() > 65536) java.io.RandomAccessFile(file, "r").use {
            for (offset in listOf(file.length()/2, (file.length()-65536).coerceAtLeast(0))) {
                it.seek(offset); val sample = ByteArray(minOf(65536L, file.length()-offset).toInt())
                it.readFully(sample); samples += sample
            }
        }
        if (samples.withIndex().all { decodeSample(it.value, "UTF-8", it.index > 0) != null }) return "UTF-8"
        return listOf("GB18030", "GBK", "GB2312", "Big5").maxByOrNull { name ->
            samples.withIndex().sumOf { (i, sample) -> decodeSample(sample, name, i > 0)?.let(ChineseScore::score) ?: -100000 }
        } ?: "UTF-8"
    }
    fun open(file: File, requested: String): Pair<java.io.BufferedReader, String> {
        val name = if (requested == "Auto") detect(file) else requested
        val decoder = Charset.forName(name).newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return InputStreamReader(file.inputStream(), decoder).buffered(16384) to name
    }
    private fun decodeSample(bytes: ByteArray, name: String, middle: Boolean): String? {
        for (skip in 0..if (middle) minOf(3,bytes.size) else 0) {
            for (trim in 0..minOf(3, bytes.size-skip)) try {
                return Charset.forName(name).newDecoder().decode(ByteBuffer.wrap(bytes, skip, bytes.size-trim-skip)).toString()
            } catch (_: java.nio.charset.CharacterCodingException) { }
        }
        return null
    }
    private fun java.io.InputStream.readHead(limit: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(4096)
        while (out.size() < limit) {
            val count = read(buffer, 0, minOf(buffer.size, limit-out.size()))
            if (count < 0) break
            out.write(buffer, 0, count)
        }
        return out.toByteArray()
    }
}
