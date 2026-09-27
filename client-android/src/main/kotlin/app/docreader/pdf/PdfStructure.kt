package app.docreader.pdf

class PdfStructure(val file: PdfFile) {
    val root = file.dict(file.trailer["Root"])
    val pages = mutableListOf<PdfRef>()
    init {
        val seen = hashSetOf<Int>()
        fun walk(value: PdfValue?, depth: Int) {
            require(depth < 100)
            val ref = value as? PdfRef ?: return
            if (!seen.add(ref.id)) return
            val dict = file.dict(ref)
            if (dict.name("Type") == "Page") pages += ref
            else dict["Kids"].array().forEach { walk(it,depth+1) }
        }
        walk(root["Pages"],0)
    }
    fun page(number: Int) = file.dict(pages[number])
    fun inherited(page: PdfDict, name: String): PdfValue? {
        var current = page; val seen = hashSetOf<Int>()
        while (true) {
            current[name]?.let { return file.resolve(it) }
            val ref = current["Parent"] as? PdfRef ?: return null
            if (!seen.add(ref.id)) return null
            current = file.dict(ref)
        }
    }
    fun destination(value: PdfValue?): Int {
        val resolved = file.resolve(value)
        if (resolved is PdfArray) return when (val first = resolved.values.firstOrNull()) {
            is PdfRef -> pages.indexOfFirst { it.id == first.id }
            is PdfNumber -> first.number.toInt()
            else -> -1
        }
        if (resolved is PdfDict) return destination(resolved["D"])
        val name = if (resolved is PdfName) resolved.name else (resolved as? PdfString)?.text() ?: return -1
        file.dict(root["Dests"])[name]?.let { return destination(it) }
        val tree = file.dict(file.dict(root["Names"])["Dests"])
        fun find(node: PdfDict, depth: Int): PdfValue? {
            if (depth > 50) return null
            val entries = node["Names"].array()
            for (i in entries.indices step 2) if ((entries[i] as? PdfString)?.text() == name) return entries.getOrNull(i+1)
            for (kid in node["Kids"].array()) find(file.dict(kid),depth+1)?.let { return it }
            return null
        }
        return find(tree,0)?.let { destination(it) } ?: -1
    }
    fun chapters(): List<PdfChapter> {
        val result = mutableListOf<PdfChapter>(); val seen = hashSetOf<Int>()
        fun walk(value: PdfValue?, level: Int) {
            if (level > 64) return
            var ref = value as? PdfRef
            while (ref != null && seen.add(ref.id)) {
                val item = file.dict(ref); val page = destination(item["Dest"] ?: file.dict(item["A"])["D"])
                if (page >= 0) result += PdfChapter((item["Title"] as? PdfString)?.text().orEmpty(),page,level)
                walk(item["First"],level+1); ref = item["Next"] as? PdfRef
            }
        }
        walk(file.dict(root["Outlines"])["First"],0); return result
    }
}
