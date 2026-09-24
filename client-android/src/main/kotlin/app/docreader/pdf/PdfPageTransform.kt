package app.docreader.pdf

class PdfPageTransform(box: List<PdfValue>, rotation: Int) {
    private val x = box.getOrNull(0).number().toFloat()
    private val y = box.getOrNull(1).number().toFloat()
    val width = (box.getOrNull(2).number().toFloat()-x).coerceAtLeast(1f)
    val height = (box.getOrNull(3).number().toFloat()-y).coerceAtLeast(1f)
    private val angle = ((rotation%360)+360)%360
    val displayWidth = if (angle%180 == 0) width else height
    val displayHeight = if (angle%180 == 0) height else width
    fun point(px: Float, py: Float): Pair<Float,Float> {
        val a = px-x; val b = py-y
        return when (angle) { 90 -> b to a; 180 -> width-a to b; 270 -> height-b to width-a; else -> a to height-b }
    }
    fun box(l: Float, b: Float, r: Float, t: Float): PdfBox {
        val points = listOf(point(l,b),point(r,t),point(l,t),point(r,b))
        return PdfBox(points.minOf { it.first },points.minOf { it.second },points.maxOf { it.first },points.maxOf { it.second })
    }
}
data class PdfMatrix(val a: Float = 1f, val b: Float = 0f, val c: Float = 0f,
    val d: Float = 1f, val e: Float = 0f, val f: Float = 0f) {
    fun point(x: Float,y: Float) = (a*x+c*y+e) to (b*x+d*y+f)
    fun then(m: PdfMatrix) = PdfMatrix(m.a*a+m.c*b,m.b*a+m.d*b,m.a*c+m.c*d,m.b*c+m.d*d,
        m.a*e+m.c*f+m.e,m.b*e+m.d*f+m.f)
    companion object {
        fun of(values: List<PdfValue>): PdfMatrix {
            val v = values.map { it.number().toFloat() }
            return if (v.size == 6) PdfMatrix(v[0],v[1],v[2],v[3],v[4],v[5]) else PdfMatrix()
        }
    }
}
