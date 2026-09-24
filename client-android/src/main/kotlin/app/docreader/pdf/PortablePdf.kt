package app.docreader.pdf

import java.io.File

class PortablePdf(file: File, password: String) : AutoCloseable {
    val source = PdfFile(file)
    val structure: PdfStructure
    init {
        try { PdfCrypt.open(source,password); structure = PdfStructure(source) }
        catch (e: Exception) { source.close(); throw e }
    }
    fun page(number: Int): PdfPageData {
        val dict = structure.page(number)
        val box = (structure.inherited(dict,"CropBox") ?: structure.inherited(dict,"MediaBox")).array()
        val transform = PdfPageTransform(box,structure.inherited(dict,"Rotate").number().toInt())
        val content = PdfContent(source,transform)
        val resources = source.dict(structure.inherited(dict,"Resources"))
        val value = source.resolve(dict["Contents"])
        val streams = if (value is PdfArray) value.values else listOfNotNull(value)
        content.readAll(streams.mapNotNull { source.resolve(it) as? PdfStream },resources)
        val links = source.resolve(dict["Annots"]).array().mapNotNull {
            val annotation = source.dict(it)
            if (annotation.name("Subtype") != "Link") return@mapNotNull null
            val rect = annotation["Rect"].array().map { it.number().toFloat() }
            if (rect.size < 4) return@mapNotNull null
            val action = source.dict(annotation["A"])
            val target = structure.destination(annotation["Dest"] ?: action["D"])
            PdfLink(listOf(transform.box(rect[0],rect[1],rect[2],rect[3])),target,(action["URI"] as? PdfString)?.text().orEmpty())
        }
        return PdfPageData(transform.displayWidth.toInt(),transform.displayHeight.toInt(),content.text,links)
    }
    override fun close() = source.close()
}
