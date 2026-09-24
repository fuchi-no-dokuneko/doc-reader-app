package app.docreader.format

object XlsCells {
    fun rk(value: Int): Double {
        val number = if (value and 2 != 0) (value shr 2).toDouble()
        else Double.fromBits((value.toLong() and 0xfffffffcL) shl 32)
        return if (value and 1 != 0) number / 100 else number
    }
    fun number(value: Double) = if (value.isFinite() && value == value.toLong().toDouble())
        value.toLong().toString() else value.toString()
    fun parse(id: Int, b: ByteArray, strings: DiskStrings, put: (Int,Int,String) -> Unit) {
        if (b.size < 6) return
        val row = Binary.u16(b,0); val col = Binary.u16(b,2)
        when (id) {
            0xfd -> put(row,col,strings.get(Binary.i32(b,6)))
            0x203 -> put(row,col,number(Binary.double(b,6)))
            0x27e -> put(row,col,number(rk(Binary.i32(b,6))))
            0xbd -> {
                var p = 4; var c = col
                while (p + 6 <= b.size - 2) { put(row,c++,number(rk(Binary.i32(b,p+2)))); p += 6 }
            }
            0x205 -> put(row,col,if (b[7].toInt() == 0) (b[6].toInt()!=0).toString() else "#ERROR ${b[6]}")
            0x204 -> {
                val length = Binary.u16(b,6); val wide = b.getOrElse(8) { 0 }.toInt() and 1 != 0
                val start = 9; val n = minOf(length * if (wide) 2 else 1,b.size-start)
                put(row,col,String(b,start,n,if (wide) Charsets.UTF_16LE else Charsets.ISO_8859_1))
            }
            6 -> if (Binary.u16(b,12) != 0xffff) put(row,col,number(Binary.double(b,6)))
        }
    }
}
