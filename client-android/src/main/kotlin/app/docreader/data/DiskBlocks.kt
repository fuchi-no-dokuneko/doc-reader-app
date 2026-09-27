package app.docreader.data

import app.docreader.domain.*
import java.io.*

class DiskBlocks(directory: File, private val writable: Boolean = false) : BlockSource, BlockSink {
    private val data = RandomAccessFile(File(directory, "blocks.bin"), if (writable) "rw" else "r")
    private val index = RandomAccessFile(File(directory, "blocks.idx"), if (writable) "rw" else "r")
    private val toc = File(directory, "chapters.bin")
    private val headings = mutableListOf<Chapter>()
    override val count get() = (index.length() / 8).toInt()
    override val chapters get() = headings.toList()
    init {
        if (writable) { data.setLength(0); index.setLength(0) }
        else if (toc.exists()) DataInputStream(toc.inputStream().buffered()).use {
            while (it.available() > 0) headings += Chapter(it.readUTF(), it.readInt(), it.readInt())
        }
    }
    @Synchronized override fun emit(block: Block) {
        check(writable)
        val position = count
        index.seek(index.length()); index.writeLong(data.length())
        data.seek(data.length()); BlockCodec.write(data, block)
        if (block.type == BlockType.HEADING) headings += Chapter(block.text.take(300), position, block.level)
    }
    @Synchronized override fun block(index: Int): Block {
        require(index in 0 until count)
        this.index.seek(index * 8L); data.seek(this.index.readLong())
        return BlockCodec.read(data)
    }
    override fun close() {
        if (writable) DataOutputStream(toc.outputStream().buffered()).use { out ->
            headings.forEach { out.writeUTF(it.title); out.writeInt(it.block); out.writeInt(it.level) }
        }
        data.close(); index.close()
    }
}
