package com.kaushalpanjee.core.util

import android.util.Base64
import com.kaushalpanjee.security.SecurityUtils
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCryptography {

    private var enableEncryption = true

    @Throws(
        UnsupportedEncodingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun encryptIntoBase64String(inputText: String, secretKey: String, ivKey: String): String {
        if (enableEncryption) {
            val keyBytes = formatKey(secretKey)
            val ivBytes = formatIV(ivKey)

            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(SecurityUtils.getCryptLibAes())
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

            val encryptedBytes = cipher.doFinal(inputText.toByteArray(StandardCharsets.UTF_8))

            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
        }
        return inputText
    }

    fun decryptIntoString(inputText: String, secretKey: String, ivKey: String): String {
        return try {
            val keyBytes = formatKey(secretKey)
            val ivBytes = formatIV(ivKey)

            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(SecurityUtils.getCryptLibAes())
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val decodedBytes = Base64.decode(inputText, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)

            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            log("Decryption failed: ", e.message.toString())
            ""
        }
    }

    fun aesDecrypt(encryptedText: String, secretKey: String, ivKey: String): String {
        return try {
            val keyBytes = formatKey(secretKey)
            val ivBytes = formatIV(ivKey)

            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(SecurityUtils.getCryptLibAes())
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val encryptedBytes = hexStringToByteArray(encryptedText)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    private fun convertByteArrayToHexString(byteArray: ByteArray): String {
        val sb = StringBuffer()
        for (b in byteArray) {
            val hexString = String.format("%02X", b)
            sb.append(hexString)
        }
        return sb.toString()
    }

    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    // ✅ Proper Key Formatter for AES-256
    private fun formatKey(key: String): ByteArray {
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        return when {
            keyBytes.size == 16 -> keyBytes // already 256 bits
            keyBytes.size < 16 -> keyBytes.copyOf(16) // pad with zeros
            else -> keyBytes.copyOf(16) // truncate
        }
    }

    // ✅ IV must be exactly 16 bytes
    private fun formatIV(iv: String): ByteArray {
        val ivBytes = iv.toByteArray(StandardCharsets.UTF_8)
        return when {
            ivBytes.size == 16 -> ivBytes
            ivBytes.size < 16 -> ivBytes.copyOf(16)
            else -> ivBytes.copyOf(16)
        }
    }

    private fun log(tag: String, message: String) {
        println("$tag: $message")
        // Or use Log.e(tag, message) in Android
    }
}
