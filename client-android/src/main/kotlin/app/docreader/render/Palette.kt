package app.docreader.render

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import app.docreader.domain.Theme

data class Palette(val paper: Int, val ink: Int, val surface: Int, val accent: Int, val muted: Int) {
    companion object {
        fun forTheme(context: Context, theme: Theme): Palette {
            val dark = theme == Theme.DARK || theme == Theme.SYSTEM &&
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            return when {
                dark -> Palette(0xff151b24.toInt(),0xffe4e9f0.toInt(),0xff243043.toInt(),0xff8ab4ff.toInt(),0xffadb9cc.toInt())
                theme == Theme.SEPIA -> Palette(0xfff3e7ce.toInt(),0xff433627.toInt(),0xffe5d5b7.toInt(),0xff77552e.toInt(),0xff73634c.toInt())
                else -> Palette(Color.WHITE,0xff202c3d.toInt(),0xffedf2fb.toInt(),0xff305fba.toInt(),0xff607086.toInt())
            }
        }
    }
}
