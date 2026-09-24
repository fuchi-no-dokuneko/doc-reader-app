package app.docreader.format

import app.docreader.domain.*
import java.io.Reader

object SourceParser {
    fun parse(reader: Reader, language: String, sink: BlockSink, plain: Boolean = false) {
        var depth = 0; var section = false
        TextLines.read(reader) { line ->
            val trimmed = line.trimStart()
            val indent = line.takeWhile { it == ' ' || it == '\t' }.sumOf { if (it == '\t') 4 else 1 } / 2
            val closing = trimmed.firstOrNull() in listOf('}', ']', ')')
            val isSection=trimmed.matches(Regex("\\[[^]{}]+]")) && language in setOf("ini","cfg","config","conf","toml")
            val level = if (isSection) 0 else maxOf(indent, depth - if (closing) 1 else 0,
                if (section) 1 else 0).coerceAtLeast(0)
            val opens = braceDelta(line)
            val fold = if (opens > 0 || trimmed.endsWith(':') || isSection ||
                trimmed.matches(Regex(".*:\\s*[>|][-+0-9]*")) || trimmed.endsWith(" then") || trimmed.endsWith(" do")) 1 else 0
            if (isSection) section=true
            sink.emit(Block(if (plain) BlockType.TEXT else BlockType.CODE,
                line, level = level, language = language, fold = fold))
            depth = (depth + opens).coerceAtLeast(0)
        }
    }
    private fun braceDelta(line: String): Int {
        var quote = ' '; var escaped = false; var delta = 0
        for (char in line) {
            if (escaped) { escaped = false; continue }
            if (char == '\\' && quote != ' ') { escaped = true; continue }
            if (quote != ' ') { if (char == quote) quote = ' '; continue }
            if (char == '\'' || char == '"' || char == '`') quote = char
            else if (char == '{' || char == '[' || char == '(') delta++
            else if (char == '}' || char == ']' || char == ')') delta--
        }
        return delta
    }
}
