package app.docreader.pdf

object PdfStreamLength {
    fun find(file: PdfFile, start: Long): Long {
        val marker="endstream".toByteArray(Charsets.US_ASCII)
        var position=start; var match=0
        while (position<file.size) {
            val data=file.read(position,minOf(8192,file.size-position).toInt())
            for ((index,byte) in data.withIndex()) {
                if (byte==marker[match]) match++ else match=if (byte==marker[0]) 1 else 0
                if (match==marker.size) {
                    var end=position+index+1-marker.size
                    if (end>start && file.read(end-1,1)[0].toInt()==10) end--
                    if (end>start && file.read(end-1,1)[0].toInt()==13) end--
                    return end-start
                }
            }
            position+=data.size
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
        }
        error("Incomplete PDF stream")
    }
}
