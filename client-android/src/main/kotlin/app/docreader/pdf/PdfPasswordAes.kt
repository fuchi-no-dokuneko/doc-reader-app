package app.docreader.pdf

object PdfPasswordAes {
    fun key(dict: PdfDict, password: String): ByteArray {
        val pass = java.text.Normalizer.normalize(password,java.text.Normalizer.Form.NFKC)
            .toByteArray(Charsets.UTF_8).take(127).toByteArray()
        val user = (dict["U"] as PdfString).bytes
        val owner = (dict["O"] as PdfString).bytes
        fun hash(salt: ByteArray, extra: ByteArray): ByteArray = if (dict.number("R") == 5)
            PdfCipher.hash("SHA-256",pass+salt+extra) else strengthened(pass,salt,extra)
        val extra: ByteArray; val salt: ByteArray; val encrypted: ByteArray
        if (hash(user.copyOfRange(32,40),byteArrayOf()).contentEquals(user.copyOf(32))) {
            extra = byteArrayOf(); salt = user.copyOfRange(40,48); encrypted = (dict["UE"] as PdfString).bytes
        } else if (hash(owner.copyOfRange(32,40),user.copyOf(48)).contentEquals(owner.copyOf(32))) {
            extra = user.copyOf(48); salt = owner.copyOfRange(40,48); encrypted = (dict["OE"] as PdfString).bytes
        } else throw SecurityException("Incorrect PDF password")
        return PdfCipher.aes(hash(salt,extra),ByteArray(16),false).doFinal(encrypted)
    }
    private fun strengthened(password: ByteArray, salt: ByteArray, user: ByteArray): ByteArray {
        var key = PdfCipher.hash("SHA-256",password+salt+user)
        var iteration = 0; var last = 0
        while (iteration < 64 || last > iteration-32) {
            val part = password+key+user
            val repeated = ByteArray(part.size*64) { part[it%part.size] }
            val encrypted = PdfCipher.aes(key.copyOf(16),key.copyOfRange(16,32),true).doFinal(repeated)
            val selector = encrypted.take(16).sumOf { it.toInt() and 255 } % 3
            key = PdfCipher.hash(listOf("SHA-256","SHA-384","SHA-512")[selector],encrypted)
            last = encrypted.last().toInt() and 255; iteration++
        }
        return key.copyOf(32)
    }
}
