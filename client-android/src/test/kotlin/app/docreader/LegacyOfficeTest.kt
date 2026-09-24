package app.docreader

import app.docreader.format.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import java.nio.*

class LegacyOfficeTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun word97UnicodePieceTable() {
        val text="Hello 中文\rSecond paragraph\r"
        val word=ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)
        word.putInt(418,0); word.putInt(422,21)
        text.toByteArray(Charsets.UTF_16LE).copyInto(word.array(),512)
        val table=ByteBuffer.allocate(21).order(ByteOrder.LITTLE_ENDIAN)
        table.put(0,2); table.putInt(1,16); table.putInt(5,0); table.putInt(9,text.length); table.putInt(15,512)
        val file=CfbFixture.write(temporary.newFile(),mapOf("WordDocument" to word.array(),"0Table" to table.array()))
        val out=Blocks(); DocDocument.parse(file,out)
        assertEquals(listOf("Hello 中文","Second paragraph"),out.values.map { it.text })
    }
    @Test fun binaryPowerPointTextAtoms() {
        val title=CfbFixture.record(4000,"Title 中文".toByteArray(Charsets.UTF_16LE),true)
        val body=CfbFixture.record(4008,"Body".toByteArray(Charsets.ISO_8859_1),true)
        val slide=CfbFixture.record(1006,title+body,true,true)
        val file=CfbFixture.write(temporary.newFile(),mapOf("PowerPoint Document" to slide))
        val out=Blocks(); PptDocument.parse(file,out)
        assertTrue(out.text.contains("Slide 1")); assertTrue(out.text.contains("Title 中文")); assertTrue(out.text.contains("Body"))
    }
    @Test fun binaryExcelSharedStringAndNumberCells() {
        fun record(type: Int,bytes: ByteArray)=CfbFixture.record(type,bytes)
        val bof=record(0x809,byteArrayOf(0,6,0x10,0))
        val sst=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putInt(1).putInt(1).putShort(5).put(0).put("hello".toByteArray()).array()
        val label=ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN).putShort(0).putShort(0).putShort(0).putInt(0).array()
        val number=ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN).putShort(0).putShort(1).putShort(0).putDouble(42.5).array()
        val stream=record(0xfc,sst)+bof+record(0xfd,label)+record(0x203,number)+record(0xa,byteArrayOf())
        val file=CfbFixture.write(temporary.newFile(),mapOf("Workbook" to stream))
        val out=Blocks(); XlsDocument.parse(file,temporary.newFolder(),out)
        assertTrue(out.text.contains("hello")); assertTrue(out.text.contains("42.5"))
    }
}
