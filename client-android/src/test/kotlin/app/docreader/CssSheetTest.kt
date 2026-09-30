package app.docreader

import app.docreader.format.*
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class CssSheetTest {
    @Test fun emptyStylesheetHasNoDeclarations() {
        assertTrue(CssSheet.properties(CssSheet().forTag("p",emptyMap())).isEmpty())
    }
    @Test fun twoFlatRulesRemainSeparate() {
        val css=CssSheet("h1 {font-size: 24px} p {text-align: center}")
        assertEquals(mapOf("font-size" to "24px"),CssSheet.properties(css.forTag("h1",emptyMap())))
        assertEquals(mapOf("text-align" to "center"),CssSheet.properties(css.forTag("p",emptyMap())))
    }
    @Test fun commaSelectorsAndClassMatchingKeepDeclarations() {
        val css=CssSheet("p, .note {font-style: italic}")
        assertEquals("italic",CssSheet.properties(css.forTag("div",mapOf("class" to "note")))["font-style"])
        assertEquals("italic",CssSheet.properties(css.forTag("p",emptyMap()))["font-style"])
    }
    @Test fun commentsAndMalformedRulesLeaveContentAvailable() {
        val css=CssSheet("/* first\nsecond { ignored } */ p {color: red} .broken {color:")
        assertEquals("red",CssSheet.properties(css.forTag("p",emptyMap()))["color"])
        val blocks=Blocks()
        HtmlDocument.parse("<html><body><p>Plain HTML remains readable.</p></body></html>".reader(),blocks)
        assertTrue(blocks.text.contains("Plain HTML remains readable."))
    }
    @Test fun epubExternalAndEmptyStylesheetsKeepChapterText() {
        for (name in listOf("empty","styled")) {
            val blocks=Blocks()
            EpubDocument.parse(File("src/androidTest/assets/$name.epub"),blocks)
            assertTrue(blocks.text.contains("Runtime EPUB chapter"))
            assertTrue(blocks.text.contains("Readable $name stylesheet EPUB."))
            if (name=="styled") assertTrue(blocks.values.any { "italic" in it.style })
        }
    }
}
