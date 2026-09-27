package app.docreader.ui

import android.content.*
import android.view.*
import app.docreader.domain.Mark
import app.docreader.pdf.*
import kotlinx.coroutines.launch

class PdfSelection(private val pane: ReaderPane, private val number: Int, data: PdfPageData) {
    data class Glyph(val text: String, val bounds: PdfBox)
    val glyphs=data.text.flatMap { run ->
        if (run.bounds.size==run.text.length) run.text.mapIndexed { i,c -> Glyph(c.toString(),run.bounds[i]) }
        else run.bounds.mapIndexed { i,box ->
            val start=run.text.length*i/run.bounds.size; val end=run.text.length*(i+1)/run.bounds.size
            Glyph(run.text.substring(start,end),box)
        }
    }
    var start=-1; var end=-1
    fun hit(x: Float,y: Float)=glyphs.indexOfFirst { it.bounds.contains(x,y) }
    fun boxes(): List<PdfBox> = if (start<0 || end<0) emptyList() else
        glyphs.subList(minOf(start,end),maxOf(start,end)+1).map { it.bounds }
    private fun text()=glyphs.subList(minOf(start,end),maxOf(start,end)+1).joinToString("") { it.text }
    fun actions(view: View) {
        if (start<0 || end<0) return
        view.startActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.add(0,1,0,"Copy"); menu.add(0,2,1,"Highlight"); return true
            }
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu)=false
            override fun onDestroyActionMode(mode: ActionMode) { start=-1; end=-1; view.invalidate() }
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when(item.itemId) {
                    1 -> (pane.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("PDF selection",text()))
                    2 -> {
                        val selected=text()
                        val bounds=boxes().joinToString(";") { "${it.left},${it.top},${it.right},${it.bottom}" }
                        pane.scope.launch {
                        val tab=pane.model.tab(pane.id) ?: return@launch
                        pane.model.repo.addMark(Mark(document=tab.document.id,page=number,text=selected,type="highlight",bounds=bounds,anchor=number.toLong()))
                        pane.model.change(pane.id) { it.copy(revision=it.revision+1) }
                    } }
                    else -> return false
                }; mode.finish(); return true
            }
        },ActionMode.TYPE_FLOATING)
    }
}
