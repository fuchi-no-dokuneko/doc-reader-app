package app.docreader.ui

import app.docreader.render.Palette
import androidx.core.graphics.ColorUtils

object EditorPalette {
    fun from(reading: Palette): Palette = if (ColorUtils.calculateLuminance(reading.paper)<.5)
        Palette(0xff102e2b.toInt(),0xffe7f6ee.toInt(),0xff204f47.toInt(),0xff8cdeca.toInt(),0xffacc7be.toInt())
    else Palette(0xffe9f8f2.toInt(),0xff17372e.toInt(),0xffbce9d9.toInt(),0xff096a50.toInt(),0xff426458.toInt())
}
