package app.docreader.format

object Markup {
    fun escape(text: String) = text.replace("&", "&amp;").replace("<", "&lt;")
        .replace(">", "&gt;").replace("\"", "&quot;")
    fun inline(text: String): String {
        var html = escape(text)
        html = Regex("`([^`]+)`").replace(html) { "<code>${it.groupValues[1]}</code>" }
        html = Regex("\\*\\*(.+?)\\*\\*|__(.+?)__").replace(html) {
            "<b>${it.groupValues[1].ifEmpty { it.groupValues[2] }}</b>"
        }
        html = Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)").replace(html) { "<i>${it.groupValues[1]}</i>" }
        html = Regex("~~(.+?)~~").replace(html) { "<s>${it.groupValues[1]}</s>" }
        html = Regex("(?<!!)\\[([^]]+)]\\(([^)]+)\\)").replace(html) {
            "<a href=\"${it.groupValues[2].substringBefore(' ')}\">${it.groupValues[1]}</a>"
        }
        return html
    }
    fun table(rows: List<List<String>>, header: Boolean = true): String = buildString {
        append("<table>")
        rows.forEachIndexed { i, row ->
            append("<tr>"); val tag = if (header && i == 0) "th" else "td"
            row.forEach { append("<$tag>${inline(it)}</$tag>") }; append("</tr>")
        }
        append("</table>")
    }
}
