package app.docreader.pdf

data class PdfBox(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
}
data class PdfText(val text: String, val bounds: List<PdfBox>)
data class PdfLink(val bounds: List<PdfBox>, val page: Int = -1, val url: String = "")
data class PdfChapter(val title: String, val page: Int, val level: Int)
data class PdfPageData(val width: Int, val height: Int, val text: List<PdfText>, val links: List<PdfLink>)
