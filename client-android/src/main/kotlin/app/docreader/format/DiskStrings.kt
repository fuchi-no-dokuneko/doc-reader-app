package app.docreader.format

import java.io.File
import java.io.RandomAccessFile

class DiskStrings(directory: File) : AutoCloseable {
    private val data = RandomAccessFile(File(directory, "strings.bin"), "rw")
    private val offsets = RandomAccessFile(File(directory, "strings.idx"), "rw")
    init { data.setLength(0); offsets.setLength(0) }
    fun add(value: String) {
        offsets.seek(offsets.length()); offsets.writeLong(data.length()); data.seek(data.length())
        val bytes = value.toByteArray(Charsets.UTF_8); data.writeInt(bytes.size); data.write(bytes)
    }
    fun get(index: Int): String {
        if (index < 0 || index * 8L >= offsets.length()) return ""
        offsets.seek(index * 8L); data.seek(offsets.readLong())
        val size = data.readInt(); require(size in 0..4_194_304)
        return ByteArray(size).also(data::readFully).toString(Charsets.UTF_8)
    }
    override fun close() { data.close(); offsets.close() }
}
