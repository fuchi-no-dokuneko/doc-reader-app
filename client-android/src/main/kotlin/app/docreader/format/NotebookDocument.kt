package app.docreader.format

import app.docreader.data.DiskBlocks
import app.docreader.domain.*
import java.io.File
import java.io.Reader

object NotebookDocument {
    fun parse(reader: Reader, sink: BlockSink, assets: AssetFiles) {
        JsonInput(reader).use { json -> json.fields { key ->
            if (key != "cells") json.skip()
            else json.entries { index -> cell(json, index, sink, assets) }
        } }
    }
    private fun cell(json: JsonInput, index: Int, sink: BlockSink, assets: AssetFiles) {
        val temporary = File(assets.directory, "cell-$index").apply { mkdirs() }
        val source = File(temporary, "source.txt")
        var kind = "code"; var language = "python"
        val attachments=mutableMapOf<String,String>()
        try {
            DiskBlocks(temporary, true).use { outputs ->
                json.fields { key -> when (key) {
                    "cell_type" -> kind = json.string()
                    "source" -> source.bufferedWriter().use { out -> json.textChunks(out::write) }
                    "outputs" -> json.entries { NotebookOutput.parse(json, outputs, assets) }
                    "attachments" -> json.fields { name -> NotebookOutput.data(json,object : BlockSink {
                        override fun emit(block: Block) { if (block.type==BlockType.IMAGE) attachments[name]=block.asset }
                    },assets) }
                    "metadata" -> json.fields { if (it == "language") language = json.string() else json.skip() }
                    else -> json.skip()
                } }
            }
            sink.emit(Block(BlockType.HEADING, "${if (kind == "markdown") "Markdown" else "Code"} cell ${index+1}", 2))
            if (source.exists()) source.bufferedReader().use {
                if (kind == "markdown") MarkdownDocument.parse(it, sink) { href ->
                    attachments[href.removePrefix("attachment:")] ?: href
                }
                else if (kind == "raw") SourceParser.parse(it,"",sink,true)
                else SourceParser.parse(it, language, sink)
            }
            DiskBlocks(temporary).use { output ->
                if (output.count > 0) sink.emit(Block(BlockType.TEXT, "Saved output"))
                for (i in 0 until output.count) sink.emit(output.block(i))
            }
        } finally { temporary.deleteRecursively() }
    }
}
