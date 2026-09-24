package app.docreader

import app.docreader.domain.*
import app.docreader.format.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class EpubTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun spineCssAnchorsAndRelativeImages() {
        val file=zipFixture(temporary.newFile(),mapOf(
            "META-INF/container.xml" to "<container><rootfiles><rootfile full-path='OPS/book.opf'/></rootfiles></container>",
            "OPS/book.opf" to "<package><manifest><item id='c' href='text/chapter.xhtml' media-type='application/xhtml+xml'/><item id='s' href='style.css' media-type='text/css'/></manifest><spine><itemref idref='c'/></spine></package>",
            "OPS/style.css" to "h1 {font-size: 1.5em; text-align: center} .note {font-style: italic}",
            "OPS/text/chapter.xhtml" to "<html><head><title>Hidden title</title></head><body><h1 id='intro'>Introduction</h1><p class='note'>Body &amp; text</p><img src='../images/pic.png'/></body></html>"))
        val out=Blocks(); EpubDocument.parse(file,out)
        assertFalse(out.text.contains("Hidden title"))
        val title=out.values.first { it.text=="Introduction" }
        assertEquals("OPS/text/chapter.xhtml#intro",title.anchor)
        assertTrue(title.style.contains("font-size"))
        assertTrue(out.values.any { it.text=="Body & text" && "italic" in it.style })
        assertTrue(out.values.last().asset.endsWith("!/OPS/images/pic.png"))
    }
}
