package app.docreader.format

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.xml.sax.InputSource
import org.w3c.dom.Element

object XmlFiles {
    fun read(input: InputStream): org.w3c.dom.Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        // Android's Harmony factory supports fewer features than desktop Xerces.
        for (feature in listOf("http://xml.org/sax/features/external-general-entities",
            "http://xml.org/sax/features/external-parameter-entities",
            "http://apache.org/xml/features/nonvalidating/load-external-dtd")) {
            try { factory.setFeature(feature,false) } catch (_: ParserConfigurationException) { }
        }
        val builder=factory.newDocumentBuilder()
        builder.setEntityResolver { _,_ -> InputSource(java.io.StringReader("")) }
        return input.use { builder.parse(it) }
    }
    fun elements(parent: org.w3c.dom.Document, name: String): List<Element> {
        val nodes = parent.getElementsByTagNameNS("*", name)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }
    fun resolve(base: String, href: String) = java.net.URI(null, null, "/$base", null)
        .resolve(href.replace(" ", "%20")).path.removePrefix("/")
    fun link(base: String, href: String): String {
        val uri=java.net.URI(href.replace(" ","%20"))
        if (uri.isAbsolute || uri.rawAuthority!=null) return href
        return resolve(base,href)+(uri.fragment?.let { "#$it" } ?: "")
    }
}
