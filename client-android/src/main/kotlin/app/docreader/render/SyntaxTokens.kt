package app.docreader.render

data class SyntaxToken(val start: Int,val end: Int,val kind: Int)

object SyntaxTokens {
    private val number=Regex("(?:0[xX][0-9a-fA-F]+|0[bB][01]+|\\d+(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)")
    fun scan(text: String,language: String,active: () -> Boolean = { true }): List<SyntaxToken> {
        val lang=language.lowercase(); val out=ArrayList<SyntaxToken>(); var i=0
        while (i<text.length && active()) {
            val start=i; val char=text[i]
            val comment=SyntaxGrammar.comment(text,i,lang)
            val kind=when {
                comment!=null -> { i=comment; 2 }
                char in "\"'`" -> { i=SyntaxGrammar.quoted(text,i); 1 }
                char.isDigit() -> { i=number.matchAt(text,i)?.range?.last?.plus(1) ?: i+1; 3 }
                char.isLetter() || char in "_$" -> {
                    while (i<text.length && (text[i].isLetterOrDigit() || text[i] in "_$")) i++
                    if (text.substring(start,i).lowercase() in SyntaxWords.words) 0 else -1
                }
                char in "{}[]():=+*/%!&|<>;.,?-" -> { i++; 4 }
                else -> { i++; -1 }
            }
            if (kind>=0) out+=SyntaxToken(start,i,kind)
        }
        return out
    }
}
