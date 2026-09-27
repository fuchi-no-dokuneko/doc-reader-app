package app.docreader.format

import java.util.zip.ZipFile
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

object XlsxStrings {
    fun read(zip: ZipFile, strings: DiskStrings) {
        val entry = zip.getEntry("xl/sharedStrings.xml") ?: return
        val text = StringBuilder(); var capture = false
        OfficeXml.parse(zip.getInputStream(entry), object : DefaultHandler() {
            override fun startElement(u: String?, l: String, n: String?, a: Attributes) {
                if (l == "si") text.setLength(0)
                if (l == "t") capture = true
            }
            override fun characters(c: CharArray, s: Int, l: Int) { if (capture) text.append(c, s, l) }
            override fun endElement(u: String?, l: String, n: String?) {
                if (l == "t") capture = false
                if (l == "si") strings.add(text.toString())
            }
        })
    }
}
