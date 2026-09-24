package app.docreader.format

import app.docreader.domain.*
import java.io.File
import java.util.zip.ZipFile
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

object XlsxDocument {
    fun parse(file: File, sink: BlockSink, work: File) = ZipFile(file).use { zip ->
        DiskStrings(work).use { strings ->
            XlsxStrings.read(zip, strings)
            val rels = OfficeXml.relationships(zip, "xl/workbook.xml")
            val workbook = XmlFiles.read(zip.getInputStream(zip.getEntry("xl/workbook.xml")))
            val styles=XlsxStyles(zip,XmlFiles.elements(workbook,"workbookPr").firstOrNull()?.getAttribute("date1904") in setOf("1","true"))
            for (sheet in XmlFiles.elements(workbook, "sheet")) {
                val id = sheet.getAttribute("r:id")
                val entry = zip.getEntry(rels[id] ?: continue) ?: continue
                sink.emit(Block(BlockType.HEADING, sheet.getAttribute("name"), 1))
                val rows = SheetRows(sink); val value = StringBuilder(); val formula = StringBuilder()
                var row = 0; var column = 0; var type = ""; var capture = ""; var style=0
                OfficeXml.parse(zip.getInputStream(entry), object : DefaultHandler() {
                    override fun startElement(u: String?, l: String, n: String?, a: Attributes) {
                        if (l == "row") row = (a.getValue("r")?.toIntOrNull() ?: row+1)-1
                        if (l == "c") { column = SheetRows.column(a.getValue("r").orEmpty()); type = a.getValue("t").orEmpty(); style=a.getValue("s")?.toIntOrNull() ?: 0; value.setLength(0); formula.setLength(0) }
                        if (l in setOf("t", "v", "f")) capture = l
                    }
                    override fun characters(c: CharArray, s: Int, l: Int) {
                        if (capture == "f") formula.append(c, s, l) else if (capture.isNotEmpty()) value.append(c, s, l)
                    }
                    override fun endElement(u: String?, l: String, n: String?) {
                        if (l == capture) capture = ""
                        if (l == "c") rows.cell(row, column, when (type) {
                            "s" -> strings.get(value.toString().toInt()); "b" -> if (value.toString() == "1") "true" else "false"
                            else -> styles.format(value.toString(),style).ifEmpty { if (formula.isNotEmpty()) "=$formula" else "" }
                        })
                    }
                }); rows.flush()
            }
        }
    }
}
