package com.kaushalpanjee.core.util.optimize

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {

    fun sha512(input: String): String =
        MessageDigest.getInstance("SHA-512")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun otp4(): Int =
        SecureRandom().nextInt(9000) + 1000

    fun encryptAES(input: String, key: String): String {
        val spec = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, spec)
        return Base64.encodeToString(cipher.doFinal(input.toByteArray()), Base64.NO_WRAP)
    }

    fun decryptAES(data: String, key: String): String {
        val spec = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, spec)
        return String(cipher.doFinal(Base64.decode(data, Base64.NO_WRAP)))
    }
}
