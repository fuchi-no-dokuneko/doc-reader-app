package app.docreader.format

import java.util.zip.ZipFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class XlsxStyles(zip: ZipFile, private val epoch1904: Boolean) {
    private val formats: List<String>
    init {
        val entry=zip.getEntry("xl/styles.xml")
        if (entry==null) formats=emptyList()
        else {
            val xml=XmlFiles.read(zip.getInputStream(entry))
            val custom=XmlFiles.elements(xml,"numFmt").associate { it.getAttribute("numFmtId") to it.getAttribute("formatCode") }
            val parent=XmlFiles.elements(xml,"cellXfs").firstOrNull()
            val children=parent?.getElementsByTagNameNS("*","xf")
            formats=List(children?.length ?: 0) { index ->
                val id=(children!!.item(index) as org.w3c.dom.Element).getAttribute("numFmtId")
                custom[id] ?: when(id.toIntOrNull()) { in 14..17 -> "yyyy-mm-dd"; in 18..21,45,46,47 -> "hh:mm:ss"; 22 -> "yyyy-mm-dd hh:mm:ss"; else -> "" }
            }
        }
    }
    fun format(raw: String, index: Int): String {
        val value=raw.toDoubleOrNull() ?: return raw
        val pattern=formats.getOrNull(index).orEmpty().lowercase().replace(Regex("\"[^\"]*\"|\\\\."),"")
        val date='y' in pattern || 'd' in pattern
        val time='h' in pattern || 's' in pattern
        if (!date && !time) return raw
        val days=value.toLong(); val seconds=kotlin.math.round((value-days)*86400).toLong()
        val base=if (epoch1904) LocalDateTime.of(1904,1,1,0,0) else LocalDateTime.of(1899,12,31,0,0)
        val instant=base.plusDays(days-if (!epoch1904 && days>=60) 1 else 0).plusSeconds(seconds)
        return instant.format(DateTimeFormatter.ofPattern(if (date && time) "yyyy-MM-dd HH:mm:ss" else if (date) "yyyy-MM-dd" else "HH:mm:ss"))
    }
}
