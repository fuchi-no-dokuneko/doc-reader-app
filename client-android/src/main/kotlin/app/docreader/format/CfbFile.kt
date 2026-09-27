package app.docreader.format

import java.io.File
import java.io.RandomAccessFile

class CfbFile(file: File) : AutoCloseable {
    private val input = RandomAccessFile(file, "r")
    private val header = ByteArray(512).also(input::readFully)
    private val fat: CfbFat
    private data class Entry(val name: String, val start: Int, val size: Long, val root: Boolean)
    private val entries = mutableListOf<Entry>()
    private val mini: BinarySource
    private val miniFat: IntArray
    init {
        require(header.take(8).toByteArray().contentEquals(byteArrayOf(-48,-49,17,-32,-95,-79,26,-31))) {
            "This is not an Office binary document"
        }
        fat = CfbFat(input, header)
        val directory = fat.source(Binary.i32(header, 48), Long.MAX_VALUE)
        for (p in 0L until directory.size step 128) {
            val b = directory.read(p, 128); val len = Binary.u16(b, 64)
            if (len in 2..64 && b[66].toInt() in 2..5) entries += Entry(
                String(b, 0, len - 2, Charsets.UTF_16LE), Binary.i32(b,116),
                if (fat.sector == 512) Binary.i32(b,120).toLong() and 0xffffffffL else Binary.i64(b,120), b[66].toInt() == 5)
        }
        val root = entries.firstOrNull { it.root } ?: error("Missing Office root stream")
        mini = fat.source(root.start, root.size)
        val table = fat.source(Binary.i32(header,60), Binary.i32(header,64).toLong() * fat.sector)
        require(table.size <= 16 * 1024 * 1024)
        val bytes = table.read(0, table.size.toInt())
        miniFat = IntArray(bytes.size / 4) { Binary.i32(bytes, it * 4) }
    }
    fun stream(name: String): BinarySource? {
        val e = entries.firstOrNull { it.name == name && !it.root } ?: return null
        if (e.size >= 4096) return fat.source(e.start, e.size)
        val ids = fat.chain(e.start, miniFat)
        return object : BinarySource {
            override val size = e.size
            override fun read(position: Long, length: Int): ByteArray {
                require(position >= 0 && position + length <= size)
                val out = ByteArray(length); var p = position; var done = 0
                while (done < length) {
                    val offset = (p % 64).toInt(); val n = minOf(length - done, 64 - offset)
                    mini.read(ids[(p / 64).toInt()] * 64L + offset, n).copyInto(out, done)
                    p += n; done += n
                }
                return out
            }
        }
    }
    override fun close() = input.close()
}
