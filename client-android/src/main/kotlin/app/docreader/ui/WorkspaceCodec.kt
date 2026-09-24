package app.docreader.ui

import org.json.*

object WorkspaceCodec {
    fun encode(s: ReaderState): String = JSONObject().put("left",s.left).put("right",s.right).put("active",s.active)
        .put("home",s.home).put("tabs",JSONArray(s.tabs.map {
            JSONObject().put("id",it.id).put("doc",it.document.id)
        })).toString()
}
