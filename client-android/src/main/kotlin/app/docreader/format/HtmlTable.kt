package app.docreader.format

import app.docreader.domain.*

class HtmlTable(private val sink: BlockSink) {
    private val row=StringBuilder(); private val plain=StringBuilder(); private var header=""
    private var depth=0; private var rowCount=0
    val active get()=depth>0
    fun token(token: HtmlTokens.Token) {
        if (token.tag=="table") {
            if (!token.closing) { depth++; if (depth==1) { header=""; rowCount=0; return } }
            else if (--depth==0) {
                if (rowCount==0 && header.isNotEmpty()) sink.emit(Block(BlockType.TABLE,html="<table>$header</table>"))
                return
            }
        }
        row.append(token.raw)
        if (token.tag.isEmpty()) plain.append(HtmlTokens.text(token.raw))
        if (token.tag=="td" && token.closing) plain.append('\t')
        if (token.tag=="tr" && token.closing && depth==1) {
            if (row.contains("<th") && rowCount==0) header+=row.toString()
            else {
                sink.emit(Block(BlockType.TABLE,plain.toString(),html="<table>$header$row</table>")); rowCount++
            }
            row.setLength(0); plain.setLength(0)
        }
    }
}
