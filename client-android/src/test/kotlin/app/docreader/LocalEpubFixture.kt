package app.docreader

import app.docreader.domain.BlockType
import app.docreader.format.EpubDocument
import org.junit.Assert.*
import java.io.File
import java.util.zip.ZipFile

object LocalEpubFixture {
    // Optional extra publication supplied explicitly; the fixed regression runs regardless.
    fun verify() {
        val path=System.getenv("DOC_READER_EPUB") ?: return
        val file=File(path); require(file.isFile) { "Missing supplied EPUB: $path" }
        val blocks=Blocks(); EpubDocument.parse(file,blocks)
        assertTrue("Publication contains readable text",blocks.text.length>50000)
        assertTrue("Publication contains chapters",blocks.values.count { it.type==BlockType.HEADING }>12)
        val images=blocks.values.filter { it.type==BlockType.IMAGE }
        assertTrue("Publication contains local artwork",images.isNotEmpty())
        ZipFile(file).use { zip -> images.forEach { assertNotNull(zip.getEntry(it.asset.substringAfter("!/"))) } }
        println("EPUB publication verified: ${file.name}, ${blocks.count} blocks, ${blocks.text.length} characters, ${images.size} images")
    }
}
