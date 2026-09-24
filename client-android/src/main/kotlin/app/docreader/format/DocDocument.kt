package app.docreader.format

import app.docreader.domain.*
import java.io.File
import java.nio.charset.Charset

object DocDocument {
    fun parse(file: File, sink: BlockSink) = CfbFile(file).use { cfb ->
        val word = cfb.stream("WordDocument") ?: error("Missing Word document")
        val fib = word.read(0, minOf(512L, word.size).toInt())
        require(fib.size >= 426) { "Unsupported old Word document" }
        val flags = Binary.u16(fib, 10)
        require(flags and 0x100 == 0) { "Save this encrypted Word file without a password first" }
        val table = cfb.stream(if (flags and 0x200 != 0) "1Table" else "0Table") ?: error("Missing Word table")
        var p = Binary.i32(fib, 418).toLong(); val end = p + Binary.i32(fib, 422)
        while (p < end && table.read(p, 1)[0].toInt() == 1) p += 3 + Binary.u16(table.read(p+1,2),0)
        require(table.read(p,1)[0].toInt() == 2) { "Missing Word text pieces" }
        val length = Binary.i32(table.read(p+1,4),0); p += 5
        val count = (length - 4) / 12
        require(count in 0..1_000_000)
        val text = StringBuilder(); var field = 0
        fun flush() { if (text.isNotEmpty()) sink.emit(Block(text = text.toString())); text.setLength(0) }
        for (i in 0 until count) {
            val cp = table.read(p+i*4,8); val chars = Binary.i32(cp,4)-Binary.i32(cp,0)
            val fc = Binary.i32(table.read(p+(count+1)*4+i*8+2,4),0)
            val compressed = fc and 0x40000000 != 0
            val start = (fc and 0x3fffffff).toLong() / if (compressed) 2 else 1
            val charset = if (compressed) Charset.forName("windows-1252") else Charsets.UTF_16LE
            word.stream(start, chars.toLong() * if (compressed) 1 else 2).reader(charset).use { reader ->
                val buffer = CharArray(4096)
                while (true) {
                    val n = reader.read(buffer); if (n < 0) break
                    for (j in 0 until n) when (val c = buffer[j]) {
                        '\u0013' -> field++; '\u0014' -> field = (field-1).coerceAtLeast(0)
                        '\u0015' -> field = 0
                        '\r', '\u000c', '\u0007' -> flush()
                        else -> if (field == 0 && (c >= ' ' || c == '\t')) text.append(c)
                    }
                    if (text.length >= 4096) flush()
                }
            }
        }
        flush()
    }
}
