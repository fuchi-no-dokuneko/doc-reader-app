package app.docreader.ui

import android.text.*
import android.text.style.*
import android.view.*
import android.widget.TextView
import app.docreader.domain.*
import app.docreader.render.*

object SelectableText {
    fun create(pane: ReaderPane, spec: LayoutSpec, slice: PageSlice, block: Block, marks: List<Mark>): TextView {
        val full=TextContent.styled(block,spec,android.graphics.Color.luminance(pane.ui.colors.paper)<.4)
        val start=slice.start.coerceIn(0,full.length); val end=slice.end.coerceIn(start,full.length)
        val value=SpannableString(full.subSequence(start,end))
        marks.filter { it.type=="highlight" && (it.anchor shr 32).toInt()==slice.block }.forEach {
            val a=it.start-start; val b=it.end-start
            if (a<value.length && b>0) value.setSpan(BackgroundColorSpan(0x66ffc857),a.coerceAtLeast(0),b.coerceAtMost(value.length),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return TextView(pane.context).apply {
            text=value; setTextColor(pane.ui.colors.ink); setTextIsSelectable(true); includeFontPadding=false
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,BlockText.size(block,spec))
            typeface=BlockText.typeface(block,spec); setLineSpacing(0f,app.docreader.render.CssText.line(block,spec))
            TextContent.alignment(this,block)
            breakStrategy=android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
            hyphenationFrequency=Layout.HYPHENATION_FREQUENCY_NORMAL
            if (block.fold>0) {
                contentDescription="Fold / unfold: ${block.text}"
                setOnClickListener { if (selectionStart==selectionEnd) {
                    PageSurface.consume(this); pane.model.reading.fold(pane.id,slice.block)
                } }
            }
            customSelectionActionModeCallback=object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    menu.add(0,101,0,"Highlight")
                    if (block.fold>0) menu.add(0,102,1,"Fold / Unfold")
                    return true
                }
                override fun onPrepareActionMode(mode: ActionMode, menu: Menu)=false
                override fun onDestroyActionMode(mode: ActionMode) {}
                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    when(item.itemId) {
                        101 -> { val a=minOf(selectionStart,selectionEnd).coerceAtLeast(0); val b=maxOf(selectionStart,selectionEnd).coerceAtLeast(a)
                            pane.model.reading.highlight(pane.id,slice,a+start,b+start,text.subSequence(a,b).toString()) }
                        102 -> pane.model.reading.fold(pane.id,slice.block)
                        else -> return false
                    }; mode.finish(); return true
                }
            }
            movementMethod=ReaderLinks(pane)
        }
    }
}
