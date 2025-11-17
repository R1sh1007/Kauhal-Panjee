package com.kaushalpanjee.core.util.optimize

import android.content.Context
import android.net.Uri
import android.util.Base64

object FileUtil {

    fun makeFileName(userId: Int?) = "${userId ?: "user"}_${System.currentTimeMillis()}.jpg"

    fun uriToBase64(context: Context, uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use {
            return Base64.encodeToString(it.readBytes(), Base64.NO_WRAP)
        }
        return ""
    }
}
