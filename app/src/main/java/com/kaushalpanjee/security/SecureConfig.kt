package com.kaushalpanjee.security

import android.util.Log

/**
 * Created by Rishi Porwal
 */
object SecureConfig {


    init {
        try{
        System.loadLibrary("secure-keys")
        Log.d("SecureConfig", "✅ Native library loaded")
    } catch (e: Exception) {
        Log.e("SecureConfig", " Failed to load native library", e)
    }
    }

    // external JNI methods
    private external fun getEncryptIvKeyNative(): String
    private external fun getEncryptKeyNative(): String
    private external fun getCryptLibAesNative(): String
    private external fun getCryptIdNative(): String
    private external fun getCryptIvNative():String
    private external fun getKeyByNameNative(keyName: String): String

    private external fun nativeRefreshTokenUrl(): String
    private external fun nativeClientSecretKey(): String
    private external fun nativeWadhKey(): String



    // Kotlin getters

    val encryptIvKey: String by lazy { getEncryptIvKeyNative() }
    val encryptKey: String by lazy { getEncryptKeyNative() }
    val cryptLibAes: String by lazy { getCryptLibAesNative() }
    val cryptId: String by lazy { getCryptIdNative() }
    val cryptIv: String by lazy { getCryptIvNative() }

    fun getKey(keyName: String): String = getKeyByNameNative(keyName)

    val REFRESH_TOKEN_URL: String by lazy { nativeRefreshTokenUrl() }
    val CLIENT_SECRET_KEY: String by lazy { nativeClientSecretKey() }
    val WADH_KEY: String by lazy { nativeWadhKey() }

}
