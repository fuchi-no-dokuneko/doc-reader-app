package app.docreader.format

import java.io.Reader

object TextLines {
    fun read(reader: Reader, emit: (String) -> Unit) {
        val line = StringBuilder(); val chars = CharArray(8192)
        var first = true
        while (true) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            val count = reader.read(chars); if (count < 0) break
            for (i in 0 until count) {
                val char = chars[i]
                if (first && char == '\uFEFF') { first = false; continue }
                first = false
                if (char == '\n') { emit(line.toString().removeSuffix("\r")); line.setLength(0) }
                else line.append(char)
                if (line.length >= 8192 && !Character.isHighSurrogate(char)) {
                    emit(line.toString()); line.setLength(0)
                }
            }
        }
        if (line.isNotEmpty()) emit(line.toString())
    }
}
