package app.docreader

import app.docreader.format.Encodings
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import java.nio.charset.Charset

class EncodingTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun chineseAndUnicodeRoundTrips() {
        val examples=mapOf("UTF-8" to "Hello 世界 🌏", "GBK" to "这是一个中文文件，我们正在阅读文档。",
            "GB2312" to "中国人民学习计算机语言。", "GB18030" to "中国𠀀文档测试", "Big5" to "這是一個繁體中文文件，我們正在閱讀文件。",
            "UTF-16LE" to "中文編碼", "UTF-16BE" to "中文編碼")
        for ((charset,text) in examples) {
            val file=temporary.newFile(charset)
            val bom=when(charset) { "UTF-16LE" -> byteArrayOf(-1,-2); "UTF-16BE" -> byteArrayOf(-2,-1); else -> byteArrayOf() }
            file.writeBytes(bom+text.toByteArray(Charset.forName(charset)))
            val (reader,detected)=Encodings.open(file,"Auto")
            assertEquals("$charset detected as $detected",text,reader.use { it.readText().removePrefix("\uFEFF") })
            val manual=Encodings.open(file,charset).first.use { it.readText().removePrefix("\uFEFF") }
            assertEquals(text,manual)
        }
    }
    @Test fun nonAsciiBeyondHeaderAndPartialUtf8Boundary() {
        val file=temporary.newFile()
        val text="#".repeat(65536)+"中文資料🌏".repeat(20000)
        file.writeText(text)
        assertEquals("UTF-8",Encodings.detect(file))
        assertEquals(text,Encodings.open(file,"Auto").first.use { it.readText() })
    }
}
