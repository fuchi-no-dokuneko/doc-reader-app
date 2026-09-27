package app.docreader.pdf

object PdfSerialize {
    fun value(value: PdfValue): String = when (value) {
        is PdfNumber -> if (value.number == value.number.toLong().toDouble()) value.number.toLong().toString() else value.number.toString()
        is PdfName -> "/"+value.name.toByteArray(Charsets.ISO_8859_1).joinToString("") {
            val c = (it.toInt() and 255).toChar()
            if (c > ' ' && c < 127.toChar() && c !in "#()<>[]{}/%") c.toString() else "#%02x".format(it)
        }
        is PdfString -> "<"+value.bytes.joinToString("") { "%02x".format(it) }+">"
        is PdfArray -> value.values.joinToString(" ","[","]",transform=::value)
        is PdfDict -> value.values.entries.joinToString(" ","<<",">>") { value(PdfName(it.key))+" "+value(it.value) }
        is PdfRef -> "${value.id} ${value.generation} R"
        is PdfWord -> value.word
        is PdfStream -> error("Streams are serialized separately")
    }
}
