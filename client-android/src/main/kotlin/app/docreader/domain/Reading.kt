package app.docreader.domain

enum class Kind { PDF, EPUB, MARKDOWN, NOTEBOOK, JSON, YAML, CODE, CONFIG,
    TEXT, HTML, CSV, DOCX, XLSX, PPTX, DOC, XLS, PPT, RTF }
enum class Theme { SYSTEM, LIGHT, DARK, SEPIA }
enum class Mode { SCROLL, PAGED, PRECISION }
data class ReadingSettings(
    val theme: Theme = Theme.SYSTEM, val mode: Mode = Mode.PAGED,
    val font: String = "serif", val size: Float = 18f,
    val lineHeight: Float = 1.4f, val margin: Int = 24,
    val encoding: String = "Auto", val jump: Int = 10
)
data class DocumentInfo(val id: String, val title: String, val kind: Kind,
    val size: Long, val source: String = "", val opened: Long = System.currentTimeMillis(),
    val contentHash: String = "")
data class ReadingPosition(val page: Int = 0, val offset: Int = 0, val anchor: Long = 0)
data class Chapter(val title: String, val block: Int, val level: Int = 1)
data class Mark(val id: Long = 0, val document: String, val page: Int,
    val start: Int = 0, val end: Int = 0, val text: String = "", val type: String = "bookmark",
    val bounds: String = "", val anchor: Long = 0)
data class LayoutSpec(val width: Int, val height: Int, val density: Float,
    val scale: Float, val settings: ReadingSettings)
