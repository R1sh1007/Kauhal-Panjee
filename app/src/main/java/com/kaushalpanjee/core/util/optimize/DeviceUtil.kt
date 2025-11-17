package com.kaushalpanjee.core.util.optimize

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.*

object DeviceUtil {

    @SuppressLint("HardwareIds")
    fun androidId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    fun deviceInfo(): String =
        "MODEL: ${Build.MODEL}, MANUFACTURER: ${Build.MANUFACTURER}, DEVICE: ${Build.DEVICE}"

    fun timeZoneId(): String = TimeZone.getDefault().id

    fun timeZoneOffsetMinutes(): Int {
        val tz = TimeZone.getDefault()
        return tz.rawOffset / 60000
    }
}
