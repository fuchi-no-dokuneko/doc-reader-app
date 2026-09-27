package app.docreader.domain

data class MeasuredLine(val start: Int, val end: Int, val height: Int)
data class MeasuredBlock(val lines: List<MeasuredLine>, val atomic: Boolean = false)
fun interface BlockMeasure { fun measure(block: Block): MeasuredBlock }

class Pagination(private val height: Int, private val gap: Int, private val measure: BlockMeasure) {
    fun build(source: BlockSource, collapsed: Set<Int>, emit: (List<PageSlice>) -> Unit) {
        val page = mutableListOf<PageSlice>(); var used = 0; var hidden: Int? = null
        fun flush() { if (page.isNotEmpty()) emit(page.toList()); page.clear(); used = 0 }
        for (index in 0 until source.count) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            val block = source.block(index)
            if (hidden != null) { if (block.level > hidden || block.text.isBlank()) continue; hidden = null }
            if (index in collapsed) hidden = block.level
            val measured = measure.measure(block); val lines = measured.lines
            if (lines.isEmpty()) continue
            val total = lines.sumOf { it.height }
            if (block.type == BlockType.HEADING && used > 0 && height-used < total+lines[0].height*2+gap) flush()
            if (measured.atomic) {
                if (used > 0 && used + total > height) flush()
                page += PageSlice(index,lines.first().start,lines.last().end)
                used += minOf(total,height)+gap
            } else {
                var start = 0
                while (start < lines.size) {
                    var end = start; var taken = 0
                    while (end < lines.size && used+taken+lines[end].height <= height) taken += lines[end++].height
                    if (end == start) { if (used > 0) { flush(); continue }; end++; taken = lines[start].height }
                    if (end < lines.size && end-start == 1 && used > 0) { flush(); continue }
                    if (lines.size-end == 1 && end-start > 2) { end--; taken -= lines[end].height }
                    page += PageSlice(index,lines[start].start,lines[end-1].end); used += taken+gap
                    start = end
                    if (start < lines.size) flush()
                }
            }
        }
        flush()
    }
}
