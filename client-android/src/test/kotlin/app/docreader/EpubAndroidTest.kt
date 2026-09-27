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
class EpubAndroidTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun epubOpensWithAndroidXmlParser() {
        val key="javax.xml.parsers.DocumentBuilderFactory"
        val previous=System.getProperty(key)
        System.setProperty(key,"org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl")
        try {
            val file=zipFixture(temporary.newFile(),mapOf(
                "META-INF/container.xml" to "<container xmlns='urn:oasis:names:tc:opendocument:xmlns:container'><rootfiles><rootfile full-path='OPS/book.opf'/></rootfiles></container>",
                "OPS/book.opf" to "<package xmlns='http://www.idpf.org/2007/opf'><manifest><item id='c' href='chapter.xhtml' media-type='application/xhtml+xml'/></manifest><spine><itemref idref='c'/></spine></package>",
                "OPS/chapter.xhtml" to "<html xmlns='http://www.w3.org/1999/xhtml'><body><h1>Working EPUB</h1><p>Readable on Android.</p></body></html>"))
            val out=Blocks(); EpubDocument.parse(file,out)
            assertTrue(out.text.contains("Readable on Android."))
            LocalEpubFixture.verify()
        } finally {
            if (previous==null) System.clearProperty(key) else System.setProperty(key,previous)
        }
    }
}
