package app.docreader.format

import app.docreader.domain.*
import java.io.File

object PptDocument {
    fun parse(file: File, sink: BlockSink) = CfbFile(file).use { cfb ->
        val data = cfb.stream("PowerPoint Document") ?: error("Missing PowerPoint document")
        var slide = 0
        fun records(start: Long, end: Long, depth: Int) {
            require(depth < 64); var p = start
            while (p + 8 <= end) {
                val head = data.read(p,8); val type = Binary.u16(head,2)
                val length = Binary.i32(head,4).toLong() and 0xffffffffL
                val next = p + 8 + length; require(next <= end) { "Damaged PowerPoint record" }
                if (type == 1006) sink.emit(Block(BlockType.HEADING, "Slide ${++slide}", 1))
                if (Binary.u16(head,0) and 15 == 15) records(p+8,next,depth+1)
                else if (type == 4000 || type == 4008) {
                    val charset = if (type == 4000) Charsets.UTF_16LE else Charsets.ISO_8859_1
                    data.stream(p+8,length).reader(charset).use { reader ->
                        TextLines.read(reader) { sink.emit(Block(text = it.replace('\u000b','\n'))) }
                    }
                }
                p = next
            }
        }
        records(0,data.size,0)
    }
}
