package com.kaushalpanjee.uidai.crypto


import com.kaushalpanjee.security.SecurityUtils
import kotlin.Throws
import java.lang.Exception
import javax.inject.Inject


class EncryptionImpl @Inject constructor(private val cryptLib: CryptLib) : CryptoStrategy {

    @Throws(Exception::class)
    override fun encrypt(body: String): String {
        return cryptLib.encrypt(body, SecurityUtils.getCryptId(), SecurityUtils.getCryptIv())
    }

    override fun decrypt(data: String): String? {
        return null
    }
}