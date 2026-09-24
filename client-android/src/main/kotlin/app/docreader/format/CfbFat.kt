package app.docreader.format

import java.io.RandomAccessFile

class CfbFat(private val file: RandomAccessFile, header: ByteArray) {
    val sector = 1 shl Binary.u16(header, 30)
    private val limit = (file.length() / sector).toInt()
    private val fat: IntArray
    init {
        require(sector == 512 || sector == 4096) { "Invalid Office sector size" }
        val sectors = mutableListOf<Int>()
        for (i in 0 until 109) Binary.i32(header, 76 + i * 4).takeIf { it >= 0 }?.let(sectors::add)
        var next = Binary.i32(header, 68)
        val seen = hashSetOf<Int>()
        while (next >= 0 && seen.add(next)) {
            val data = sector(next)
            for (i in 0 until sector / 4 - 1) Binary.i32(data, i * 4).takeIf { it >= 0 }?.let(sectors::add)
            next = Binary.i32(data, sector - 4)
        }
        require(sectors.size <= limit)
        fat = IntArray(sectors.size * sector / 4)
        sectors.forEachIndexed { index, id ->
            val b = sector(id)
            for (i in b.indices step 4) fat[index * sector / 4 + i / 4] = Binary.i32(b, i)
        }
    }
    fun sector(id: Int): ByteArray {
        require(id in 0 until limit) { "Broken Office sector chain" }
        return ByteArray(sector).also { file.seek((id + 1L) * sector); file.readFully(it) }
    }
    fun chain(start: Int, table: IntArray = fat): IntArray {
        val result = ArrayList<Int>(); val seen = HashSet<Int>(); var id = start
        while (id >= 0) {
            require(id < table.size && seen.add(id)) { "Broken Office sector chain" }
            result += id; id = table[id]
        }
        return result.toIntArray()
    }
    fun source(start: Int, size: Long): BinarySource {
        val ids = chain(start)
        return object : BinarySource {
            override val size = minOf(size, ids.size.toLong() * sector)
            override fun read(position: Long, length: Int): ByteArray {
                require(position >= 0 && length >= 0 && position + length <= this.size)
                val out = ByteArray(length); var p = position; var done = 0
                while (done < length) {
                    val b = sector(ids[(p / sector).toInt()]); val off = (p % sector).toInt()
                    val n = minOf(length - done, sector - off)
                    b.copyInto(out, done, off, off + n); p += n; done += n
                }
                return out
            }
        }
    }
}
