package app.docreader.ui

import app.docreader.domain.DocumentInfo

object EditedTabs {
    suspend fun apply(model: ReaderModel,session: EditSession,doc: DocumentInfo,copy: Boolean) {
        require(copy || doc.id==session.document.id) {
            "Save cannot change the original document identity. Your edits are still open."
        }
        model.preferences.encoding(doc.id,session.source.encoding)
        if (copy) {
            // A destination already in the library may also have open readers.
            TabRefresh.apply(model,doc)
            TabRefresh.apply(model,doc,session.tab)
        } else TabRefresh.apply(model,doc)
    }
}
