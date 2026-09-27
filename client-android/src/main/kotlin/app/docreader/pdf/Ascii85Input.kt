package app.docreader.pdf

import java.io.InputStream

class Ascii85Input(private val input: InputStream) : InputStream() {
    private var bytes = byteArrayOf(); private var index = 0; private var done = false
    override fun read(): Int {
        if (index < bytes.size) return bytes[index++].toInt() and 255
        if (done) return -1
        var value = 0L; var count = 0
        while (count < 5) {
            val c = input.read()
            if (c < 0 || c == '~'.code) { done = true; break }
            if (c <= 32) continue
            if (c == 'z'.code && count == 0) { bytes = ByteArray(4); index = 1; return 0 }
            require(c in 33..117); value = value*85+c-33; count++
        }
        if (count == 0) return -1
        require(count > 1)
        val n = count
        while (count++ < 5) value = value*85+84
        bytes = ByteArray(if (n == 5) 4 else n-1) { (value shr (24-it*8)).toByte() }
        index = 0; return read()
    }
    override fun close() = input.close()
}
