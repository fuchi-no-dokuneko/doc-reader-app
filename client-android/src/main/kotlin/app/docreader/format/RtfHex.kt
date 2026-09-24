package app.docreader.format

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class RtfHex(private val text: StringBuilder) {
    private val bytes=ByteArrayOutputStream()
    fun add(hex: String) { bytes.write(hex.toInt(16)) }
    fun flush(encoding: String) {
        if (bytes.size()==0) return
        text.append(bytes.toByteArray().toString(Charset.forName(encoding))); bytes.reset()
    }
}
