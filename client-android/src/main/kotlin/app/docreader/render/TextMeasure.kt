package app.docreader.render

import app.docreader.domain.*

class TextMeasure(private val spec: LayoutSpec, private val assets: LocalAssets) : BlockMeasure {
    override fun measure(block: Block): MeasuredBlock {
        if (block.type == BlockType.IMAGE) {
            val (width,height) = assets.dimensions(block.asset)
            val h = (height * BlockText.contentWidth(spec).toFloat() / width.coerceAtLeast(1)).toInt()
                .coerceIn(40,BlockText.contentHeight(spec))
            return MeasuredBlock(listOf(MeasuredLine(0,1,h)),true)
        }
        if (block.type == BlockType.TABLE) {
            val rows = Regex("<tr[ >]",RegexOption.IGNORE_CASE).findAll(block.html).count().coerceAtLeast(1)
            val longest = block.text.lines().maxOfOrNull { it.length } ?: 0
            val wrap = (longest * spec.settings.size * spec.scale / 3 / BlockText.contentWidth(spec)).toInt()+1
            val h = (rows * (spec.settings.size*spec.scale*spec.settings.lineHeight*wrap + 16*spec.density)).toInt()
            return MeasuredBlock(listOf(MeasuredLine(0,block.text.length,h.coerceAtMost(BlockText.contentHeight(spec)))),true)
        }
        val layout = BlockText.layout(block,spec)
        return MeasuredBlock(List(layout.lineCount) { line ->
            MeasuredLine(layout.getLineStart(line),layout.getLineEnd(line),layout.getLineBottom(line)-layout.getLineTop(line))
        })
    }
}
