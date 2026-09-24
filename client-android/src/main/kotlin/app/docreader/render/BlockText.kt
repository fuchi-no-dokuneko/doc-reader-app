package app.docreader.render

import android.graphics.Typeface
import android.text.*
import android.text.style.*
import app.docreader.domain.*

object BlockText {
    fun text(block: Block, dark: Boolean): CharSequence = when {
        block.type == BlockType.CODE -> Syntax.color(block.text.ifEmpty { " " },dark)
        block.html.isNotEmpty() && block.type != BlockType.TABLE ->
            Html.fromHtml(block.html,Html.FROM_HTML_MODE_COMPACT).trimEnd()
        else -> block.text.ifEmpty { " " }
    }
    fun size(block: Block, spec: LayoutSpec): Float {
        val multiplier = when (block.type) { BlockType.HEADING -> (1.6f-(block.level-1)*0.12f).coerceAtLeast(1.05f)
            BlockType.CODE -> 0.88f; else -> 1f }
        val base=spec.settings.size*spec.scale*multiplier
        return CssText.size(CssText.properties(block)["font-size"],base,spec.density)?.coerceIn(base*.6f,base*3f) ?: base
    }
    fun typeface(block: Block, spec: LayoutSpec): Typeface {
        val css=CssText.properties(block)
        val family = if (block.type == BlockType.CODE) "monospace" else
            if (spec.settings.font=="serif") css["font-family"]?.substringBefore(',')?.trim(' ', '\'', '"') ?: "serif" else spec.settings.font
        val bold = block.type == BlockType.HEADING || "bold" in block.style || (css["font-weight"]?.toIntOrNull() ?: 0)>=600
        val italic = "italic" in block.style
        return Typeface.create(family,when { bold && italic -> Typeface.BOLD_ITALIC; bold -> Typeface.BOLD; italic -> Typeface.ITALIC; else -> Typeface.NORMAL })
    }
    fun layout(block: Block, spec: LayoutSpec, text: CharSequence = TextContent.styled(block,spec,false)): StaticLayout {
        val paint = TextPaint(3).apply { textSize = size(block,spec); typeface = typeface(block,spec) }
        return StaticLayout.Builder.obtain(text,0,text.length,paint,contentWidth(spec))
            .setIncludePad(false).setLineSpacing(0f,CssText.line(block,spec)).setAlignment(CssText.alignment(block))
            .setBreakStrategy(android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
            .setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NORMAL).build()
    }
    fun contentWidth(spec: LayoutSpec) = (spec.width-spec.settings.margin*spec.density*2).toInt().coerceAtLeast(80)
    fun contentHeight(spec: LayoutSpec) = (spec.height-56*spec.density-spec.settings.margin*spec.density*2).toInt().coerceAtLeast(120)
}
