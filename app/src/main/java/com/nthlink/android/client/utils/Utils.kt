package com.nthlink.android.client.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_TEXT
import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.ValueCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.nthlink.android.client.R
import com.nthlink.android.core.utils.NO_RESOURCE

const val SHARE_TYPE_TEXT = "text/plain"

fun removeAllCookies(callback: ValueCallback<Boolean>? = null) {
    CookieManager.getInstance().removeAllCookies(callback)
}

fun installFromGooglePlay(context: Context): Boolean {
    val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
    } else {
        context.packageManager.getInstallerPackageName(context.packageName)
    }

    return installer != null && "com.android.vending" == installer
}

suspend fun requireRatingApp(activity: Activity) {
    ReviewManagerFactory.create(activity).run {
        val reviewInfo = requestReview()
        launchReview(activity, reviewInfo)
    }
}

fun showMessageDialog(
    context: Context,
    @StringRes messageId: Int,
    @StringRes positiveText: Int = R.string.ok,
    onPositive: (() -> Unit)? = null,
    @StringRes negativeText: Int = R.string.cancel,
    onNegative: (() -> Unit)? = null
) {
    showMaterialAlertDialog(context) {
        setCancelable(false)
        setMessage(messageId)
        setPositiveButton(positiveText) { dialog, _ ->
            onPositive?.invoke()
            dialog.dismiss()
        }

        onNegative?.let {
            setNegativeButton(negativeText) { dialog, _ ->
                it.invoke()
                dialog.dismiss()
            }
        }
    }
}

fun getLoadWebUrlIntent(url: String) = Intent(ACTION_VIEW, Uri.parse(url))

fun showMaterialAlertDialog(
    context: Context,
    overrideThemeResId: Int = NO_RESOURCE,
    setBuilder: MaterialAlertDialogBuilder.() -> Unit
): AlertDialog {
    val builder = MaterialAlertDialogBuilder(context, overrideThemeResId)
    builder.setBuilder()
    return builder.show()
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

fun getSendTextIntent(text: String, type: String = SHARE_TYPE_TEXT) = Intent().apply {
    action = ACTION_SEND
    putExtra(EXTRA_TEXT, text)
    this.type = type
}