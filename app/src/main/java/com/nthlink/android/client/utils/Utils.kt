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
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory

const val EMPTY = ""
const val ZERO = 0
const val NO_RESOURCE = ZERO
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

fun showMaterialAlertDialog(
    context: Context,
    overrideThemeResId: Int = NO_RESOURCE,
    setBuilder: MaterialAlertDialogBuilder.() -> Unit
): AlertDialog {
    val builder = MaterialAlertDialogBuilder(context, overrideThemeResId)
    builder.setBuilder()
    return builder.show()
}

fun showAlertDialog(
    context: Context,
    @StyleRes themeResId: Int = NO_RESOURCE,
    setBuilder: AlertDialog.Builder.() -> Unit
): AlertDialog {
    val builder = AlertDialog.Builder(context, themeResId)
    builder.setBuilder()
    return builder.show()
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

fun getLoadWebUrlIntent(url: String) = Intent(ACTION_VIEW, Uri.parse(url))

fun getSendTextIntent(text: String, type: String = SHARE_TYPE_TEXT) = Intent().apply {
    action = ACTION_SEND
    putExtra(EXTRA_TEXT, text)
    this.type = type
}