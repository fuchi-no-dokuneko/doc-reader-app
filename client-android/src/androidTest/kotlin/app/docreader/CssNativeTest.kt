package app.docreader

import app.docreader.format.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.docreader.domain.*

@RunWith(AndroidJUnit4::class)
class CssNativeTest {
    @Test fun androidRegexAcceptsEmptyAndFlatCssRules() {
        assertTrue(CssSheet().forTag("p",emptyMap()).all { it==';' })
        val css=CssSheet("/* comment\n continues */ h1 {font-size: 24px} p, .note {font-style: italic}")
        assertEquals("24px",CssSheet.properties(css.forTag("h1",emptyMap()))["font-size"])
        assertEquals("italic",CssSheet.properties(css.forTag("div",mapOf("class" to "note")))["font-style"])
        assertEquals("italic",CssSheet.properties(css.forTag("p",emptyMap()))["font-style"])
    }
    @Test fun htmlDefaultStylesAndUnsupportedRulesKeepTextReadable() {
        val out=mutableListOf<Block>(); val sink=object: BlockSink { override fun emit(block: Block) { out+=block } }
        HtmlDocument.parse("<html><body><p>Native HTML text</p></body></html>".reader(),sink)
        assertTrue(out.any { it.text=="Native HTML text" })
        CssSheet("broken { ; @unsupported what").forTag("p",emptyMap())
    }
}
