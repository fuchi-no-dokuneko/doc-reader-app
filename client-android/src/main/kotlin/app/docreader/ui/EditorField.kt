package app.docreader.ui

import android.graphics.Typeface
import android.text.*
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText

object EditorField {
    fun create(ui: UiKit,session: EditSession) = EditText(ui.context).apply {
        contentDescription="Source code editor"
        gravity=Gravity.TOP or Gravity.START; typeface=Typeface.MONOSPACE; textSize=16f
        setTextColor(ui.colors.ink); setBackgroundColor(ui.colors.paper)
        setPadding(ui.dp(16),ui.dp(12),ui.dp(16),ui.dp(48))
        inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        typeface=Typeface.MONOSPACE
        imeOptions=EditorInfo.IME_FLAG_NO_EXTRACT_UI
        setHorizontallyScrolling(true); setText(session.text)
        isSaveEnabled=false
        setSelection(session.start.coerceIn(0,length()),session.end.coerceIn(0,length()))
        if (session.source.newline=="\r\n") filters=arrayOf(InputFilter { source,start,end,_,_,_ ->
            if (source.subSequence(start,end).toString()=="\n") "\r\n" else null
        })
        post { scrollTo(0,session.scroll) }
    }
}
