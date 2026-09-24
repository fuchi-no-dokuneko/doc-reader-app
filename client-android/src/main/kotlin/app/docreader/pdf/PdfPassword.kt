package app.docreader.pdf

object PdfPassword {
    fun key(dict: PdfDict, id: ByteArray, password: String): ByteArray {
        val revision = dict.number("R")
        if (revision >= 5) return PdfPasswordAes.key(dict,password)
        val bytes = password.toByteArray(Charsets.ISO_8859_1)
        val length = if (revision == 2) 5 else dict.number("Length",40)/8
        val owner = (dict["O"] as PdfString).bytes; val user = (dict["U"] as PdfString).bytes
        fun derive(input: ByteArray): ByteArray {
            val permissions = dict.number("P")
            val p = ByteArray(4) { (permissions shr (it*8)).toByte() }
            val extra = if (revision >= 4 && (dict["EncryptMetadata"] as? PdfWord)?.word == "false") ByteArray(4) { -1 } else byteArrayOf()
            var key = PdfCipher.hash("MD5",PdfCipher.pad(input)+owner+p+id+extra)
            if (revision >= 3) repeat(50) { key = PdfCipher.hash("MD5",key.copyOf(length)) }
            return key.copyOf(length)
        }
        fun valid(key: ByteArray): Boolean {
            var value = if (revision == 2) PdfCipher.rc4(key,PdfCipher.padding)
                else PdfCipher.rc4(key,PdfCipher.hash("MD5",PdfCipher.padding+id))
            if (revision >= 3) for (i in 1..19) value = PdfCipher.rc4(key.map { (it.toInt() xor i).toByte() }.toByteArray(),value)
            val n = if (revision == 2) 32 else 16
            return value.copyOf(n).contentEquals(user.copyOf(n))
        }
        val direct = derive(bytes); if (valid(direct)) return direct
        var ownerKey = PdfCipher.hash("MD5",PdfCipher.pad(bytes))
        if (revision >= 3) repeat(50) { ownerKey = PdfCipher.hash("MD5",ownerKey) }
        ownerKey = ownerKey.copyOf(length)
        var decoded = owner
        if (revision == 2) decoded = PdfCipher.rc4(ownerKey,decoded)
        else for (i in 19 downTo 0) decoded = PdfCipher.rc4(ownerKey.map { (it.toInt() xor i).toByte() }.toByteArray(),decoded)
        val result = derive(decoded)
        if (!valid(result)) throw SecurityException("Incorrect PDF password")
        return result
    }
}
