package app.docreader.format

import java.io.PushbackReader

internal object JsonStrings {
    fun read(reader: PushbackReader, emit: (String) -> Unit) {
        val text = StringBuilder()
        while (true) {
            val value = reader.read(); require(value >= 0) { "Unfinished JSON string" }
            val char = value.toChar()
            if (char == '"') break
            if (char == '\\') {
                val next = reader.read().toChar()
                text.append(when (next) {
                    'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; 'b' -> '\b'; 'f' -> '\u000c'
                    'u' -> CharArray(4).also { for (i in it.indices) it[i] = reader.read().toChar() }
                        .concatToString().toInt(16).toChar()
                    '"', '\\', '/' -> next
                    else -> error("Invalid JSON escape")
                })
            } else text.append(char)
            if (text.length >= 4096 && !Character.isHighSurrogate(text.last())) {
                emit(text.toString()); text.setLength(0)
            }
        }
        if (text.isNotEmpty()) emit(text.toString())
    }
}
