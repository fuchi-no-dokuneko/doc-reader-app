package app.docreader.pdf

class PdfFont(file: PdfFile, dict: PdfDict) {
    private val cmap = (file.resolve(dict["ToUnicode"]) as? PdfStream)?.let { PdfCMap(file.decoded(it).reader(Charsets.ISO_8859_1)) }
    private val descendant = dict["DescendantFonts"].array().firstOrNull()?.let(file::dict)
    private val first = dict.number("FirstChar")
    private val widths = file.resolve(dict["Widths"]).array()
    private val cidWidths = descendant?.get("W").array()
    private val defaultWidth = descendant?.number("DW",1000) ?: 500
    private val encoding = dict.name("Encoding")
    private val differences = file.dict(dict["Encoding"])["Differences"].array()
    fun decode(bytes: ByteArray): List<Pair<Int,String>> {
        cmap?.let { return it.decode(bytes) }
        if (encoding.startsWith("Identity")) return bytes.toList().chunked(2).map {
            val code = ((it[0].toInt() and 255) shl 8) or (it.getOrElse(1) { 0 }.toInt() and 255)
            code to code.toChar().toString()
        }
        val custom = hashMapOf<Int,String>(); var index = 0
        differences.forEach { if (it is PdfNumber) index = it.number.toInt()
            else if (it is PdfName) custom[index++] = PdfGlyphs.text(it.name) }
        return bytes.map { b -> val code = b.toInt() and 255
            code to (custom[code] ?: byteArrayOf(b).toString(java.nio.charset.Charset.forName(
                if (encoding == "MacRomanEncoding") "x-MacRoman" else "windows-1252"))) }
    }
    fun width(code: Int): Float {
        if (descendant == null) return (widths.getOrNull(code-first)?.number()?.toFloat() ?: defaultWidth.toFloat())/1000
        var p = 0
        while (p+1 < cidWidths.size) {
            val start = cidWidths[p++].number().toInt(); val next = cidWidths[p++]
            if (next is PdfArray) {
                if (code in start until start+next.values.size) return next.values[code-start].number().toFloat()/1000
            } else if (p < cidWidths.size) {
                val w = cidWidths[p++].number().toFloat()
                if (code in start..next.number().toInt()) return w/1000
            }
        }
        return defaultWidth/1000f
    }
}
