package app.docreader.format

import app.docreader.domain.*
import java.io.File
import java.util.zip.ZipFile
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

object DocxDocument {
    fun parse(file: File, sink: BlockSink) = ZipFile(file).use { zip ->
        val parts = listOf("word/document.xml", "word/footnotes.xml", "word/endnotes.xml")
        for (part in parts) {
            val entry = zip.getEntry(part) ?: continue
            val rels = OfficeXml.relationships(zip, part)
            var inside = false; var level = 0; var table = false
            val text = StringBuilder(); val row = mutableListOf<String>()
            val cell = StringBuilder()
            val images = mutableListOf<String>()
            OfficeXml.parse(zip.getInputStream(entry), object : DefaultHandler() {
                override fun startElement(uri: String?, local: String, name: String?, attrs: Attributes) {
                    when (local) {
                        "p" -> { text.setLength(0); level = 0 }
                        "pStyle" -> level = (attrs.getValue(uri, "val") ?: attrs.getValue("w:val")).orEmpty()
                            .removePrefix("Heading").removePrefix("heading").toIntOrNull() ?: 0
                        "t" -> inside = true
                        "tab" -> text.append('\t')
                        "br" -> text.append('\n')
                        "tbl" -> table = true
                        "tr" -> row.clear()
                        "tc" -> cell.setLength(0)
                        "blip" -> {
                            val id = attrs.getValue("http://schemas.openxmlformats.org/officeDocument/2006/rels", "embed")
                            rels[id]?.let { images += "zip:${file.path}!/$it" }
                        }
                    }
                }
                override fun characters(c: CharArray, start: Int, len: Int) {
                    if (inside) text.append(c, start, len)
                    if (!table && text.length >= 8192) paragraph()
                }
                fun paragraph() {
                    if (text.isNotEmpty()) sink.emit(Block(if (level > 0) BlockType.HEADING else BlockType.TEXT,
                        text.toString(), level))
                    text.setLength(0)
                }
                override fun endElement(uri: String?, local: String, name: String?) {
                    when (local) {
                        "t" -> inside = false
                        "p" -> if (!table) paragraph() else cell.append(text).append('\n')
                        "tc" -> { row += cell.toString().trim() }
                        "tr" -> sink.emit(Block(BlockType.TABLE, row.joinToString("\t"), html = Markup.table(listOf(row), false)))
                        "tbl" -> table = false
                    }
                    if (local == "p") { images.forEach { sink.emit(Block(BlockType.IMAGE, asset = it)) }; images.clear() }
                }
            })
        }
    }
}
