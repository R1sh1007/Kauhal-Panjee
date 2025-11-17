package com.kaushalpanjee.core.util.optimize


import android.content.Context
import androidx.core.content.edit

private const val PREF_APP = "app_preferences"

fun Context.saveString(key: String, value: String) {
    getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
        .edit { putString(key, value) }
}

fun Context.getStringPref(key: String, default: String = ""): String {
    return getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
        .getString(key, default) ?: default
}

fun Context.saveBoolean(key: String, value: Boolean) {
    getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
        .edit { putBoolean(key, value) }
}

fun Context.getBooleanPref(key: String, default: Boolean = false): Boolean {
    return getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
        .getBoolean(key, default)
}
