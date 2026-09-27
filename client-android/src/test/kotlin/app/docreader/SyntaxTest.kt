package app.docreader

import app.docreader.render.SyntaxTokens
import org.junit.Test
import org.junit.Assert.*

class SyntaxTest {
    @Test fun languageAwareCommentsStringsNumbersAndKeywords() {
        val samples=mapOf("py" to "def run():\n  return \"# text\" # note",
            "js" to "const n = 42; /* two\n lines */",
            "kt" to "val name = \"reader\" // comment", "sql" to "SELECT 42 -- comment")
        for ((lang,source) in samples) {
            val tokens=SyntaxTokens.scan(source,lang)
            assertTrue(lang,tokens.any { it.kind==0 }); assertTrue(lang,tokens.any { it.kind==2 })
        }
        assertTrue(SyntaxTokens.scan("a {color: #fff}","css").none { it.kind==2 })
        assertTrue(SyntaxTokens.scan("{\"text\":\"//not a comment\",\"n\":1.5e-2}","json").none { it.kind==2 })
        assertTrue(SyntaxTokens.scan("<!-- hidden\n text -->", "html").single().kind==2)
    }
    @Test fun longStringsIncompleteTypingAndCancellation() {
        val source="\""+"a".repeat(100000)+"\""
        assertEquals(source.length,SyntaxTokens.scan(source,"json").single().end)
        val unfinished=SyntaxTokens.scan("x = \"typing\nreturn 2","py")
        assertTrue(unfinished.any { it.kind==1 }); assertTrue(unfinished.any { it.kind==0 })
        assertTrue(SyntaxTokens.scan(source,"json") { false }.isEmpty())
    }
}
