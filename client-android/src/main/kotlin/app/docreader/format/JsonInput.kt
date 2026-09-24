package app.docreader.format

import java.io.PushbackReader
import java.io.Reader

class JsonInput(reader: Reader) : AutoCloseable {
    private val input = PushbackReader(reader, 2)
    fun peek(): Char {
        var c = input.read()
        while (c >= 0 && c.toChar().isWhitespace()) c = input.read()
        if (c >= 0) input.unread(c)
        return if (c < 0) '\u0000' else c.toChar()
    }
    fun take(char: Char) { require(peek() == char) { "Expected $char in JSON" }; input.read() }
    fun fields(read: (String) -> Unit) {
        take('{')
        if (peek() != '}') while (true) {
            val key = string(4096); take(':'); read(key)
            if (peek() != ',') break
            take(',')
        }
        take('}')
    }
    fun entries(read: (Int) -> Unit) {
        take('['); var index = 0
        if (peek() != ']') while (true) {
            read(index++)
            if (peek() != ',') break
            take(',')
        }
        take(']')
    }
    fun stringChunks(emit: (String) -> Unit) { take('"'); JsonStrings.read(input, emit) }
    fun string(limit: Int = 65536): String {
        val text = StringBuilder()
        stringChunks { require(text.length + it.length <= limit) { "JSON metadata is too large" }; text.append(it) }
        return text.toString()
    }
    fun textChunks(emit: (String) -> Unit) {
        if (peek() == '[') entries { stringChunks(emit) } else stringChunks(emit)
    }
    fun literal(): String {
        peek(); val text = StringBuilder()
        while (true) {
            val c = input.read()
            if (c < 0) break
            if (c.toChar() in ",]}" || c.toChar().isWhitespace()) { input.unread(c); break }
            require(text.length < 1024) { "Invalid JSON value" }; text.append(c.toChar())
        }
        require(text.isNotEmpty()) { "Expected JSON value" }
        return text.toString()
    }
    fun skip(depth: Int = 0) {
        require(depth < 256) { "JSON nesting is too deep" }
        when (peek()) {
            '{' -> fields { skip(depth+1) }; '[' -> entries { skip(depth+1) }
            '"' -> stringChunks { }; else -> literal()
        }
    }
    override fun close() = input.close()
}
