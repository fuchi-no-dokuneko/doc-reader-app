package app.docreader.pdf

object PdfGlyphs {
    private val punctuation = mapOf("space" to " ","period" to ".","comma" to ",","colon" to ":",
        "semicolon" to ";","hyphen" to "-","endash" to "–","emdash" to "—","parenleft" to "(",
        "parenright" to ")","slash" to "/","backslash" to "\\","quotedbl" to "\"","quotesingle" to "'",
        "quoteleft" to "‘","quoteright" to "’","quotedblleft" to "“","quotedblright" to "”",
        "bullet" to "•","ellipsis" to "…","fi" to "fi","fl" to "fl","ff" to "ff","ffi" to "ffi",
        "exclam" to "!","question" to "?","ampersand" to "&","at" to "@","numbersign" to "#",
        "percent" to "%","dollar" to "$","plus" to "+","equal" to "=","underscore" to "_")
    fun text(name: String): String {
        if (name.length == 1) return name
        punctuation[name]?.let { return it }
        val digits = listOf("zero","one","two","three","four","five","six","seven","eight","nine")
        if (name in digits) return digits.indexOf(name).toString()
        if (name.startsWith("uni")) return runCatching { name.drop(3).chunked(4).map { it.toInt(16).toChar() }.joinToString("") }.getOrDefault("�")
        if (name.startsWith('u')) return runCatching { String(Character.toChars(name.drop(1).toInt(16))) }.getOrDefault("�")
        val accents = mapOf("acute" to '\u0301',"grave" to '\u0300',"circumflex" to '\u0302',"tilde" to '\u0303',"dieresis" to '\u0308',"cedilla" to '\u0327',"ring" to '\u030a')
        accents[name.drop(1)]?.let { return java.text.Normalizer.normalize("${name[0]}$it",java.text.Normalizer.Form.NFC) }
        return "�"
    }
}
