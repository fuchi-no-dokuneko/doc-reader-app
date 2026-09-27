package app.docreader.render

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

object Syntax {
    private val keywords = ("abstract alignas and as assert async await auto begin bool boolean break byte case catch char class " +
        "const constexpr continue data def default defer del do double elif else end enum except export extends extern false final finally " +
        "float fn for foreach from fun function go goto if impl import in inline int interface internal is lambda let local long match " +
        "module namespace new nil none null object of or override package pass private protected protocol pub public raise readonly " +
        "record ref require return sealed self short sizeof static string struct super suspend switch synchronized template then this " +
        "throw throws trait true try type typedef typeof union unless unsigned use using val var virtual void volatile when where while with yield " +
        "select insert update delete create alter drop table into values join left right inner outer on group order by limit having distinct " +
        "set primary key foreign references not exists count sum asc desc union all").split(' ').toSet()
    private val tokens = Regex("(?:\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|`[^`]*`)|(?://.*|#.*|--.*|/\\*.*?\\*/)|\\b(?:0[xX][0-9a-fA-F]+|\\d+(?:\\.\\d+)?)\\b|[\\p{L}_][\\p{L}\\p{N}_]*|[{}\\[\\]():=]")
    fun color(text: String, dark: Boolean): SpannableString {
        val out = SpannableString(text)
        tokens.findAll(text).forEach { match ->
            val value = match.value
            val color = when {
                value.first() in "\"'`" -> if (dark) 0xffa8d6a1 else 0xff287543
                value.startsWith("//") || value.startsWith('#') || value.startsWith("--") || value.startsWith("/*") -> if (dark) 0xff92a0b5 else 0xff687482
                value.first().isDigit() -> if (dark) 0xffffca85 else 0xff9b5312
                value.lowercase() in keywords -> if (dark) 0xffc1a6ff else 0xff7643a7
                value.length == 1 && value[0] in "{}[]():=" -> if (dark) 0xff81cde7 else 0xff176e8b
                else -> return@forEach
            }
            out.setSpan(ForegroundColorSpan(color.toInt()),match.range.first,match.range.last+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return out
    }
}
