package app.docreader.format

import app.docreader.domain.*

class HtmlBlocks(private val sink: BlockSink) {
    val text=StringBuilder(); val html=StringBuilder()
    var tag="p"; var style=""; var anchor=""
    fun flush() {
        if (text.isNotBlank()) {
            val level=tag.removePrefix("h").toIntOrNull() ?: 0
            val kind=when { level in 1..6 -> BlockType.HEADING; tag=="pre" -> BlockType.CODE; else -> BlockType.HTML }
            sink.emit(Block(kind,HtmlTokens.text(text.toString()).trim(),level,html=html.toString(),anchor=anchor,style=style))
        } else if (anchor.isNotBlank()) sink.emit(Block(anchor=anchor))
        text.setLength(0); html.setLength(0); anchor=""
    }
}
