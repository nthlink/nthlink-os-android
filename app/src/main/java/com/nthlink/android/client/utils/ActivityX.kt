package com.nthlink.android.client.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.nthlink.android.client.R

fun AppCompatActivity.showAlertDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    cancelable: Boolean = false
) {
    showMaterialAlertDialog(context = this) {
        setTitle(titleRes)
        setMessage(messageRes)
        setCancelable(cancelable)
        setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
    }
}

fun AppCompatActivity.showAlertDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    cancelable: Boolean = false,
    okListener: () -> Unit
) {
    showMaterialAlertDialog(context = this) {
        setTitle(titleRes)
        setMessage(messageRes)
        setCancelable(cancelable)
        setPositiveButton(R.string.ok) { dialog, _ ->
            okListener()
            dialog.dismiss()
        }
        setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    }
}

fun AppCompatActivity.openWebPage(url: String) {
    val intent = getLoadWebUrlIntent(url)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}