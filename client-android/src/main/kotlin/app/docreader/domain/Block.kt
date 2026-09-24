package app.docreader.domain

enum class BlockType { TEXT, HEADING, CODE, IMAGE, TABLE, HTML, RULE }
data class Block(
    val type: BlockType = BlockType.TEXT,
    val text: String = "", val level: Int = 0,
    val language: String = "", val asset: String = "",
    val html: String = "", val anchor: String = "",
    val fold: Int = 0, val style: String = ""
)
data class PageSlice(val block: Int, val start: Int, val end: Int)
data class PageRef(val slices: List<PageSlice>, val anchor: Long)
interface BlockSink { fun emit(block: Block) }
interface BlockSource : AutoCloseable {
    val count: Int
    fun block(index: Int): Block
    val chapters: List<Chapter>
}
interface DocumentRepository {
    suspend fun documents(): List<DocumentInfo>
    suspend fun remember(document: DocumentInfo)
    suspend fun remove(id: String)
    suspend fun position(document: String, tab: String): ReadingPosition
    suspend fun savePosition(document: String, tab: String, position: ReadingPosition)
    suspend fun marks(document: String): List<Mark>
    suspend fun addMark(mark: Mark)
    suspend fun removeMark(id: Long)
}
