package app.docreader.data

import app.docreader.domain.*
import java.io.File
import java.io.RandomAccessFile

class DiskPages(directory: File) : AutoCloseable {
    private val data = RandomAccessFile(File(directory,"pages.bin"),"rw")
    private val index = RandomAccessFile(File(directory,"pages.idx"),"rw")
    @Volatile var count = 0
        private set
    init { data.setLength(0); index.setLength(0) }
    @Synchronized fun add(slices: List<PageSlice>) {
        if (slices.isEmpty()) return
        index.seek(count*8L); index.writeLong(data.length()); data.seek(data.length())
        data.writeInt(slices.size)
        slices.forEach { data.writeInt(it.block); data.writeInt(it.start); data.writeInt(it.end) }
        count++
    }
    @Synchronized fun page(number: Int): PageRef {
        require(number in 0 until count)
        index.seek(number*8L); data.seek(index.readLong())
        val n = data.readInt(); require(n in 1..10000)
        val slices = List(n) { PageSlice(data.readInt(),data.readInt(),data.readInt()) }
        return PageRef(slices,anchor(slices.first()))
    }
    fun find(anchor: Long): Int {
        var low = 0; var high = count-1
        while (low <= high) {
            val middle = (low+high) ushr 1
            if (page(middle).anchor <= anchor) low = middle+1 else high = middle-1
        }
        return high.coerceAtLeast(0)
    }
    override fun close() { data.close(); index.close() }
    companion object {
        fun anchor(slice: PageSlice) = (slice.block.toLong() shl 32) or slice.start.toLong()
    }
}
