package com.kaushalpanjee.core.util.optimize




object CrashlyticsUtil {

//    private val crashlytics: FirebaseCrashlytics by lazy {
//        FirebaseCrashlytics.getInstance()
//    }

    /** Log non-fatal exception */
    fun logException(throwable: Throwable) {
//        safeCall {
//            crashlytics.recordException(throwable)
//        }
    }

    /** Log simple string messages */
    fun logMessage(message: String) {
//        safeCall {
//            crashlytics.log(message)
//        }
    }

    /** Add crashlytics custom key */
    fun setKey(key: String, value: String) {
//        safeCall {
//            crashlytics.setCustomKey(key, value)
//        }
    }

    /** Set logged-in user ID */
    fun setUserId(userId: String?) {
//        safeCall {
//            crashlytics.setUserId(userId ?: "")
//        }
    }

    /** Safe wrapper avoids crashes if Firebase is misconfigured */
    private inline fun safeCall(block: () -> Unit) {
        try {
            block()
        } catch (_: Exception) {
            // Avoid crash — silently ignore in release
        }
    }
}
