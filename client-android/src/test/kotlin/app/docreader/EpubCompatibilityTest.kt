package app.docreader

import app.docreader.format.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.rules.TemporaryFolder

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class EpubCompatibilityTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun epub2NcxDtdPrefixedXhtmlAndEncodedLinks() = androidXml {
        val file=zipFixture(temporary.newFile(),mapOf(
            "META-INF/container.xml" to "<container><rootfiles><rootfile full-path='OPS/book.opf'/></rootfiles></container>",
            "OPS/book.opf" to "<package><manifest><item id='a' href='text/first%20chapter.xhtml'/><item id='b' href='text/second.xhtml'/><item id='toc' href='toc.ncx' media-type='application/x-dtbncx+xml'/></manifest><spine><itemref idref='a'/><itemref idref='b'/></spine></package>",
            "OPS/toc.ncx" to "<!DOCTYPE ncx SYSTEM 'file:///unavailable.dtd'><ncx xmlns='http://www.daisy.org/z3986/2005/ncx/'><navMap><navPoint><navLabel><text>First title</text></navLabel><content src='text/first%20chapter.xhtml#start'/></navPoint></navMap></ncx>",
            "OPS/text/first chapter.xhtml" to "<x:html xmlns:x='http://www.w3.org/1999/xhtml'><x:body><x:h1 id='start'>Chapter One</x:h1><x:p>Text <x:a href='./second.xhtml#end'>next</x:a></x:p></x:body></x:html>",
            "OPS/text/second.xhtml" to "<html><body><p id='end'>Chapter Two</p><svg><image xlink:href='../cover.png'/></svg></body></html>"))
        val out=Blocks(); EpubDocument.parse(file,out)
        assertEquals("First title",out.values.first().text)
        assertTrue(out.text.indexOf("Chapter One")<out.text.indexOf("Chapter Two"))
        assertTrue(out.values.any { "OPS/text/second.xhtml#end" in it.html })
        assertTrue(out.values.any { it.asset.endsWith("!/OPS/cover.png") })
    }
    @Test fun utf16ChapterAndExternalEntityHandling() = androidXml {
        val encoded="<html><body><p>中文 EPUB</p></body></html>".toByteArray(Charsets.UTF_16)
        val out=Blocks(); XmlText.reader(encoded.inputStream()).use { HtmlDocument.parse(it,out) }
        assertEquals("中文 EPUB",out.text)
        val xml="<!DOCTYPE package SYSTEM 'file:///unavailable.dtd'><package/>"
        assertEquals("package",XmlFiles.read(xml.byteInputStream()).documentElement.tagName)
    }
}
