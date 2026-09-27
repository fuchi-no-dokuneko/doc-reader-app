package app.docreader.ui

import android.text.*
import android.widget.*
import app.docreader.MainActivity
import app.docreader.domain.EditableFiles
import app.docreader.render.Palette
import androidx.core.graphics.ColorUtils

class EditorView(activity: MainActivity,private val model: ReaderModel,val session: EditSession) {
    private val colors=EditorPalette.from(Palette.forTheme(activity,model.state.value.settings.theme))
    private val ui=UiKit(activity,colors)
    val view=ui.column().apply { setBackgroundColor(colors.paper) }
    private val title=ui.text("",16f,true)
    private val status=ui.text("",12f)
    val field=EditorField.create(ui,session)
    private val actions=EditorActions(activity,model)
    private val save=ui.button("Save") { actions.save() }
    private val copy=ui.button("Save a copy") { actions.save(copy=true) }
    private val read=ui.button("Read") { actions.close() }
    private val tab=ui.button("Tab") {
        if (model.state.value.editBusy.isEmpty()) field.text.replace(
            minOf(field.selectionStart,field.selectionEnd).coerceAtLeast(0),
            maxOf(field.selectionStart,field.selectionEnd).coerceAtLeast(0),"    ")
    }
    private val highlighter=EditorHighlight(field,EditableFiles.language(session.document),ColorUtils.calculateLuminance(colors.paper)<.5)
    init {
        view.addView(title.apply { setBackgroundColor(colors.surface) })
        val tools=ui.row().apply {
            addView(read); addView(save); addView(copy)
            addView(tab)
        }
        view.addView(HorizontalScrollView(activity).apply { addView(tools) })
        view.addView(status)
        view.addView(field,LinearLayout.LayoutParams(-1,0,1f))
        field.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                session.text=text.toString(); highlighter.refresh(); render()
            }
        })
        highlighter.refresh(); render()
    }
    fun render() {
        val busy=model.state.value.editBusy
        val language=EditableFiles.language(session.document)
        if (highlighter.language!=language) { highlighter.language=language; highlighter.refresh() }
        title.text="EDIT MODE · ${session.document.title}"
        status.text=busy.ifEmpty { "${if (session.dirty) "Unsaved changes" else "Saved"} · ${session.source.encoding}" }
        field.isEnabled=busy.isEmpty(); save.isEnabled=busy.isEmpty(); copy.isEnabled=busy.isEmpty(); read.isEnabled=busy.isEmpty()
        tab.isEnabled=busy.isEmpty()
    }
    fun dispose() {
        session.start=field.selectionStart; session.end=field.selectionEnd; session.scroll=field.scrollY
        highlighter.close()
    }
}
