package app.docreader.pdf

import java.io.ByteArrayOutputStream

object PdfStrings {
    fun literal(cursor: PdfCursor): PdfString {
        val out = ByteArrayOutputStream(); var depth = 1
        while (depth > 0) {
            var c = cursor.read(); require(c >= 0)
            if (c == '\\'.code) {
                c = cursor.read()
                c = when (c.toChar()) { 'n' -> 10; 'r' -> 13; 't' -> 9; 'b' -> 8; 'f' -> 12
                    in '0'..'7' -> {
                        var n = c-'0'.code
                        for (i in 0..1) { val d = cursor.read(); if (d in 48..55) n = n*8+d-48 else { cursor.unread(); break } }; n
                    }
                    '\n' -> continue
                    '\r' -> { if (cursor.read() != 10) cursor.unread(); continue }
                    else -> c }
            } else if (c == '('.code) depth++ else if (c == ')'.code) { depth--; if (depth == 0) break }
            out.write(c); require(out.size() < 4*1024*1024)
        }; return PdfString(out.toByteArray())
    }
    fun hex(cursor: PdfCursor): PdfString {
        val out = ByteArrayOutputStream(); var high = -1
        while (true) {
            val c = cursor.read(); require(c >= 0)
            if (c == '>'.code) break
            if (c <= 32) continue
            val n = c.toChar().digitToInt(16)
            if (high < 0) high = n else { out.write(high*16+n); high = -1 }
        }
        if (high >= 0) out.write(high*16)
        return PdfString(out.toByteArray())
    }
}
