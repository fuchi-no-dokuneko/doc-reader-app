package app.docreader.render

import android.text.*
import android.text.style.*
import app.docreader.domain.*

object TextContent {
    fun styled(block: Block,spec: LayoutSpec,dark: Boolean): CharSequence {
        val text=SpannableString(BlockText.text(block,dark))
        val indent=CssText.indent(block,spec)
        if (indent!=0) text.setSpan(LeadingMarginSpan.Standard(indent,0),0,text.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val style=CssText.properties(block)
        if (style["text-decoration"]?.contains("underline")==true)
            text.setSpan(UnderlineSpan(),0,text.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (style["text-decoration"]?.contains("line-through")==true)
            text.setSpan(StrikethroughSpan(),0,text.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return text
    }
    fun alignment(view: android.widget.TextView,block: Block) {
        view.gravity=when(CssText.alignment(block)) {
            Layout.Alignment.ALIGN_CENTER -> android.view.Gravity.CENTER_HORIZONTAL
            Layout.Alignment.ALIGN_OPPOSITE -> android.view.Gravity.END
            else -> android.view.Gravity.START
        }
        if (android.os.Build.VERSION.SDK_INT>=26)
            view.justificationMode=if (CssText.properties(block)["text-align"]=="justify")
                android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD else android.graphics.text.LineBreaker.JUSTIFICATION_MODE_NONE
    }
}
