package app.docreader

import app.docreader.domain.*
import app.docreader.format.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class NotebookTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun unorderedCellsAndSavedRichOutputs() {
        val out=Blocks(); val dir=temporary.newFolder()
        val json="""{"nbformat":4,"cells":[
          {"source":["# Notebook\n","**hello**"],"cell_type":"markdown"},
          {"outputs":[{"output_type":"stream","text":["one\n","two\n"]},
             {"data":{"text/plain":["fallback"],"text/html":["<table><tr><th>A</th></tr><tr><td>42</td></tr></table>"]}},
             {"data":{"image/png":["aGVsbG8="]}}],"source":["print(42)"],"cell_type":"code"}]}"""
        NotebookDocument.parse(json.reader(),out,AssetFiles(dir))
        assertTrue(out.text.contains("Notebook")); assertTrue(out.text.contains("print(42)"))
        assertTrue(out.text.contains("one")); assertTrue(out.text.contains("two"))
        assertTrue(out.values.any { it.type==BlockType.TABLE && "42" in it.html })
        val image=out.values.first { it.type==BlockType.IMAGE }
        assertEquals("hello",java.io.File(image.asset).readText())
        assertFalse(dir.listFiles()!!.any { it.name.startsWith("cell-") })
    }
}
