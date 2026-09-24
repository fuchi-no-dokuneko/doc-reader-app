package app.docreader.pdf

object PdfTextDraw {
    fun show(value: PdfString, font: PdfFont, state: PdfTextState, page: PdfPageTransform): PdfText? {
            val decoded = font.decode(value.bytes); val combined = state.matrix.then(state.transform)
            val bounds = mutableListOf<PdfBox>(); val chars = StringBuilder(); var offset = 0f
            for ((code,letters) in decoded) {
                val advance = (font.width(code)*state.size+state.characterSpace+if (code == 32) state.wordSpace else 0f)*state.scale
                val a = combined.point(offset,state.rise-state.size*.2f)
                val b = combined.point(offset+advance,state.rise+state.size*.85f)
                val box = page.box(minOf(a.first,b.first),minOf(a.second,b.second),maxOf(a.first,b.first),maxOf(a.second,b.second))
                chars.append(letters); repeat(letters.length) { bounds += box }; offset += advance
            }
            state.advance(offset)
            return if (chars.isNotEmpty()) PdfText(chars.toString(),bounds) else null
        }
}
