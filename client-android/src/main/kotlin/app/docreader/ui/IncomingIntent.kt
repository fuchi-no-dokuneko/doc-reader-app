package app.docreader.ui

import android.content.Intent
import android.net.Uri

object IncomingIntent {
    @Suppress("DEPRECATION")
    fun accept(model: ReaderModel, intent: Intent) {
        val clip=intent.clipData
        val uri=intent.data ?: intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            ?: if (clip!=null && clip.itemCount>0) clip.getItemAt(0).uri else null
        if (uri!=null) model.importFile(uri,intent.flags)
        else intent.getStringExtra(Intent.EXTRA_TEXT)?.let { SharedImport.open(model,it) }
    }
}
