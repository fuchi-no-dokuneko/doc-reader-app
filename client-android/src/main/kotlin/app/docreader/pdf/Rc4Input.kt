package app.docreader.pdf

import java.io.InputStream

class Rc4Input(private val input: InputStream, key: ByteArray) : InputStream() {
    val state = IntArray(256) { it }; var i = 0; var j = 0
    init { for (n in 0..255) { j = (j+state[n]+(key[n%key.size].toInt() and 255)) and 255
        val t = state[n]; state[n] = state[j]; state[j] = t }; j = 0 }
    override fun read(): Int {
        val b = input.read(); if (b < 0) return -1
        i = (i+1) and 255; j = (j+state[i]) and 255
        val t = state[i]; state[i] = state[j]; state[j] = t
        return b xor state[(state[i]+state[j]) and 255]
    }
    override fun close() = input.close()
}
