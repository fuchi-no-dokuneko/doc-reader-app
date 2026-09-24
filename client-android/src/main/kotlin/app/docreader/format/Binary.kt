package app.docreader.format

import java.io.InputStream

object Binary {
    fun u16(b: ByteArray, i: Int) = (b[i].toInt() and 255) or ((b[i + 1].toInt() and 255) shl 8)
    fun i32(b: ByteArray, i: Int) = u16(b, i) or (u16(b, i + 2) shl 16)
    fun i64(b: ByteArray, i: Int) = (i32(b, i).toLong() and 0xffffffffL) or (i32(b, i + 4).toLong() shl 32)
    fun double(b: ByteArray, i: Int) = Double.fromBits(i64(b, i))
}
interface BinarySource {
    val size: Long
    fun read(position: Long, length: Int): ByteArray
    fun stream(start: Long = 0, length: Long = size - start): InputStream = object : InputStream() {
        var position = start
        val end = (start + length).coerceAtMost(size)
        override fun read(): Int = if (position >= end) -1 else read(position++, 1)[0].toInt() and 255
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            if (position >= end) return -1
            val n = minOf(len.toLong(), end - position).toInt()
            read(position, n).copyInto(b, off); position += n; return n
        }
    }
}
