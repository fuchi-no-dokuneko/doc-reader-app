package app.docreader.format

import app.docreader.domain.*
import java.io.PushbackReader
import java.io.Reader

object CsvDocument {
    fun parse(reader: Reader, sink: BlockSink, delimiter: Char = ',') {
        val input = PushbackReader(reader, 1); val cell = StringBuilder()
        val row = mutableListOf<String>(); var quoted = false; var header: List<String>? = null
        fun emit() {
            row += cell.toString(); cell.setLength(0)
            if (header == null) header = row.toList()
            val lines = if (header == row) listOf(row.toList()) else listOf(header!!, row.toList())
            sink.emit(Block(BlockType.TABLE, row.joinToString("\t"), html = Markup.table(lines)))
            row.clear()
        }
        while (true) {
            val value = input.read(); if (value < 0) break
            val char = value.toChar()
            if (char == '"') {
                if (quoted) {
                    val next = input.read()
                    if (next == '"'.code) cell.append('"')
                    else { quoted = false; if (next >= 0) input.unread(next) }
                } else if (cell.isEmpty()) quoted = true else cell.append(char)
            } else if (char == delimiter && !quoted) { row += cell.toString(); cell.setLength(0) }
            else if (char == '\n' && !quoted) emit()
            else if (char != '\r' || quoted) cell.append(char)
            if (cell.length > 65536) {
                row += cell.toString(); cell.setLength(0)
                sink.emit(Block(BlockType.TABLE, row.joinToString("\t"), html = Markup.table(listOf(row), false))); row.clear()
            }
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) emit()
    }
}
