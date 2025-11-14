#include <jni.h>
#include <string>

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_getEncryptIvKeyNative(JNIEnv* env, jobject) {
    return env->NewStringUTF("$10A80$10A80$10A80$10A80$10A80$10A80$10A80$10A80");
}

JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_getEncryptKeyNative(JNIEnv* env, jobject) {
    return env->NewStringUTF("$10A80$10A80$10A80$10A80$10A80$10A80$10A80$10A80");
}

JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_getCryptLibAesNative(JNIEnv* env, jobject) {
    return env->NewStringUTF("AES/CBC/PKCS5PADDING");
}

JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_getCryptIdNative(JNIEnv* env, jobject) {
    return env->NewStringUTF("8080808080808080");
}

JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_getCryptIvNative(JNIEnv* env, jobject) {
    return env->NewStringUTF("8080808080808080");
}
  
JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_nativeRefreshTokenUrl(JNIEnv *env, jobject) {
    return env->NewStringUTF("jhbheugcy2373y379y37gydygdy");
}


JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_nativeClientSecretKey(JNIEnv *env, jobject) {
    return env->NewStringUTF("dgtbncbehkcbjebccnkec78yf37bc");
}


JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_nativeWadhKey(JNIEnv *env, jobject) {
    return env->NewStringUTF("sgydIC09zzy6f8Lb3xaAqzKquKe9lFcNR9uTvYxFp+A=");
}


JNIEXPORT jstring JNICALL
Java_com_kaushalpanjee_security_SecureConfig_getKeyByNameNative(JNIEnv* env, jobject, jstring keyName) {
    const char *nativeKeyName = env->GetStringUTFChars(keyName, nullptr);

    std::string result;
    std::string keyStr(nativeKeyName);

    if (keyStr == "encrypt_iv_key") {
        result = "$10A80$10A80$10A";
    } else if (keyStr == "encrypt_key") {
        result = "$10A80$10A80$10A";
    } else if (keyStr == "crypt_lib_aes") {
        result = "AES/CBC/PKCS5PADDING";
    } else if (keyStr == "crypt_id") {
        result = "8080808080808080";
    } else if (keyStr == "crypt_iv") {
        result = "8080808080808080";
    } else {
        result = "";
    }
    env->ReleaseStringUTFChars(keyName, nativeKeyName);
    return env->NewStringUTF(result.c_str());
}

}
