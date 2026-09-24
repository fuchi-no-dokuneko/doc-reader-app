package app.docreader

import app.docreader.domain.*
import app.docreader.data.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class PaginationTest {
    @get:Rule val temporary=TemporaryFolder()
    private val measure=BlockMeasure { b -> MeasuredBlock(b.text.indices.map { MeasuredLine(it,it+1,10) },b.type==BlockType.IMAGE) }
    @Test fun headingAndWidowOrphanRules() {
        val blocks=Blocks().apply {
            emit(Block(text="aaaaaa")); emit(Block(BlockType.HEADING,"HH",1)); emit(Block(text="123456"))
        }
        val pages=mutableListOf<List<PageSlice>>()
        Pagination(90,0,measure).build(blocks,emptySet(),pages::add)
        assertEquals(2,pages.size)
        assertEquals(listOf(PageSlice(0,0,6)),pages[0])
        assertEquals(1,pages[1].first().block)
        val orphan=Blocks().apply { emit(Block(text="1234567890")) }
        val divided=mutableListOf<List<PageSlice>>()
        Pagination(90,0,measure).build(orphan,emptySet(),divided::add)
        assertEquals(8,divided[0].single().end)
        assertEquals(8,divided[1].single().start)
    }
    @Test fun sixThousandPagesUseDiskIndexAndLazyBlockReads() {
        var reads=0
        val source=object : BlockSource {
            override val count=6001
            override val chapters=emptyList<Chapter>()
            override fun block(index: Int): Block { reads++; return Block(BlockType.IMAGE,"page-$index") }
            override fun close() {}
        }
        val atomic=BlockMeasure { MeasuredBlock(listOf(MeasuredLine(0,1,100)),true) }
        DiskPages(temporary.newFolder()).use { pages ->
            Pagination(100,0,atomic).build(source,emptySet(),pages::add)
            assertEquals(6001,pages.count); assertEquals(6001,reads)
            assertEquals(5999,pages.page(5999).slices.single().block)
            assertEquals(4500,pages.find(4500L shl 32))
        }
    }
    @Test fun foldingSkipsChildrenAndBlankLinesOnly() {
        val source=Blocks().apply { emit(Block(BlockType.CODE,"A",fold=1)); emit(Block(BlockType.CODE,"b",level=1))
            emit(Block(BlockType.CODE,"")); emit(Block(BlockType.CODE,"c",level=1)); emit(Block(BlockType.CODE,"D")) }
        val pages=mutableListOf<List<PageSlice>>()
        Pagination(100,0,measure).build(source,setOf(0),pages::add)
        assertEquals(listOf(0,4),pages.flatten().map { it.block })
    }
}
