package app.docreader.pdf

import java.io.InputStream
import kotlin.math.abs

class PredictorInput(private val input: InputStream, options: PdfDict) : InputStream() {
    private val predictor = options.number("Predictor",1)
    private val colors = options.number("Colors",1)
    private val bits = options.number("BitsPerComponent",8)
    private val stride = ((colors*bits+7)/8).coerceAtLeast(1)
    private val rowSize = (options.number("Columns",1)*colors*bits+7)/8
    private var prior = ByteArray(rowSize)
    private var row = byteArrayOf(); private var position = 0
    init { require(rowSize in 1..16_777_216) }
    override fun read(): Int {
        if (position == row.size) {
            val type = if (predictor >= 10) input.read() else 1
            if (type < 0) return -1
            row = ByteArray(rowSize)
            var n = 0
            while (n < rowSize) { val read = input.read(row,n,rowSize-n); if (read < 0) return -1; n += read }
            for (i in row.indices) {
                val a = if (i >= stride) row[i-stride].toInt() and 255 else 0
                val b = prior[i].toInt() and 255
                val c = if (i >= stride) prior[i-stride].toInt() and 255 else 0
                val add = when (type) { 0 -> 0; 1 -> a; 2 -> b; 3 -> (a+b)/2; 4 -> {
                    val p = a+b-c; val pa = abs(p-a); val pb = abs(p-b); val pc = abs(p-c)
                    if (pa <= pb && pa <= pc) a else if (pb <= pc) b else c
                }; else -> error("Invalid PDF predictor") }
                row[i] = (row[i]+add).toByte()
            }
            prior = row.copyOf(); position = 0
        }
        return row[position++].toInt() and 255
    }
    override fun close() = input.close()
}
