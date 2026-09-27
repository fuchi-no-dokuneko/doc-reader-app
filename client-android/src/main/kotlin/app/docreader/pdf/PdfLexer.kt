package app.docreader.pdf


class PdfLexer(val cursor: PdfCursor) {
    fun value(depth: Int = 0): PdfValue {
        require(depth < 100) { "PDF nesting is too deep" }
        val c = cursor.space()
        return when (c.toChar()) {
            '/' -> PdfName(Regex("#([0-9A-Fa-f]{2})").replace(cursor.word()) { it.groupValues[1].toInt(16).toChar().toString() })
            '(' -> PdfStrings.literal(cursor)
            '[' -> {
                val values = mutableListOf<PdfValue>()
                while (true) {
                    val next = cursor.space(); if (next == ']'.code) break
                    require(next >= 0); cursor.unread(); values += value(depth+1)
                }; PdfArray(values)
            }
            '<' -> if (cursor.read() == '<'.code) {
                val dict = linkedMapOf<String,PdfValue>()
                while (true) {
                    val next = cursor.space()
                    if (next == '>'.code) { require(cursor.read() == '>'.code); break }
                    require(next == '/'.code); cursor.unread()
                    val name = value(depth+1) as PdfName; dict[name.name] = value(depth+1)
                }; PdfDict(dict)
            } else { cursor.unread(); PdfStrings.hex(cursor) }
            else -> {
                val word = cursor.word(c); val number = word.toDoubleOrNull()
                if (number == null) PdfWord(word) else {
                    val saved = cursor.position
                    val generation = cursor.word().toIntOrNull()
                    if (generation != null && cursor.word() == "R") PdfRef(number.toInt(),generation)
                    else { cursor.position = saved; PdfNumber(number) }
                }
            }
        }
    }
}
