package app.docreader.render

import android.graphics.*
import android.graphics.pdf.*
import android.os.*
import app.docreader.pdf.*
import java.io.File

class PdfBook(file: File, password: String?) : AutoCloseable {
    private var portable: PortablePdf? = null
    private var unlocked: File? = null
    private val renderer: PdfRenderer
    val cache = BitmapCache()
    val count get() = renderer.pageCount
    init {
        try {
            if (Build.VERSION.SDK_INT < 35) {
                portable = PortablePdf(file,password.orEmpty())
                if (portable!!.source.crypt != null) unlocked = PdfWrite.unlocked(portable!!.source)
            }
            val fd = ParcelFileDescriptor.open(unlocked ?: file,ParcelFileDescriptor.MODE_READ_ONLY)
            try {
                renderer = if (Build.VERSION.SDK_INT >= 35)
                    PdfRenderer(fd,LoadParams.Builder().setPassword(password).build()) else PdfRenderer(fd)
            } catch (e: Exception) { fd.close(); throw e }
            if (portable == null) portable = runCatching { PortablePdf(file,password.orEmpty()) }.getOrNull()
        } catch (e: Exception) { portable?.close(); unlocked?.delete(); throw e }
    }
    fun data(number: Int): PdfPageData = if (Build.VERSION.SDK_INT >= 35)
        renderer.openPage(number).use(NativePdfText::data)
        else portable!!.page(number)
    fun bitmap(number: Int, width: Int, zoom: Float = 1f): Bitmap {
        val target = (width*zoom).toInt().coerceIn(1,4096); val key = "$number:$target"
        cache.get(key)?.let { return it }
        val bitmap = renderer.openPage(number).use { page ->
            var w = target; var h = (w.toLong()*page.height/page.width).toInt().coerceAtLeast(1)
            if (w.toLong()*h > 4_000_000) {
                val scale = kotlin.math.sqrt(4_000_000.0/(w.toLong()*h)); w = (w*scale).toInt(); h = (h*scale).toInt()
            }
            Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also {
                it.eraseColor(Color.WHITE); page.render(it,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
        }
        cache.put(key,bitmap); cache.retain((number-2..number+2).map { "$it:$target" }.toSet())
        return bitmap
    }
    fun chapters() = portable?.structure?.chapters().orEmpty()
    override fun close() { cache.clear(); renderer.close(); portable?.close(); unlocked?.delete() }
}
