package app.docreader.pdf

import app.docreader.format.BinarySource
import java.io.*

class PdfFile(val file: File) : BinarySource, AutoCloseable {
    private val input = RandomAccessFile(file,"r")
    override val size = input.length()
    val refs = mutableMapOf<Int,Pair<Long,Int>>()
    var trailer = PdfDict(emptyMap())
    var crypt: PdfCrypt? = null
    private val cache = object : LinkedHashMap<Int,PdfValue>(32,.75f,true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int,PdfValue>?) = size > 32
    }
    init {
        val tail = read((size-65536).coerceAtLeast(0),minOf(size,65536).toInt()).toString(Charsets.ISO_8859_1)
        val start = Regex("startxref\\s+(\\d+)").findAll(tail).lastOrNull()?.groupValues?.get(1)?.toLong()
            ?: error("PDF cross-reference index is missing")
        PdfXref.load(this,start,hashSetOf())
    }
    @Synchronized override fun read(position: Long, length: Int): ByteArray {
        require(position >= 0 && length >= 0 && position+length <= size)
        return ByteArray(length).also { input.seek(position); input.readFully(it) }
    }
    fun lexer(offset: Long) = PdfLexer(PdfCursor(this,offset))
    fun resolve(value: PdfValue?): PdfValue? = if (value is PdfRef) get(value.id) else value
    fun dict(value: PdfValue?) = resolve(value) as? PdfDict ?: (resolve(value) as? PdfStream)?.dict ?: PdfDict(emptyMap())
    fun get(id: Int): PdfValue? {
        cache[id]?.let { return it }
        val (offset,kind) = refs[id] ?: return null
        val value = if (kind >= 0) objectAt(offset) else PdfObjects.compressed(this,offset.toInt(),-kind-1)
        cache[id] = value; return value
    }
    fun objectAt(offset: Long): PdfValue {
        val lexer = lexer(offset); val id = lexer.cursor.word().toInt(); val generation = lexer.cursor.word().toInt()
        require(lexer.cursor.word() == "obj")
        val value = lexer.value()
        if (value is PdfDict && lexer.cursor.word() == "stream") {
            var c = lexer.cursor.read()
            if (c == 13) c = lexer.cursor.read()
            if (c != 10) lexer.cursor.unread()
            val length = value["Length"]?.let { resolve(it).number().toLong() }
                ?: PdfStreamLength.find(this,lexer.cursor.position)
            return PdfStream((crypt?.decode(value,id,generation) as? PdfDict) ?: value,lexer.cursor.position,length,id,generation)
        }
        return crypt?.decode(value,id,generation) ?: value
    }
    fun raw(stream: PdfStream): InputStream = crypt?.stream(this.stream(stream.offset,stream.length),stream)
        ?: this.stream(stream.offset,stream.length)
    fun decoded(stream: PdfStream): InputStream = PdfFilters.decode(raw(stream),stream.dict)
    fun clear() = cache.clear()
    override fun close() = input.close()
}
