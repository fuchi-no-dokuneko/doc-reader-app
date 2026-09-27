package app.docreader.render

import android.text.Layout
import app.docreader.domain.*
import app.docreader.format.CssSheet

object CssText {
    fun properties(block: Block)=CssSheet.properties(block.style)
    fun size(value: String?, base: Float, density: Float): Float? {
        val match=Regex("([0-9.]+)(em|rem|%|px|pt)?").matchEntire(value.orEmpty().trim()) ?: return null
        val n=match.groupValues[1].toFloatOrNull() ?: return null
        return when(match.groupValues[2]) { "em","rem" -> n*base; "%" -> n*base/100
            "pt" -> n*density*4/3; "px" -> n*density; else -> n*base }
    }
    fun line(block: Block, spec: LayoutSpec): Float {
        val value=properties(block)["line-height"] ?: return spec.settings.lineHeight
        val base=BlockText.size(block,spec)
        return ((size(value,base,spec.density) ?: base)/base).coerceIn(1f,3f)*spec.settings.lineHeight/1.4f
    }
    fun alignment(block: Block)=when(properties(block)["text-align"]) {
        "center" -> Layout.Alignment.ALIGN_CENTER; "right","end" -> Layout.Alignment.ALIGN_OPPOSITE
        else -> Layout.Alignment.ALIGN_NORMAL
    }
    fun indent(block: Block,spec: LayoutSpec)=(size(properties(block)["text-indent"],BlockText.size(block,spec),spec.density) ?: 0f).toInt()
}
