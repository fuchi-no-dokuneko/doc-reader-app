package app.docreader.format

import java.io.Reader

object HtmlTokens {
    data class Token(val raw: String, val tag: String = "", val closing: Boolean = false,
        val attrs: Map<String, String> = emptyMap())
    fun read(reader: Reader, emit: (Token) -> Unit) {
        val text = StringBuilder()
        while (true) {
            val value = reader.read(); if (value < 0) break
            if (value.toChar() != '<') {
                text.append(value.toChar())
                if (text.length >= 4096) { emit(Token(text.toString())); text.setLength(0) }
                continue
            }
            if (text.isNotEmpty()) { emit(Token(text.toString())); text.setLength(0) }
            val raw = StringBuilder("<"); var quote = ' '
            while (raw.length < 32768) {
                val next = reader.read(); if (next < 0) break
                val char = next.toChar(); raw.append(char)
                if (quote != ' ') { if (char == quote) quote = ' ' }
                else if (char == '\'' || char == '"') quote = char
                else if (char == '>') break
            }
            val valueText = raw.toString()
            if (valueText.startsWith("<!") || valueText.startsWith("<?")) continue
            val closing = valueText.startsWith("</")
            val name = valueText.removePrefix("<").removePrefix("/")
                .takeWhile { it.isLetterOrDigit() || it == ':' }.lowercase()
            val attrs = Regex("([\\w:-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))")
                .findAll(valueText).associate { it.groupValues[1].lowercase() to
                    it.groupValues.drop(2).firstOrNull(String::isNotEmpty).orEmpty() }
            emit(Token(valueText, name.substringAfter(':'), closing, attrs))
        }
        if (text.isNotEmpty()) emit(Token(text.toString()))
    }
    fun text(value: String): String = Regex("&(#x[0-9a-fA-F]+|#\\d+|amp|lt|gt|quot|apos|nbsp);").replace(value) {
        when (val name = it.groupValues[1]) {
            "amp" -> "&"; "lt" -> "<"; "gt" -> ">"; "quot" -> "\""; "apos" -> "'"; "nbsp" -> " "
            else -> runCatching { String(Character.toChars(if (name.startsWith("#x")) name.drop(2).toInt(16) else name.drop(1).toInt())) }.getOrDefault(it.value)
        }
    }
}
