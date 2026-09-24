package app.docreader.format

class CssSheet(css: String = "") {
    private val rules = linkedMapOf<String, String>()
    init { add(css) }
    fun add(css: String) {
        Regex("([^{}]+)\\{([^{}]*)}").findAll(css.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), ""))
            .forEach { rule -> rule.groupValues[1].split(',').forEach { rules[it.trim()] = rule.groupValues[2].trim() } }
    }
    fun forTag(tag: String, attrs: Map<String, String>): String {
        val classes = attrs["class"].orEmpty().split(' ')
        val selectors = listOf("*", "body", tag) + classes.flatMap { listOf(".$it", "$tag.$it") } +
            listOf("#${attrs["id"].orEmpty()}")
        return (selectors.mapNotNull(rules::get) + attrs["style"].orEmpty()).joinToString(";")
    }
    companion object {
        fun properties(css: String) = css.split(';').mapNotNull {
            val split = it.indexOf(':')
            if (split < 0) null else it.take(split).trim().lowercase() to it.drop(split+1).trim()
        }.toMap()
    }
}
