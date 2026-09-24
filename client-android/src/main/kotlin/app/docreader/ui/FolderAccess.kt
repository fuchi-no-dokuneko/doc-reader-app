package app.docreader.ui

import android.content.Intent
import android.net.Uri
import app.docreader.MainActivity
import kotlinx.coroutines.launch

object FolderAccess {
    fun accept(activity: MainActivity, uri: Uri) {
        activity.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.model.scope.launch {
            activity.model.preferences.folder(uri.toString())
            activity.model.assets.folder=uri
            activity.model.state.value.tabs.forEach { activity.model.loading.restart(it.id) }
            activity.model.message("Linked images can now be read from this folder")
        }
    }
}
