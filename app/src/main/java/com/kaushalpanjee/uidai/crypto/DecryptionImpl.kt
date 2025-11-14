package com.kaushalpanjee.uidai.crypto


import com.kaushalpanjee.security.SecurityUtils
import java.lang.Exception
import javax.inject.Inject

class DecryptionImpl @Inject constructor(private val cryptLib: CryptLib) : CryptoStrategy {


    override fun encrypt(body: String): String? {
        return null
    }

    @Throws(Exception::class)
    override fun decrypt(data: String): String {
        return cryptLib.decrypt(data, SecurityUtils.getCryptId(), SecurityUtils.getCryptIv())
    }
}