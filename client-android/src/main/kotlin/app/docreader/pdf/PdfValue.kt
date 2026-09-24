package app.docreader.pdf

sealed interface PdfValue
data class PdfNumber(val number: Double) : PdfValue
data class PdfName(val name: String) : PdfValue
data class PdfString(val bytes: ByteArray) : PdfValue {
    fun text(): String = if (bytes.size >= 2 && bytes[0] == (-2).toByte() && bytes[1] == (-1).toByte())
        String(bytes,2,bytes.size-2,Charsets.UTF_16BE) else bytes.toString(Charsets.ISO_8859_1)
}
data class PdfArray(val values: List<PdfValue>) : PdfValue
data class PdfDict(val values: Map<String,PdfValue>) : PdfValue {
    operator fun get(key: String) = values[key]
    fun number(key: String, default: Int = 0) = (values[key] as? PdfNumber)?.number?.toInt() ?: default
    fun name(key: String) = (values[key] as? PdfName)?.name.orEmpty()
}
data class PdfRef(val id: Int, val generation: Int = 0) : PdfValue
data class PdfWord(val word: String) : PdfValue
data class PdfStream(val dict: PdfDict, val offset: Long, val length: Long, val id: Int = 0,
    val generation: Int = 0) : PdfValue
fun PdfValue?.number() = (this as? PdfNumber)?.number ?: 0.0
fun PdfValue?.array() = (this as? PdfArray)?.values.orEmpty()
