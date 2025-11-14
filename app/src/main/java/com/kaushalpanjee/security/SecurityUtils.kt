package com.kaushalpanjee.security

import android.util.Log

/**
 * Created by Rishi Porwal
 */

object SecurityUtils {

    // Key names constants
    object Keys {
        const val ENCRYPT_IV_KEY = "encrypt_iv_key"
        const val ENCRYPT_KEY = "encrypt_key"
        const val CRYPT_LIB_AES = "crypt_lib_aes"
        const val CRYPT_ID = "crypt_id"
        const val CRYPT_IV = "crypt_iv"
    }

    fun getEncryptIvKey(): String = SecureConfig.encryptIvKey
    fun getEncryptKey(): String = SecureConfig.encryptKey
    @JvmStatic
    fun getCryptLibAes(): String = SecureConfig.cryptLibAes
    @JvmStatic
    fun getCryptId(): String = SecureConfig.cryptId
    @JvmStatic
    fun getCryptIv(): String = SecureConfig.cryptIv

    fun getRefreshTokenUrl(): String = SecureConfig.REFRESH_TOKEN_URL

    fun getClientSecretKey(): String = SecureConfig.CLIENT_SECRET_KEY

    fun getWadhKey(): String = SecureConfig.WADH_KEY

    fun getSecureValue(keyName: String): String = SecureConfig.getKey(keyName)

    fun keyLogsTest() {
//        val TAG="Secure KEY Testing"
//
//        try {
//            Log.d(TAG, "=== SecureConfig Test - BEGIN ===")
//            Log.d(TAG, "ENCRYPT_IV_KEY (property): ${getEncryptIvKey()}")
//            Log.d(TAG, "ENCRYPT_KEY     (property): ${getEncryptKey()}")
//            Log.d(TAG, "CRYPT_LIB_AES   (property): ${getCryptLibAes()}")
//            Log.d(TAG, "CRYPT_ID        (property): ${getCryptId()}")
//            Log.d(TAG, "CRYPT_IV        (property): ${getCryptIv()}")
//
//            // Example using getSecureValue(keyName)
//            Log.d(TAG, "getSecureValue(ENCRYPT_IV_KEY): ${getSecureValue(Keys.ENCRYPT_IV_KEY)}")
//
//            Log.d(TAG, "=== SecureConfig Test - END ===")
//
//            println("SecureConfigTest logged to Logcat (tag=$TAG)")
//        } catch (t: Throwable) {
//            Log.e(TAG, "Error while logging secure values", t)
//        }
    }


}