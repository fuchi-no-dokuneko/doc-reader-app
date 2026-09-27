package app.docreader.format

import app.docreader.domain.*

class MarkdownLines(private val sink: BlockSink, private val asset: (String) -> String) {
    var fence=""
    private var language=""
    fun emit(text: String) {
        val trim=text.trim()
        val marker=Regex("^(`{3,}|~{3,})(.*)$").matchEntire(trim)
        if (marker!=null && (fence.isEmpty() || marker.groupValues[1].first()==fence.first() && marker.groupValues[1].length>=fence.length)) {
            if (fence.isEmpty()) { fence=marker.groupValues[1]; language=marker.groupValues[2].trim() } else fence=""
            return
        }
        if (fence.isNotEmpty() || text.startsWith("    ")) {
            sink.emit(Block(BlockType.CODE,if (fence.isEmpty()) text.drop(4) else text,language=language)); return
        }
        val heading=Regex("^(#{1,6}) +(.+?)(?: +#+)?$").matchEntire(trim)
        if (heading!=null) {
            val title=heading.groupValues[2]
            sink.emit(Block(BlockType.HEADING,title,heading.groupValues[1].length,html=Markup.inline(title),anchor=slug(title))); return
        }
        if (trim.matches(Regex("(?:[-*_]\\s*){3,}"))) { sink.emit(Block(BlockType.RULE,"―")); return }
        val pattern=Regex("!\\[([^]]*)]\\((<[^>]+>|[^)]+)\\)")
        val images=pattern.findAll(text).toList()
        var body=pattern.replace(text,"")
        body=body.replace(Regex("^(\\s*)[-*+] +")) { it.groupValues[1]+"• " }
        body=body.replace(Regex("^(\\s*)• \\[([ xX])] +")) { it.groupValues[1]+if (it.groupValues[2]==" ") "☐ " else "☑ " }
        if (body.isNotEmpty() || images.isEmpty()) sink.emit(Block(text=body,
            html=if (body.trimStart().startsWith('>')) "<blockquote>${Markup.inline(body.trimStart().drop(1))}</blockquote>" else Markup.inline(body)))
        images.forEach { sink.emit(Block(BlockType.IMAGE,it.groupValues[1],asset=asset(it.groupValues[2].substringBefore(" \"").trim('<','>')))) }
    }
    companion object { fun slug(text: String)=text.lowercase().replace(Regex("[^\\p{L}\\p{N} _-]"),"").replace(' ','-') }
}
