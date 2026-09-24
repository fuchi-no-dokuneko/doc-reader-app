package app.docreader.pdf

import java.io.*
import java.util.zip.InflaterInputStream

object PdfFilters {
    fun decode(input: InputStream, dict: PdfDict): InputStream {
        var stream = input
        val filters = if (dict["Filter"] is PdfArray) dict["Filter"].array() else listOfNotNull(dict["Filter"])
        val params = if (dict["DecodeParms"] is PdfArray) dict["DecodeParms"].array() else listOfNotNull(dict["DecodeParms"])
        filters.forEachIndexed { i,value ->
            val name = (value as? PdfName)?.name
            stream = when (name) {
                "FlateDecode", "Fl" -> InflaterInputStream(stream)
                "ASCIIHexDecode", "AHx" -> HexInput(stream)
                "ASCII85Decode", "A85" -> Ascii85Input(stream)
                "LZWDecode", "LZW" -> LzwInput(stream,(params.getOrNull(i) as? PdfDict)?.number("EarlyChange",1) ?: 1)
                "Crypt" -> stream
                else -> error("Unsupported PDF text filter: $name")
            }
            val options = params.getOrNull(i) as? PdfDict
            if (options != null && options.number("Predictor",1) > 1) stream = PredictorInput(stream,options)
        }
        return stream
    }
}
private class HexInput(private val input: InputStream) : InputStream() {
    var done = false
    override fun read(): Int {
        fun nibble(): Int { while (true) { val c = input.read(); if (c < 0 || c == '>'.code) return -1
            if (c > 32) return c.toChar().digitToInt(16) } }
        if (done) return -1
        val a = nibble(); if (a < 0) { done = true; return -1 }
        val b = nibble(); if (b < 0) done = true
        return a*16+b.coerceAtLeast(0)
    }
    override fun close() = input.close()
}
