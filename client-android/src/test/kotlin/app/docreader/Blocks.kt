package app.docreader

import app.docreader.domain.*
import java.io.File
import java.util.zip.*

class Blocks : BlockSink,BlockSource {
    val values=mutableListOf<Block>()
    override fun emit(block: Block) { values+=block }
    override val count get()=values.size
    override fun block(index: Int)=values[index]
    override val chapters get()=emptyList<Chapter>()
    override fun close() {}
    val text get()=values.joinToString("\n") { it.text }
}
fun zipFixture(file: File, entries: Map<String,String>): File {
    ZipOutputStream(file.outputStream()).use { out -> entries.forEach { (name,text) ->
        out.putNextEntry(ZipEntry(name)); out.write(text.toByteArray()); out.closeEntry()
    } }; return file
}
