package com.kaushalpanjee.core.util.optimize

import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.Fragment


//For Dialog Extension Function

fun Context.showAlert(
    title: String? = null,
    message: String,
    positive: String = "OK",
    onPositive: (() -> Unit)? = null
) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(positive) { _, _ -> onPositive?.invoke() }
        show()
    }
}

fun Fragment.showAlert(
    title: String? = null,
    message: String,
    positive: String = "OK",
    onPositive: (() -> Unit)? = null
) {
    context?.showAlert(title, message, positive, onPositive)
}

fun Context.showConfirm(
    title: String? = null,
    message: String,
    yes: String = "Yes",
    no: String = "No",
    onYes: (() -> Unit)? = null,
    onNo: (() -> Unit)? = null
) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(yes) { _, _ -> onYes?.invoke() }
        setNegativeButton(no) { _, _ -> onNo?.invoke() }
        show()
    }
}