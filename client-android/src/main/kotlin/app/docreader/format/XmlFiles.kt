package app.docreader.format

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object XmlFiles {
    fun read(input: InputStream): org.w3c.dom.Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        return input.use { factory.newDocumentBuilder().parse(it) }
    }
    fun elements(parent: org.w3c.dom.Document, name: String): List<Element> {
        val nodes = parent.getElementsByTagNameNS("*", name)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }
    fun resolve(base: String, href: String) = java.net.URI(null, null, "/$base", null)
        .resolve(href.replace(" ", "%20")).path.removePrefix("/")
}
