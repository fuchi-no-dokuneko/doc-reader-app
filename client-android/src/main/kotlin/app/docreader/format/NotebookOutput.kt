package app.docreader.format

import app.docreader.domain.*
import java.io.File

object NotebookOutput {
    fun parse(json: JsonInput, sink: BlockSink, assets: AssetFiles) {
        json.fields { key -> when (key) {
            "text", "traceback" -> json.textChunks { part ->
                TextLines.read(part.reader()) { sink.emit(Block(BlockType.CODE, stripAnsi(it), language = "output")) }
            }
            "ename", "evalue" -> sink.emit(Block(BlockType.TEXT, json.string()))
            "data" -> data(json, sink, assets)
            else -> json.skip()
        } }
    }
    fun data(json: JsonInput, sink: BlockSink, assets: AssetFiles) {
        var chosen: Block? = null; var rank = 0
        val plain = File.createTempFile("output-", ".txt", assets.directory)
        val html = File.createTempFile("output-", ".html", assets.directory)
        try {
            json.fields { mime -> when (mime) {
                "image/png", "image/jpeg", "image/gif" -> {
                    val path = assets.base64(json, mime.substringAfter('/'))
                    chosen = Block(BlockType.IMAGE, "Notebook output", asset = path); rank = 3
                }
                "image/svg+xml" -> {
                    val svg = File.createTempFile("output-", ".svg", assets.directory)
                    svg.bufferedWriter().use { out -> json.textChunks(out::write) }
                    if (rank < 3) { chosen = Block(BlockType.IMAGE, "Notebook output", asset = svg.path); rank = 3 }
                }
                "text/html" -> { html.bufferedWriter().use { out -> json.textChunks(out::write) }; if (rank < 2) rank = 2 }
                "text/plain" -> { plain.bufferedWriter().use { out -> json.textChunks(out::write) }; if (rank < 1) rank = 1 }
                else -> json.skip()
            } }
            when (rank) {
                3 -> chosen?.let(sink::emit)
                2 -> html.bufferedReader().use { HtmlDocument.parse(it, sink) }
                1 -> plain.bufferedReader().use { SourceParser.parse(it, "output", sink, true) }
            }
        } finally { plain.delete(); html.delete() }
    }
    private fun stripAnsi(text: String) = text.replace(Regex("\u001b\\[[0-9;]*[A-Za-z]"), "")
}
