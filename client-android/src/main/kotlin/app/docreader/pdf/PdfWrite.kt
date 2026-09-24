package app.docreader.pdf

import java.io.File
import java.io.RandomAccessFile

object PdfWrite {
    fun unlocked(file: PdfFile): File {
        val output = File.createTempFile("unlocked-",".pdf",file.file.parentFile)
        try {
            RandomAccessFile(output,"rw").use { out ->
                fun text(value: String) = out.write(value.toByteArray(Charsets.ISO_8859_1))
                text("%PDF-1.7\n%âãÏÓ\n")
                val positions = sortedMapOf<Int,Pair<Long,Int>>()
                val encrypt = (file.trailer["Encrypt"] as? PdfRef)?.id
                for ((id,ref) in file.refs.toSortedMap()) {
                    if (id == encrypt) continue
                    if (Thread.currentThread().isInterrupted) throw InterruptedException()
                    val value = file.get(id) ?: continue
                    if (value is PdfStream && value.dict.name("Type") in setOf("XRef","ObjStm")) continue
                    val gen = ref.second.coerceAtLeast(0)
                    positions[id] = out.filePointer to gen; text("$id $gen obj\n")
                    if (value is PdfStream) {
                        val temp = File.createTempFile("stream-",".tmp",output.parentFile)
                        try {
                            file.raw(value).use { input -> temp.outputStream().use { input.copyTo(it,16384) } }
                            text(PdfSerialize.value(PdfDict(value.dict.values+("Length" to PdfNumber(temp.length().toDouble())))))
                            text("\nstream\n"); temp.inputStream().use { input ->
                                val buffer = ByteArray(16384)
                                while (true) { val n = input.read(buffer); if (n < 0) break; out.write(buffer,0,n) }
                            }; text("\nendstream")
                        } finally { temp.delete() }
                    } else text(PdfSerialize.value(value))
                    text("\nendobj\n")
                }
                val start = out.filePointer; val size = (positions.lastKeyOrNull() ?: 0)+1
                text("xref\n0 1\n0000000000 65535 f \n")
                positions.forEach { (id,pair) -> text("$id 1\n%010d %05d n \n".format(java.util.Locale.ROOT,pair.first,pair.second)) }
                val trailer = file.trailer.values.filterKeys { it in setOf("Root","Info","ID") }+("Size" to PdfNumber(size.toDouble()))
                text("trailer\n${PdfSerialize.value(PdfDict(trailer))}\nstartxref\n$start\n%%EOF\n")
            }
            return output
        } catch (e: Exception) { output.delete(); throw e }
    }
    private fun <K,V> java.util.SortedMap<K,V>.lastKeyOrNull(): K? = if (isEmpty()) null else lastKey()
}
