package com.kaushalpanjee

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.kaushalpanjee.common.CommonActivity
import com.kaushalpanjee.core.basecomponent.BaseActivity
import com.kaushalpanjee.core.util.AppUtil
import com.kaushalpanjee.databinding.ActivityWelcomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class WelcomeActivity : BaseActivity<ActivityWelcomeBinding>(
    ActivityWelcomeBinding::inflate
)
{
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

       // AppUtil.changeAppLanguage(this, userPreferences.getLanguage())
        AppUtil.changeAppLanguage(this, AppUtil.getSavedLanguagePreference(this))

                lifecycleScope.launch {
            delay(2000)

            /*if (isDeviceRooted() || isRunningOnEmulator()) {
                showSecurityWarning()
            }*/
            if (isDeviceRooted()) {
            showSecurityWarning()
            }
            else {
                navigate()
            }
        }
    }


    private fun navigate() {
        startActivity(Intent(this@WelcomeActivity, CommonActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    /**
     * Checks if the device is rooted.
     */
    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/system/xbin/mu"
        )
        return paths.any { File(it).exists() }
    }

    /**
     * Checks if the app is running on an emulator.
     */
    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.lowercase().contains("emulator")
                || Build.MODEL.lowercase().contains("android sdk built for x86")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("sdk_gphone")
                || Build.BOARD.lowercase().contains("unknown")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Shows an alert dialog if the device is rooted or an emulator.
     */
    private fun showSecurityWarning() {
        val message = when {
            isDeviceRooted() && isRunningOnEmulator() -> "Rooted device and Emulator detected! For security reasons, this app cannot run."
            isDeviceRooted() -> "Rooted device detected! For security reasons, this app cannot run."
            isRunningOnEmulator() -> "Emulator detected! This app cannot run on emulators."
            else -> return
        }

        AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Exit") { _, _ -> finishAffinity() }
            .show()
    }
}
