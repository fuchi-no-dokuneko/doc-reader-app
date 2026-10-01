package app.docreader.ui

import app.docreader.data.SourceText
import app.docreader.domain.DocumentInfo

class EditSession(val tab: String,var document: DocumentInfo,val source: SourceText) {
    var text=source.text
    var saved=source.text
    var baselineHash=document.contentHash
    var start=0
    var end=0
    var scroll=0
    val dirty get()=text!=saved
}
