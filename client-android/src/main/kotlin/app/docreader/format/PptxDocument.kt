package app.docreader.format

import app.docreader.domain.*
import java.io.File
import java.util.zip.ZipFile
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

object PptxDocument {
    fun parse(file: File, sink: BlockSink) = ZipFile(file).use { zip ->
        val presentation = "ppt/presentation.xml"
        val rels = OfficeXml.relationships(zip, presentation)
        val xml = XmlFiles.read(zip.getInputStream(zip.getEntry(presentation)))
        for ((index, slide) in XmlFiles.elements(xml, "sldId").withIndex()) {
            val path = rels[slide.getAttribute("r:id")] ?: continue
            val relationships = OfficeXml.relationships(zip, path)
            sink.emit(Block(BlockType.HEADING, "Slide ${index+1}", 1))
            val text = StringBuilder(); var capture = false
            OfficeXml.parse(zip.getInputStream(zip.getEntry(path)), object : DefaultHandler() {
                override fun startElement(u: String?, l: String, n: String?, a: Attributes) {
                    if (l == "t") capture = true
                    if (l == "br") text.append('\n')
                    if (l == "blip") {
                        val id = a.getValue("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed")
                        relationships[id]?.let { sink.emit(Block(BlockType.IMAGE, asset = "zip:${file.path}!/$it")) }
                    }
                }
                override fun characters(c: CharArray, s: Int, l: Int) { if (capture) text.append(c, s, l) }
                override fun endElement(u: String?, l: String, n: String?) {
                    if (l == "t") capture = false
                    if (l == "p" && text.isNotEmpty()) {
                        sink.emit(Block(BlockType.TEXT, text.toString())); text.setLength(0)
                    }
                }
            })
        }
    }
}
