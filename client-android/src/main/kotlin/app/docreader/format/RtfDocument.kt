package app.docreader.format

import app.docreader.domain.*
import java.io.PushbackReader
import java.io.Reader
import java.nio.charset.Charset

object RtfDocument {
    fun parse(reader: Reader, sink: BlockSink) {
        val input = PushbackReader(reader, 1); val text = StringBuilder()
        val hexBytes=RtfHex(text)
        val skips = java.util.ArrayDeque<Boolean>(); var skip = false; var uc = 1
        var fallback = 0; var encoding = "windows-1252"
        fun flush() { if (text.isNotEmpty()) { sink.emit(Block(BlockType.TEXT, text.toString())); text.setLength(0) } }
        while (true) {
            val value = input.read(); if (value < 0) break
            if (value!='\\'.code) hexBytes.flush(encoding)
            when (val char = value.toChar()) {
                '{' -> skips.push(skip)
                '}' -> skip = if (skips.isEmpty()) false else skips.pop()
                '\\' -> {
                    val next = input.read().toChar()
                    if (next == '*') { skip = true; continue }
                    if (next == '\'') {
                        val hex = "${input.read().toChar()}${input.read().toChar()}"
                        if (!skip && fallback == 0) hexBytes.add(hex)
                        if (fallback > 0) fallback--
                        continue
                    }
                    hexBytes.flush(encoding)
                    if (!next.isLetter()) { if (!skip && next in "{}\\") text.append(next); continue }
                    val word = StringBuilder().append(next); var c = input.read()
                    while (c >= 0 && c.toChar().isLetter()) { word.append(c.toChar()); c = input.read() }
                    val number = StringBuilder()
                    while (c >= 0 && (c.toChar().isDigit() || c.toChar() == '-')) { number.append(c.toChar()); c = input.read() }
                    if (c >= 0 && c != ' '.code) input.unread(c)
                    val n = number.toString().toIntOrNull() ?: 0
                    when (word.toString()) {
                        "fonttbl", "colortbl", "stylesheet", "pict", "object", "info" -> skip = true
                        "ansicpg" -> encoding = if (n == 936) "GBK" else if (n == 950) "Big5" else "windows-$n"
                        "uc" -> uc = n
                        "u" -> { if (!skip) text.append(n.toChar()); fallback = uc }
                        "par", "line" -> if (!skip) flush()
                        "tab" -> if (!skip) text.append('\t')
                        "bin" -> repeat(n.coerceAtLeast(0)) { input.read() }
                    }
                }
                else -> if (!skip && char !in "\r\n") {
                    if (fallback > 0) fallback-- else text.append(char)
                }
            }
            if (text.length >= 8192) flush()
        }
        hexBytes.flush(encoding); flush()
    }
}
