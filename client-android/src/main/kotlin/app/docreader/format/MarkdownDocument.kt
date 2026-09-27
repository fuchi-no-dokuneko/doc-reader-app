package app.docreader.format

import app.docreader.domain.*
import java.io.Reader

object MarkdownDocument {
    fun parse(reader: Reader, sink: BlockSink, asset: (String) -> String = { it }) {
        val lines=MarkdownLines(sink,asset)
        var pending: String?=null
        var header: List<String>?=null
        fun cells(value: String): List<String> = value.trim().trim('|').split(Regex("(?<!\\\\)\\|"))
            .map { it.trim().replace("\\|","|") }
        fun row(text: String) {
            val values=cells(text)
            sink.emit(Block(BlockType.TABLE,values.joinToString("\t"),html=Markup.table(listOf(header!!,values))))
        }
        TextLines.read(reader) { text ->
            val trimmed=text.trim()
            when {
                lines.fence.isNotEmpty() || trimmed.startsWith("```") || trimmed.startsWith("~~~") -> {
                    pending?.let(lines::emit); pending=null; header=null; lines.emit(text)
                }
                header!=null && '|' in text && trimmed.isNotEmpty() -> row(text)
                pending!=null && '|' in pending!! && cells(text).all { it.matches(Regex(":?-{3,}:?")) } -> {
                    header=cells(pending!!); pending=null
                }
                pending!=null && trimmed.matches(Regex("[=-]{3,}")) -> {
                    val title=pending!!
                    sink.emit(Block(BlockType.HEADING,title,if (trimmed[0]=='=') 1 else 2,
                        html=Markup.inline(title),anchor=MarkdownLines.slug(title))); pending=null
                }
                else -> { header=null; pending?.let(lines::emit); pending=text }
            }
        }
        pending?.let(lines::emit)
    }
}
