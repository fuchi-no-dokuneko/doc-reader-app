package app.docreader.render

import app.docreader.domain.*
import app.docreader.data.*
import java.io.File

class TextBook(val blocks: DiskBlocks, val encoding: String, private val directory: File,
    private val assets: LocalAssets) : AutoCloseable {
    var pages: DiskPages? = null
        private set
    var spec: LayoutSpec? = null
        private set
    val collapsed = mutableSetOf<Int>()
    fun paginate(layout: LayoutSpec, progress: (Int) -> Unit = {}) {
        pages?.close(); directory.mkdirs()
        val index = DiskPages(directory); pages = index; spec = layout
        Pagination(BlockText.contentHeight(layout),(6*layout.density).toInt(),TextMeasure(layout,assets))
            .build(blocks,collapsed) { slices ->
                index.add(slices)
                if (index.count % 100 == 0) progress(index.count)
            }
        if (index.count == 0) index.add(listOf(PageSlice(0,0,0)))
    }
    fun page(number: Int): List<Pair<PageSlice,Block>> {
        val index = pages ?: return emptyList()
        if (blocks.count == 0) return listOf(PageSlice(0,0,0) to Block(text="Empty document"))
        return index.page(number).slices.map { it to blocks.block(it.block) }
    }
    override fun close() { pages?.close(); blocks.close(); directory.deleteRecursively() }
}
