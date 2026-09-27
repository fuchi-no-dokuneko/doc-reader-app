package app.docreader.format

import java.util.zip.ZipFile
import org.w3c.dom.Element

object EpubNavigation {
    fun titles(zip: ZipFile, opf: String, items: Collection<Element>): Map<String,String> {
        val titles=linkedMapOf<String,String>()
        items.filter { "nav" in it.getAttribute("properties").split(' ') }.forEach { item ->
            val path=XmlFiles.resolve(opf,item.getAttribute("href")); val entry=zip.getEntry(path) ?: return@forEach
            var href=""; val text=StringBuilder()
            XmlText.reader(zip.getInputStream(entry)).use { reader -> HtmlTokens.read(reader) { token ->
                if (token.tag=="a") {
                    if (!token.closing) { href=token.attrs["href"].orEmpty(); text.setLength(0) }
                    else if (href.isNotEmpty()) {
                        titles.putIfAbsent(XmlFiles.resolve(path,href),HtmlTokens.text(text.toString()).trim()); href=""
                    }
                } else if (href.isNotEmpty() && token.tag.isEmpty()) text.append(token.raw)
            } }
        }
        items.filter { it.getAttribute("media-type")=="application/x-dtbncx+xml" }.forEach { item ->
            val path=XmlFiles.resolve(opf,item.getAttribute("href")); val entry=zip.getEntry(path) ?: return@forEach
            val xml=XmlFiles.read(zip.getInputStream(entry))
            XmlFiles.elements(xml,"navPoint").forEach { point ->
                val content=point.getElementsByTagNameNS("*","content").item(0) as? Element
                val label=point.getElementsByTagNameNS("*","text").item(0)?.textContent
                if (content!=null && label!=null) titles.putIfAbsent(XmlFiles.resolve(path,content.getAttribute("src")),label)
            }
        }
        return titles
    }
}
