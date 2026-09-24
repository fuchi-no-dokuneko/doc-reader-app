package app.docreader.pdf

import java.io.InputStream

class LzwInput(private val input: InputStream, private val early: Int) : InputStream() {
    private val table = arrayOfNulls<ByteArray>(4096)
    private var bits = 0; private var buffer = 0; private var width = 9; private var next = 258
    private var previous: ByteArray? = null; private var current = byteArrayOf(); private var position = 0
    private var done = false
    init { repeat(256) { table[it] = byteArrayOf(it.toByte()) } }
    private fun code(): Int {
        while (bits < width) { val b = input.read(); if (b < 0) return 257; buffer = (buffer shl 8) or b; bits += 8 }
        bits -= width; return (buffer ushr bits) and ((1 shl width)-1)
    }
    override fun read(): Int {
        while (position >= current.size) {
            if (done) return -1
            val code = code()
            if (code == 257) { done = true; return -1 }
            if (code == 256) { next = 258; width = 9; previous = null; continue }
            val last = previous
            current = if (code < next) table[code] ?: error("Invalid PDF LZW code")
                else if (code == next && last != null) last+last[0] else error("Invalid PDF LZW code")
            if (last != null && next < 4096) {
                table[next++] = last+current[0]
                if (next+early == 1 shl width && width < 12) width++
            }
            previous = current; position = 0
        }
        return current[position++].toInt() and 255
    }
    override fun close() = input.close()
}
