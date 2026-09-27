package app.docreader.ui

import app.docreader.domain.*

data class ReaderTab(val id: String, val document: DocumentInfo,
    val position: ReadingPosition = ReadingPosition(), val count: Int = 0,
    val busy: String = "Opening…", val error: String = "", val encoding: String = "Auto",
    val revision: Int = 0, val password: Boolean = false)
data class ReaderState(val library: List<DocumentInfo> = emptyList(),
    val tabs: List<ReaderTab> = emptyList(), val left: String? = null,
    val right: String? = null, val active: String? = null, val home: Boolean = true,
    val settings: ReadingSettings = ReadingSettings(), val message: String = "",
    val importing: Boolean = false, val editor: EditSession? = null, val editBusy: String = "")
