package app.docreader.format

import java.io.InputStream
import java.util.zip.ZipFile
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.helpers.DefaultHandler

object OfficeXml {
    fun parse(input: InputStream, handler: DefaultHandler) {
        val factory = SAXParserFactory.newInstance(); factory.isNamespaceAware = true
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        input.use { factory.newSAXParser().parse(it, handler) }
    }
    fun relationships(zip: ZipFile, part: String): Map<String, String> {
        val folder = part.substringBeforeLast('/', "")
        val rel = (if (folder.isEmpty()) "" else "$folder/") + "_rels/" + part.substringAfterLast('/') + ".rels"
        val entry = zip.getEntry(rel) ?: return emptyMap()
        return XmlFiles.elements(XmlFiles.read(zip.getInputStream(entry)), "Relationship")
            .filter { it.getAttribute("TargetMode") != "External" }.associate {
                it.getAttribute("Id") to XmlFiles.resolve(part, it.getAttribute("Target"))
            }
    }
}
