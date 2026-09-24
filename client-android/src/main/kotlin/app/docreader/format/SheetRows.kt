package app.docreader.format

import app.docreader.domain.*

class SheetRows(private val sink: BlockSink) {
    private var row = -1
    private val cells = sortedMapOf<Int, String>()
    fun cell(index: Int, column: Int, value: String) {
        if (row != index) { flush(); row = index }
        cells[column] = value
        if (cells.size >= 32) flush()
    }
    fun flush() {
        if (cells.isEmpty()) return
        val header = listOf("Row") + cells.keys.map(::columnName)
        val values = listOf((row+1).toString()) + cells.values
        sink.emit(Block(BlockType.TABLE, values.joinToString("\t"), html = Markup.table(listOf(header, values))))
        cells.clear()
    }
    companion object {
        fun column(name: String): Int {
            var index = 0
            for (c in name.takeWhile(Char::isLetter)) index = index * 26 + (c.uppercaseChar()-'A'+1)
            return (index-1).coerceAtLeast(0)
        }
        fun columnName(index: Int): String {
            var n = index+1; val result = StringBuilder()
            while (n > 0) { n--; result.append(('A'.code + n%26).toChar()); n /= 26 }
            return result.reverse().toString()
        }
    }
}
