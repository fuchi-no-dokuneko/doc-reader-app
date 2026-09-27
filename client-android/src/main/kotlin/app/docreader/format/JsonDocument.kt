package app.docreader.format

import app.docreader.domain.*
import java.io.Reader

object JsonDocument {
    fun parse(reader: Reader, sink: BlockSink) {
        JsonInput(reader).use { json ->
            var row = 0
            while (json.peek() != '\u0000') {
                if (row++ > 0) sink.emit(Block(BlockType.RULE))
                value(json, sink, "", 0)
            }
        }
    }
    private fun value(json: JsonInput, sink: BlockSink, prefix: String, depth: Int) {
        require(depth < 256) { "JSON nesting is too deep" }
        fun emit(text: String, fold: Int = 0) {
            sink.emit(Block(BlockType.CODE, "  ".repeat(depth) + text, depth, "json", fold = fold))
        }
        when (json.peek()) {
            '{' -> {
                emit(prefix + "{", 1)
                json.fields { key -> value(json, sink, "\"${escape(key)}\": ", depth+1) }
                emit("}")
            }
            '[' -> {
                emit(prefix + "[", 1)
                json.entries { value(json, sink, "", depth+1) }
                emit("]")
            }
            '"' -> {
                var first = true
                json.stringChunks { part ->
                    emit((if (first) prefix else "") + "\"${escape(part)}\""); first = false
                }
                if (first) emit(prefix + "\"\"")
            }
            else -> emit(prefix + json.literal())
        }
    }
    private fun escape(text: String) = text.replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
}
