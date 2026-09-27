package app.docreader.format

import app.docreader.domain.*
import java.io.File

object DocumentParser {
    fun parse(file: File, info: DocumentInfo, encoding: String, cache: File, sink: BlockSink,
        resolve: (String) -> String = { it }): String {
        when (info.kind) {
            Kind.EPUB -> EpubDocument.parse(file,sink)
            Kind.DOCX -> DocxDocument.parse(file,sink)
            Kind.XLSX -> XlsxDocument.parse(file,sink,cache)
            Kind.PPTX -> PptxDocument.parse(file,sink)
            Kind.DOC -> DocDocument.parse(file,sink)
            Kind.XLS -> XlsDocument.parse(file,cache,sink)
            Kind.PPT -> PptDocument.parse(file,sink)
            Kind.RTF -> file.reader(Charsets.ISO_8859_1).use { RtfDocument.parse(it,sink) }
            Kind.PDF -> error("PDF uses the native renderer")
            else -> {
                val (reader, detected) = Encodings.open(file,encoding)
                reader.use {
                    when (info.kind) {
                        Kind.MARKDOWN -> MarkdownDocument.parse(it,sink,resolve)
                        Kind.NOTEBOOK -> NotebookDocument.parse(it,sink,AssetFiles(cache))
                        Kind.HTML -> HtmlDocument.parse(it,sink,resolve)
                        Kind.CSV -> CsvDocument.parse(it,sink,if (info.title.endsWith(".tsv")) '\t' else ',')
                        Kind.JSON -> if (info.title.endsWith(".jsonc")) SourceParser.parse(it,"json",sink)
                            else JsonDocument.parse(it,sink)
                        else -> SourceParser.parse(it,Formats.language(info.title),sink,info.kind == Kind.TEXT)
                    }
                }
                return detected
            }
        }
        return "Embedded"
    }
}
