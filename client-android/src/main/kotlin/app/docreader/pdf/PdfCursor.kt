package app.docreader.pdf

import app.docreader.format.BinarySource

class PdfCursor(private val source: BinarySource, var position: Long = 0) {
    private var base = -1L
    private var buffer = byteArrayOf()
    fun read(): Int {
        if (position >= source.size) return -1
        if (position !in base until base+buffer.size) {
            base = position; buffer = source.read(base,minOf(8192,source.size-base).toInt())
        }
        return buffer[(position++-base).toInt()].toInt() and 255
    }
    fun unread() { position = (position-1).coerceAtLeast(0) }
    fun space(): Int {
        while (true) {
            val c = read()
            if (c == '%'.code) { while (true) { val n = read(); if (n < 0 || n == 10 || n == 13) break }; continue }
            if (c < 0 || c > 32) return c
        }
    }
    fun word(first: Int = space()): String {
        if (first < 0) return ""
        val text = StringBuilder().append(first.toChar())
        while (true) {
            val c = read(); if (c < 0) break
            if (c <= 32 || c.toChar() in "()<>[]{}/%") { unread(); break }
            text.append(c.toChar()); require(text.length <= 65536) { "Invalid PDF token" }
        }
        return text.toString()
    }
}
