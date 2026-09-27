package app.docreader.pdf

object PdfXref {
    fun load(file: PdfFile, offset: Long, seen: MutableSet<Long>) {
        if (!seen.add(offset)) return
        val lex = file.lexer(offset)
        val trailer: PdfDict
        if (lex.cursor.word() == "xref") {
            while (true) {
                val word = lex.cursor.word(); if (word == "trailer") break
                val start = word.toInt(); val count = lex.cursor.word().toInt(); require(count in 0..5_000_000)
                repeat(count) { n ->
                    val at = lex.cursor.word().toLong(); val generation = lex.cursor.word().toInt()
                    if (lex.cursor.word() == "n") file.refs.putIfAbsent(start+n,at to generation)
                }
            }
            trailer = lex.value() as PdfDict
        } else {
            val stream = file.objectAt(offset) as PdfStream; trailer = stream.dict
            val widths = trailer["W"].array().map { it.number().toInt() }
            require(widths.size == 3 && widths.all { it in 0..8 })
            val ranges = trailer["Index"].array().map { it.number().toInt() }
                .ifEmpty { listOf(0,trailer.number("Size")) }
            file.decoded(stream).use { input ->
                fun field(n: Int): Long { var value = 0L; repeat(n) { val b = input.read(); require(b >= 0); value = (value shl 8) or b.toLong() }; return value }
                for (i in ranges.indices step 2) repeat(ranges[i+1]) { index ->
                    val type = if (widths[0] == 0) 1 else field(widths[0]).toInt()
                    val a = field(widths[1]); val b = field(widths[2]).toInt()
                    if (type == 1) file.refs.putIfAbsent(ranges[i]+index,a to b)
                    if (type == 2) file.refs.putIfAbsent(ranges[i]+index,a to -b-1)
                }
            }
        }
        file.trailer = PdfDict(trailer.values+file.trailer.values)
        trailer["XRefStm"]?.let { load(file,it.number().toLong(),seen) }
        trailer["Prev"]?.let { load(file,it.number().toLong(),seen) }
    }
}
