package app.docreader

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object CfbFixture {
    fun write(file: File, streams: Map<String,ByteArray>): File {
        val data=ByteArray((3+streams.size*8)*512)
        val b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        b.put(byteArrayOf(-48,-49,17,-32,-95,-79,26,-31))
        b.putShort(24,0x3e); b.putShort(26,3); b.putShort(28,(-2).toShort()); b.putShort(30,9); b.putShort(32,6)
        b.putInt(44,1); b.putInt(48,1); b.putInt(56,4096); b.putInt(60,-2); b.putInt(68,-2)
        for (i in 0..108) b.putInt(76+i*4,if (i==0) 0 else -1)
        for (i in 0..127) b.putInt(512+i*4,-1)
        b.putInt(512,-3); b.putInt(516,-2)
        fun entry(index: Int,name: String,type: Int,start: Int,size: Int) {
            val offset=1024+index*128; val text=(name+'\u0000').toByteArray(Charsets.UTF_16LE)
            text.copyInto(data,offset); b.putShort(offset+64,text.size.toShort()); b.put(offset+66,type.toByte())
            b.put(offset+67,1); b.putInt(offset+68,-1); b.putInt(offset+72,-1); b.putInt(offset+76,-1)
            b.putInt(offset+116,start); b.putLong(offset+120,size.toLong())
        }
        entry(0,"Root Entry",5,-2,0)
        streams.entries.forEachIndexed { i,(name,bytes) ->
            require(bytes.size<=4096)
            val start=2+i*8
            entry(i+1,name,2,start,4096)
            for (sector in start until start+8) b.putInt(512+sector*4,if (sector==start+7) -2 else sector+1)
            bytes.copyInto(data,(start+1)*512)
        }
        file.writeBytes(data); return file
    }
    fun record(type: Int, data: ByteArray, ppt: Boolean=false, container: Boolean=false): ByteArray {
        val head=ByteBuffer.allocate(if (ppt) 8 else 4).order(ByteOrder.LITTLE_ENDIAN)
        if (ppt) { head.putShort(if (container) 15 else 0); head.putShort(type.toShort()); head.putInt(data.size) }
        else { head.putShort(type.toShort()); head.putShort(data.size.toShort()) }
        return head.array()+data
    }
}
