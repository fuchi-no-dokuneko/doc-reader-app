package app.docreader.pdf

import javax.crypto.Cipher
import javax.crypto.spec.*
import java.security.MessageDigest

object PdfCipher {
    val padding = byteArrayOf(0x28,-65,0x4e,0x5e,0x4e,0x75,-118,0x41,0x64,0,0x4e,0x56,-1,-6,1,8,0x2e,0x2e,0,-74,-48,0x68,0x3e,-128,0x2f,0x0c,-87,-2,0x64,0x53,0x69,0x7a)
    fun hash(name: String, data: ByteArray) = MessageDigest.getInstance(name).digest(data)
    fun pad(password: ByteArray) = (password+padding).copyOf(32)
    fun rc4(key: ByteArray, bytes: ByteArray): ByteArray {
        val state = IntArray(256) { it }; var j = 0
        for (i in 0..255) { j = (j+state[i]+(key[i%key.size].toInt() and 255)) and 255
            val t = state[i]; state[i] = state[j]; state[j] = t }
        var i = 0; j = 0
        return ByteArray(bytes.size) { n ->
            i = (i+1) and 255; j = (j+state[i]) and 255
            val t = state[i]; state[i] = state[j]; state[j] = t
            (bytes[n].toInt() xor state[(state[i]+state[j]) and 255]).toByte()
        }
    }
    fun aes(key: ByteArray, iv: ByteArray, encrypt: Boolean, padding: Boolean = false): Cipher =
        Cipher.getInstance("AES/CBC/"+if (padding) "PKCS5Padding" else "NoPadding").apply {
            init(if (encrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,SecretKeySpec(key,"AES"),IvParameterSpec(iv))
        }
}
