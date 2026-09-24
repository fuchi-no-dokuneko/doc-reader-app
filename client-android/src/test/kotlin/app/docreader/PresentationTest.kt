package app.docreader

import app.docreader.format.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class PresentationTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun openXmlPresentationFollowsDeclaredSlideOrder() {
        val file=zipFixture(temporary.newFile(),mapOf(
            "ppt/presentation.xml" to "<p:presentation xmlns:p='urn:ppt' xmlns:r='urn:rels'><p:sldIdLst><p:sldId r:id='b'/><p:sldId r:id='a'/></p:sldIdLst></p:presentation>",
            "ppt/_rels/presentation.xml.rels" to "<Relationships><Relationship Id='a' Target='slides/slide1.xml'/><Relationship Id='b' Target='slides/slide2.xml'/></Relationships>",
            "ppt/slides/slide1.xml" to "<sld xmlns:a='urn:drawing'><a:p><a:r><a:t>Last</a:t></a:r></a:p></sld>",
            "ppt/slides/slide2.xml" to "<sld xmlns:a='urn:drawing'><a:p><a:r><a:t>First 中文</a:t></a:r></a:p></sld>"))
        val out=Blocks(); PptxDocument.parse(file,out)
        assertEquals(listOf("Slide 1","First 中文","Slide 2","Last"),out.values.map { it.text })
    }
    @Test fun rtfUnicodeAndLegacyChineseEscapes() {
        val out=Blocks()
        RtfDocument.parse("{\\rtf1\\ansi\\ansicpg936 \\'d6\\'d0\\'ce\\'c4\\par \\uc1\\u20013?\\u25991?}".reader(),out)
        assertEquals("中文\n中文",out.text)
    }
}
