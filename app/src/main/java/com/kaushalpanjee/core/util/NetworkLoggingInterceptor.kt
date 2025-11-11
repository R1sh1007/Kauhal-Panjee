package com.kaushalpanjee.core.util

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log

class NetworkLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("API_LOG", message)   // prints in Logcat
        }

        logging.apply {
            level = HttpLoggingInterceptor.Level.BODY  // Logs URL + headers + body
        }

        return logging.intercept(chain)
    }
}
