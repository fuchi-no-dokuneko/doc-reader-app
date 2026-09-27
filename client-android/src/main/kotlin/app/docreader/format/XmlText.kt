package app.docreader.format

import java.io.*
import java.nio.charset.Charset

object XmlText {
    fun reader(input: InputStream): BufferedReader {
        val stream=PushbackInputStream(input,512)
        val head=ByteArray(512); var size=0
        while (size<head.size) {
            val n=stream.read(head,size,head.size-size); if (n<0) break
            size+=n
        }
        val bom=if (size>=3 && head[0]==0xef.toByte() && head[1]==0xbb.toByte() && head[2]==0xbf.toByte()) 3 else 0
        stream.unread(head,bom,size-bom)
        val encoding=when {
            size>=2 && head[0]==0xff.toByte() && head[1]==0xfe.toByte() -> "UTF-16"
            size>=2 && head[0]==0xfe.toByte() && head[1]==0xff.toByte() -> "UTF-16"
            size>=4 && head[0]==0.toByte() && head[1]==60.toByte() -> "UTF-16BE"
            size>=4 && head[0]==60.toByte() && head[1]==0.toByte() -> "UTF-16LE"
            else -> Regex("encoding\\s*=\\s*['\"]([^'\"]+)['\"]",RegexOption.IGNORE_CASE)
                .find(String(head,0,size,Charsets.ISO_8859_1))?.groupValues?.get(1) ?: "UTF-8"
        }
        return InputStreamReader(stream,Charset.forName(encoding)).buffered()
    }
}
