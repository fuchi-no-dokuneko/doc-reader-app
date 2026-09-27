package app.docreader.render

import android.text.*
import android.text.style.ForegroundColorSpan

object Syntax {
    class Ink(color: Int): ForegroundColorSpan(color)
    fun color(text: String,dark: Boolean,language: String=""): SpannableString =
        SpannableString(text).also { apply(it,SyntaxTokens.scan(text,language),dark) }
    fun apply(text: Spannable,tokens: List<SyntaxToken>,dark: Boolean) {
        text.getSpans(0,text.length,Ink::class.java).forEach(text::removeSpan)
        val light=intArrayOf(0xff7643a7.toInt(),0xff287543.toInt(),0xff687482.toInt(),0xff9b5312.toInt(),0xff176e8b.toInt())
        val night=intArrayOf(0xffc1a6ff.toInt(),0xffa8d6a1.toInt(),0xff92a0b5.toInt(),0xffffca85.toInt(),0xff81cde7.toInt())
        tokens.forEach { token ->
            if (token.end<=text.length) text.setSpan(Ink((if (dark) night else light)[token.kind]),
                token.start,token.end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
