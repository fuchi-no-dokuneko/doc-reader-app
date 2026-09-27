package app.docreader.format

import app.docreader.domain.*
import java.io.Reader

object HtmlDocument {
    fun parse(reader: Reader, sink: BlockSink, resolve: (String) -> String = { it },
        css: CssSheet = CssSheet(), prefix: String = "") {
        val block=HtmlBlocks(sink); val table=HtmlTable(sink); val styles=StringBuilder()
        var skip=""; var inHead=false
        val blocks=setOf("p","div","section","article","li","pre","blockquote","h1","h2","h3","h4","h5","h6")
        HtmlTokens.read(reader) { token ->
            if (token.tag=="head") { inHead=!token.closing; return@read }
            if (skip.isNotEmpty()) {
                if (token.closing && token.tag==skip) {
                    if (skip=="style") css.add(styles.toString())
                    styles.setLength(0); skip=""
                } else if (skip=="style") styles.append(token.raw)
                return@read
            }
            if (token.tag in setOf("script","style") && !token.closing) { skip=token.tag; return@read }
            if (inHead) return@read
            when {
                token.tag=="table" || table.active -> { block.flush(); table.token(token) }
                token.tag in setOf("img","image") && !token.closing -> {
                    block.flush(); sink.emit(Block(BlockType.IMAGE,token.attrs["alt"].orEmpty(),
                        asset=resolve(HtmlTokens.text(token.attrs["src"] ?: token.attrs["xlink:href"] ?: token.attrs["href"].orEmpty()))))
                }
                token.tag in blocks -> {
                    block.flush()
                    if (!token.closing) {
                        block.tag=token.tag; block.style=css.forTag(token.tag,token.attrs)
                        block.anchor=token.attrs["id"]?.let { "$prefix#$it" }.orEmpty()
                        if (token.tag=="li") { block.text.append("• "); block.html.append("• ") }
                    }
                }
                token.tag.isEmpty() -> { block.text.append(token.raw); block.html.append(token.raw) }
                else -> {
                    val href=token.attrs["href"]
                    if (token.tag=="a" && href!=null && prefix.isNotEmpty())
                        block.html.append("<a href=\"${Markup.escape(XmlFiles.link(prefix,HtmlTokens.text(href)))}\">")
                    else block.html.append(token.raw)
                    if (token.tag=="br") block.text.append('\n')
                    token.attrs["id"]?.let { block.anchor="$prefix#$it" }
                }
            }
            if (block.text.length>=8192) block.flush()
        }
        block.flush()
    }
}
