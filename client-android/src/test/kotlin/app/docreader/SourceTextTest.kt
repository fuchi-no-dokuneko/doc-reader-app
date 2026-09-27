package app.docreader

import app.docreader.data.SourceText
import app.docreader.domain.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import java.nio.charset.Charset

class SourceTextTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun keepsBomEncodingWhitespaceAndMixedLineEndings() {
        for (encoding in listOf("UTF-8","UTF-16LE","UTF-16BE","GBK","Big5")) {
            val bom=encoding.startsWith("UTF")
            val raw=(if (bom) "\uFEFF" else "")+"# 中文\r\nx = 1\n\tprint(x)\r\n"
            val file=temporary.newFile(); val bytes=raw.toByteArray(Charset.forName(encoding)); file.writeBytes(bytes)
            val source=SourceText.read(file,encoding)
            assertArrayEquals(bytes,source.encode(source.text))
            assertArrayEquals(raw.replace("x = 1","x = 2").toByteArray(Charset.forName(encoding)),
                source.encode(source.text.replace("x = 1","x = 2")))
        }
    }
    @Test fun refusesLossyEncodingAndOnlyOffersTextBasedFiles() {
        val source=SourceText("x = 1","GBK",false)
        assertThrows(java.nio.charset.CharacterCodingException::class.java) { source.encode("x = '🙂'") }
        for (name in listOf("hello.py","app.kt","index.tsx","x.yaml",".config","a.json","a.html"))
            assertTrue(name,EditableFiles.supports(Formats.detect(name)))
        for (kind in listOf(Kind.PDF,Kind.EPUB,Kind.DOCX,Kind.XLS)) assertFalse(EditableFiles.supports(kind))
    }
}
