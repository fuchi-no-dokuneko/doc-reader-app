package app.docreader.domain

object EditableFiles {
    fun supports(kind: Kind) = kind in setOf(Kind.CODE,Kind.CONFIG,Kind.JSON,Kind.YAML,
        Kind.HTML,Kind.MARKDOWN,Kind.TEXT,Kind.CSV,Kind.NOTEBOOK)
    fun language(info: DocumentInfo) = when (info.kind) {
        Kind.NOTEBOOK,Kind.JSON -> "json"
        Kind.YAML -> "yaml"
        else -> Formats.language(info.title)
    }
}
