package app.docreader.pdf

import java.io.*
import javax.crypto.CipherInputStream

class PdfCrypt(private val dict: PdfDict, private val key: ByteArray) {
    private fun method(stream: Boolean): String {
        if (dict.number("V") < 4) return "V2"
        val filter = dict.name(if (stream) "StmF" else "StrF").ifEmpty { "Identity" }
        if (filter == "Identity") return "Identity"
        return ((dict["CF"] as? PdfDict)?.get(filter) as? PdfDict)?.name("CFM") ?: "V2"
    }
    private fun cipher(input: InputStream, id: Int, gen: Int, method: String): InputStream {
        if (method == "Identity" || method == "None") return input
        val aes = method.startsWith("AES")
        val objectKey = if (method == "AESV3") key else {
            val suffix = byteArrayOf(id.toByte(),(id shr 8).toByte(),(id shr 16).toByte(),gen.toByte(),(gen shr 8).toByte())
            PdfCipher.hash("MD5",key+suffix+if (aes) byteArrayOf(0x73,0x41,0x6c,0x54) else byteArrayOf())
                .copyOf(minOf(key.size+5,16))
        }
        if (!aes) return Rc4Input(input,objectKey)
        val iv = ByteArray(16); DataInputStream(input).readFully(iv)
        return CipherInputStream(input,PdfCipher.aes(objectKey,iv,false,true))
    }
    fun stream(input: InputStream, stream: PdfStream): InputStream {
        if (stream.dict.name("Type") == "XRef") return input
        if (stream.dict.name("Type") == "Metadata" && (dict["EncryptMetadata"] as? PdfWord)?.word == "false") return input
        return cipher(input,stream.id,stream.generation,method(true))
    }
    fun decode(value: PdfValue, id: Int, generation: Int): PdfValue = when (value) {
        is PdfString -> PdfString(cipher(value.bytes.inputStream(),id,generation,method(false)).use { it.readBytes() })
        is PdfArray -> PdfArray(value.values.map { decode(it,id,generation) })
        is PdfDict -> PdfDict(value.values.mapValues { decode(it.value,id,generation) })
        else -> value
    }
    companion object {
        fun open(file: PdfFile, password: String) {
            val encrypt = file.trailer["Encrypt"] ?: return
            val dict = file.dict(encrypt)
            require(dict.name("Filter") == "Standard") { "This PDF requires a certificate" }
            val id = (file.trailer["ID"].array().firstOrNull() as? PdfString)?.bytes ?: byteArrayOf()
            file.crypt = PdfCrypt(dict,PdfPassword.key(dict,id,password)); file.clear()
        }
    }
}
