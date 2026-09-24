package app.docreader.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.*
import app.docreader.domain.*
import app.docreader.format.Markup

object RichWeb {
    @SuppressLint("SetJavaScriptEnabled")
    fun create(pane: ReaderPane, block: Block, owned: MutableList<WebView>): WebView {
        val settings=pane.model.state.value.settings
        fun hex(color: Int)="#%06x".format(color and 0xffffff)
        val source=if (block.type==BlockType.IMAGE) "<img src='https://reader.local/asset?path=${Uri.encode(block.asset)}'>" else block.html
        val html="""<!doctype html><meta name="viewport" content="width=device-width,initial-scale=1">
            <style>body{margin:0;color:${hex(pane.ui.colors.ink)};background:${hex(pane.ui.colors.paper)};
            font-family:${settings.font};font-size:${settings.size}px;line-height:${settings.lineHeight}}
            table{border-collapse:collapse;min-width:100%}td,th{border:1px solid ${hex(pane.ui.colors.muted)};
            padding:6px;vertical-align:top;white-space:pre-wrap}th{background:${hex(pane.ui.colors.surface)}}
            img,svg{max-width:100%;max-height:100vh}a{color:${hex(pane.ui.colors.accent)}}</style>$source""".trimIndent()
        return WebView(pane.context).apply {
            setBackgroundColor(pane.ui.colors.paper); this.settings.javaScriptEnabled=false
            this.settings.allowFileAccess=false; this.settings.allowContentAccess=false
            this.settings.blockNetworkLoads=true; this.settings.builtInZoomControls=true
            this.settings.displayZoomControls=false
            webViewClient=object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url=request.url
                    ReaderLinks.open(pane,if (url.host=="reader.local") url.encodedPath.orEmpty().removePrefix("/")+
                        (url.encodedFragment?.let { "#$it" } ?: "") else url.toString()); return true
                }
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                    val path=request.url.takeIf { it.host=="reader.local" }?.getQueryParameter("path")
                    return try {
                        val input=if (path != null) pane.model.assets.open(path) else "".byteInputStream()
                        val mime=if (path?.endsWith(".svg")==true) "image/svg+xml" else "image/png"
                        WebResourceResponse(mime,null,input)
                    } catch (_: Exception) { WebResourceResponse("text/plain","UTF-8","Image unavailable".byteInputStream()) }
                }
            }
            setOnTouchListener { _,_ -> PageSurface.consume(this); false }
            loadDataWithBaseURL("https://reader.local/",html,"text/html","UTF-8",null); owned+=this
        }
    }
}
