package app.docreader.data

import app.docreader.domain.*
import java.io.DataInput
import java.io.DataOutput

object BlockCodec {
    fun write(out: DataOutput, block: Block) = with(block) {
        out.writeInt(type.ordinal); out.writeInt(level); out.writeInt(fold)
        listOf(text, language, asset, html, anchor, style).forEach { value ->
            val bytes = value.toByteArray(Charsets.UTF_8)
            out.writeInt(bytes.size); out.write(bytes)
        }
    }
    fun read(input: DataInput): Block {
        val type = BlockType.entries[input.readInt()]
        val level = input.readInt(); val fold = input.readInt()
        val values = List(6) {
            val count = input.readInt()
            require(count in 0..4_194_304) { "Invalid document index" }
            ByteArray(count).also { input.readFully(it) }.toString(Charsets.UTF_8)
        }
        return Block(type, values[0], level, values[1], values[2], values[3], values[4], fold, values[5])
    }
}
