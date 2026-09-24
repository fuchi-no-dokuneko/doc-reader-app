package app.docreader.pdf

import java.io.Reader

class PdfCMap(reader: Reader) {
    private val codes = hashMapOf<String,String>()
    private val lengths = sortedSetOf<Int>(compareByDescending { it })
    init {
        var mode = ""
        reader.buffered().useLines { lines -> lines.forEach { line ->
            when {
                "beginbfchar" in line -> mode = "char"
                "beginbfrange" in line -> mode = "range"
                "endbf" in line -> mode = ""
                mode.isNotEmpty() -> {
                    val hex = Regex("<([0-9A-Fa-f]+)>").findAll(line).map { it.groupValues[1].uppercase() }.toList()
                    fun add(key: String, value: String) {
                        codes[key] = bytes(value).toString(Charsets.UTF_16BE); lengths += key.length/2
                    }
                    if (mode == "char") for (i in 0 until hex.size-1 step 2) add(hex[i],hex[i+1])
                    else if (hex.size >= 3) {
                        val start = hex[0].toLong(16); val end = hex[1].toLong(16)
                        require(end-start in 0..65535)
                        for (i in start..end) {
                            val key = i.toString(16).padStart(hex[0].length,'0').uppercase()
                            val value = if ('[' in line) hex.getOrNull((i-start+2).toInt()) ?: continue
                                else (hex[2].toLong(16)+i-start).toString(16).padStart(hex[2].length,'0')
                            add(key,value)
                        }
                    }
                }
            }
        } }
    }
    fun decode(bytes: ByteArray): List<Pair<Int,String>> {
        val result = mutableListOf<Pair<Int,String>>(); var p = 0
        while (p < bytes.size) {
            var found = false
            for (size in lengths) {
                if (p+size > bytes.size) continue
                val key = bytes.copyOfRange(p,p+size).joinToString("") { "%02X".format(it) }
                val text = codes[key] ?: continue
                result += key.toLong(16).toInt() to text; p += size; found = true; break
            }
            if (!found) { result += (bytes[p].toInt() and 255) to "�"; p++ }
        }
        return result
    }
    companion object { fun bytes(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray() }
}
