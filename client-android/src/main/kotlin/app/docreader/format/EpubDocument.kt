package app.docreader.format

import app.docreader.domain.*
import java.io.File
import java.util.zip.ZipFile

object EpubDocument {
    fun parse(file: File, sink: BlockSink) {
        ZipFile(file).use { zip ->
            val container = zip.getEntry("META-INF/container.xml") ?: error("EPUB container is missing")
            val opfName = XmlFiles.elements(XmlFiles.read(zip.getInputStream(container)), "rootfile")
                .firstOrNull()?.getAttribute("full-path") ?: error("EPUB package is missing")
            val opf = XmlFiles.read(zip.getInputStream(zip.getEntry(opfName) ?: error("EPUB package is missing")))
            val items = XmlFiles.elements(opf, "item").associateBy { it.getAttribute("id") }
            val styles = CssSheet()
            val titles=EpubNavigation.titles(zip,opfName,items.values)
            items.values.filter { it.getAttribute("media-type") == "text/css" }.forEach {
                val path = XmlFiles.resolve(opfName, it.getAttribute("href"))
                zip.getEntry(path)?.let { entry -> zip.getInputStream(entry).bufferedReader().use { css -> styles.add(css.readText()) } }
            }
            val spine = XmlFiles.elements(opf, "itemref")
            require(spine.isNotEmpty()) { "This EPUB has no reading order" }
            for ((index, ref) in spine.withIndex()) {
                if (Thread.currentThread().isInterrupted) throw InterruptedException()
                val item = items[ref.getAttribute("idref")] ?: continue
                val path = XmlFiles.resolve(opfName, item.getAttribute("href"))
                val entry = zip.getEntry(path) ?: continue
                sink.emit(Block(BlockType.HEADING, titles[path] ?: "Chapter ${index+1}", 1, anchor = path))
                zip.getInputStream(entry).bufferedReader().use { reader ->
                    HtmlDocument.parse(reader, sink, { href -> "zip:${file.path}!/${XmlFiles.resolve(path, href)}" }, styles, path)
                }
            }
        }
    }
}
