package app.docreader

import app.docreader.domain.*
import app.docreader.format.*
import org.junit.Assert.*
import org.junit.Test

class TextFormatsTest {
    @Test fun commonExtensionsAndDotfiles() {
        assertEquals(Kind.NOTEBOOK,Formats.detect("analysis.ipynb"))
        assertEquals(Kind.NOTEBOOK,Formats.detect("analysis.ipybn"))
        listOf(".env",".gitignore","app.config","settings.toml").forEach { assertEquals(it,Kind.CONFIG,Formats.detect(it)) }
        listOf("py","java","kt","c","cpp","go","rs","js","ts","swift","sql","sh","rb","dart").forEach {
            assertEquals(it,Kind.CODE,Formats.detect("source.$it"))
        }
        assertEquals(Kind.YAML,Formats.detect("deploy.YAML"))
    }
    @Test fun markdownKeepsCodePipesAndBuildsTablesImagesLinks() {
        val out=Blocks()
        MarkdownDocument.parse("# Title\n\n| A | B |\n|---|---|\n| one | two |\n\n```py\na | b\n```\n[site](https://example.org)\n![plot](plot.png)".reader(),out) { "local/$it" }
        assertEquals("Title",out.values.first().text)
        assertTrue(out.values.any { it.type==BlockType.CODE && it.text=="a | b" })
        assertTrue(out.values.any { it.type==BlockType.TABLE && "<th>A</th>" in it.html && "two" in it.html })
        assertTrue(out.values.any { "href=\"https://example.org\"" in it.html })
        assertEquals("local/plot.png",out.values.last().asset)
    }
    @Test fun jsonLinesNestedAndLongStringsRemainBounded() {
        val out=Blocks(); val long="x".repeat(14000)
        JsonDocument.parse(("{\"items\":[{\"name\":\"中文\"}],\"long\":\"$long\"}\n{\"ok\":true}").reader(),out)
        assertTrue(out.values.any { it.fold>0 && it.level==1 })
        assertTrue(out.text.contains("中文")); assertTrue(out.text.contains("true"))
        assertTrue(out.values.maxOf { it.text.length }<5000)
        assertTrue(out.text.contains("  \"items\""))
    }
    @Test fun csvQuotedMultilineAndEscapedQuotes() {
        val out=Blocks()
        CsvDocument.parse("name,note\nAlice,\"hello, world\nsecond \"\"line\"\"\"\n".reader(),out)
        assertTrue(out.text.contains("hello, world\nsecond \"line\""))
        assertTrue(out.values.last().html.contains("<th>name</th>"))
    }
}
