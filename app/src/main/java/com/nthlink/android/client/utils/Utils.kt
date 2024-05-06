package com.nthlink.android.client.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.ValueCallback
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory

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