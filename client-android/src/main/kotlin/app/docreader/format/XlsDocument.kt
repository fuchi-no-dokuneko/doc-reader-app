package app.docreader.format

import app.docreader.domain.*
import java.io.File

object XlsDocument {
    fun parse(file: File, cache: File, sink: BlockSink) = CfbFile(file).use { cfb ->
        val source = cfb.stream("Workbook") ?: cfb.stream("Book") ?: error("Missing Excel workbook")
        val records = BiffRecords(source)
        val names = mutableListOf<String>(); var sheet = -1; var active = false
        val rows = SheetRows(sink)
        DiskStrings(cache).use { strings ->
            var formula: Pair<Int,Int>? = null
            while (true) {
                val record = records.next() ?: break; val b = record.data
                when (record.id) {
                    0x2f -> error("Save this encrypted Excel file without a password first")
                    0x85 -> {
                        val n = b[6].toInt() and 255; val wide = b[7].toInt() and 1 != 0
                        names += String(b,8,n * if (wide) 2 else 1,
                            if (wide) Charsets.UTF_16LE else Charsets.ISO_8859_1)
                    }
                    0xfc -> {
                        val count = Binary.i32(b,4); require(count in 0..10_000_000)
                        val input = BiffStringInput(records,b)
                        repeat(count) { strings.add(input.string()) }
                    }
                    0x809 -> {
                        active = Binary.u16(b,2) == 0x10
                        if (active) { rows.flush(); sheet++
                            sink.emit(Block(BlockType.HEADING,names.getOrElse(sheet) { "Sheet ${sheet+1}" },1)) }
                    }
                    0xa -> { rows.flush(); active = false }
                    0x207 -> if (active && formula != null) {
                        val length = Binary.u16(b,0); val wide = b[2].toInt() and 1 != 0
                        val value = String(b,3,minOf(b.size-3,length * if (wide) 2 else 1),
                            if (wide) Charsets.UTF_16LE else Charsets.ISO_8859_1)
                        rows.cell(formula!!.first,formula!!.second,value); formula = null
                    }
                    else -> if (active) {
                        XlsCells.parse(record.id,b,strings,rows::cell)
                        if (record.id == 6 && b.size >= 14 && Binary.u16(b,12) == 0xffff)
                            formula = Binary.u16(b,0) to Binary.u16(b,2)
                    }
                }
            }
            rows.flush()
        }
    }
}
