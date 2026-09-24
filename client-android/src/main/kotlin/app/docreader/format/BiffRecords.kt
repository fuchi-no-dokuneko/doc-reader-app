package app.docreader.format

class BiffRecords(private val source: BinarySource) {
    data class Record(val id: Int, val data: ByteArray)
    var position = 0L
    var pending: Record? = null
    fun next(): Record? {
        pending?.let { pending = null; return it }
        if (position + 4 > source.size) return null
        val h = source.read(position,4); val length = Binary.u16(h,2)
        require(position + 4 + length <= source.size) { "Damaged Excel record" }
        val result = Record(Binary.u16(h,0),source.read(position+4,length))
        position += length + 4; return result
    }
}
class BiffStringInput(private val records: BiffRecords, first: ByteArray) {
    var data = first
    var offset = 8
    private fun advance() {
        val record = records.next() ?: error("Incomplete Excel shared string")
        require(record.id == 0x3c) { "Missing Excel string continuation" }
        data = record.data; offset = 0
    }
    fun byte(): Int {
        if (offset == data.size) advance()
        return data[offset++].toInt() and 255
    }
    fun short() = byte() or (byte() shl 8)
    fun int() = short() or (short() shl 16)
    fun string(): String {
        val length = short(); val flags = byte()
        val runs = if (flags and 8 != 0) short() else 0
        val extension = if (flags and 4 != 0) int() else 0
        var wide = flags and 1 != 0; val text = StringBuilder(length)
        repeat(length) {
            if (offset == data.size) { advance(); wide = byte() and 1 != 0 }
            text.append((if (wide) short() else byte()).toChar())
        }
        require(extension >= 0)
        repeat(runs * 4 + extension) { byte() }
        return text.toString()
    }
}
