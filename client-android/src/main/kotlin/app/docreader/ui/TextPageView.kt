package app.docreader.ui

import android.graphics.Bitmap
import android.view.*
import android.widget.*
import app.docreader.domain.*
import app.docreader.render.*

object TextPageView {
    fun create(pane: ReaderPane, spec: LayoutSpec, blocks: List<Pair<PageSlice,Block>>, images: Map<String,Bitmap?>,
        marks: List<Mark>, webs: MutableList<android.webkit.WebView>, heights: Map<Int,Int>): View {
        val column=pane.ui.column(); val margin=(spec.settings.margin*spec.density).toInt()
        column.setPadding(margin,margin,margin,margin)
        blocks.forEach { (slice,block) ->
            val view: View=when (block.type) {
                BlockType.IMAGE -> {
                    val bitmap=images[block.asset]
                    if (bitmap != null) ImageView(pane.context).apply {
                        setImageBitmap(bitmap); scaleType=ImageView.ScaleType.FIT_CENTER
                        contentDescription=block.text.ifEmpty { "Document image" }
                    } else if (block.asset.endsWith(".svg")) RichWeb.create(pane,block,webs)
                    else pane.ui.text("${block.text}\nImage unavailable. Grant access to its folder from Reading → Linked images.",12f)
                }
                BlockType.TABLE -> RichWeb.create(pane,block,webs)
                else -> SelectableText.create(pane,spec,slice,block,marks)
            }
            val atomic=block.type in setOf(BlockType.TABLE,BlockType.IMAGE)
            val height=if (atomic) heights[slice.block] ?: 120 else -2
            column.addView(view,LinearLayout.LayoutParams(-1,height).apply { bottomMargin=(6*spec.density).toInt() })
        }
        return column
    }
}
